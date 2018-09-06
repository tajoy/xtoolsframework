package x.tools.framework.event;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import static x.tools.framework.XUtils.getProcessName;


public class EventBusServer implements Closeable {
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

        private ClientHandler(LocalSocket socket) throws IOException {
            this.socket = socket;
            this.eventReader = new EventReader();
            this.eventWriter = new EventWriter();
            this.eventReader.setInputStream(socket.getInputStream());
            this.eventWriter.setOutputStream(socket.getOutputStream());
            this.setName("ClientHandler["
                    + getProcessName(socket.getPeerCredentials().getPid())
                    + "]"
            );
            this.setDaemon(true);
            this.start();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Event event = this.eventReader.readEvent();
                    EventBusServer.this.dispatchAll(event, this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            EventBusServer.this.sockets.remove(this);
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
