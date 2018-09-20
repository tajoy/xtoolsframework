package x.tools.eventbus;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import x.tools.log.Loggable;

public class EventBusServer implements Closeable, Loggable {
    private LocalServerSocket serverSocket;
    private Thread acceptor;
    private final Set<ClientHandler> sockets =
            Collections.synchronizedSet(
                    Collections.newSetFromMap(
                            new HashMap<>()
                    )
            );

    EventBusServer(String address) throws IOException {
        this.serverSocket = new LocalServerSocket(address);
        this.acceptor = new Thread(this::accepting);
        this.acceptor.setName(
                "EventBusServer["
                        + serverSocket.getLocalSocketAddress().getName()
                        + "]"
        );
        this.acceptor.setDaemon(true);
        this.acceptor.start();
    }


    private void accepting() {
        while (!Thread.currentThread().isInterrupted() && this.serverSocket != null) {
            try {
                LocalSocket socket = this.serverSocket.accept();
                int pid = socket.getPeerCredentials().getPid();
                debug("accept client: %d %s", pid, EventBus.getProcessName(pid));
                this.sockets.add(new ClientHandler(socket));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        synchronized (this.sockets) {
            for (ClientHandler handler : this.sockets) {
                try {
                    handler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.sockets.clear();
    }

    private void dispatchAll(Event event, ClientHandler excludedHandler) throws IOException {
        synchronized (this.sockets) {
            for (ClientHandler handler : this.sockets) {
                if (!handler.equals(excludedHandler)) {
                    handler.dispatchEvent(event);
                }
            }
        }
    }

    private class ClientHandler extends Thread {
        private final LocalSocket socket;
        private EventReader eventReader;
        private EventWriter eventWriter;
        private int pid;

        private ClientHandler(LocalSocket socket) throws IOException {
            this.socket = socket;
            this.pid = socket.getPeerCredentials().getPid();
            this.eventReader = new EventReader();
            this.eventWriter = new EventWriter();
            this.eventReader.setInputStream(socket.getInputStream());
            this.eventWriter.setOutputStream(socket.getOutputStream());
            this.setName("ClientHandler["
                    + EventBus.getProcessName(socket.getPeerCredentials().getPid())
                    + "]"
            );
            this.setDaemon(true);
            this.start();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientHandler)) return false;
            ClientHandler that = (ClientHandler) o;
            return pid == that.pid;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pid);
        }

        @Override
        public void run() {
            while (!isInterrupted() && this.eventReader.getInputStream() != null) {
                Event event = this.eventReader.readEvent();
                if (event != null) {
                    try {
                        EventBusServer.this.dispatchAll(event, this);
                    } catch (IOException e) {
                        error(e);
                    }
                } else {
                    debug("inputStream == null, wait 1000ms");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
            EventBusServer.this.sockets.remove(this);
            try {
                this.socket.close();
            } catch (IOException ignore) {
            }
        }

        private void close() throws IOException {
            this.socket.close();
            this.interrupt();
        }

        private void dispatchEvent(Event event) {
            this.eventWriter.writeEvent(event);
        }
    }


    @Override
    public void close() throws IOException {
        this.acceptor.interrupt();
    }
}
