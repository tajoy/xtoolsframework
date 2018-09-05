package x.tools.framework.event;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;


public class EventBusServer {
    private LocalServerSocket serverSocket;
    private Thread acceptor;
    private final Set<ClientHandler> sockets =
            Collections.synchronizedSet(
                    Collections.newSetFromMap(
                            new HashMap<>()
                    )
            );

    EventBusServer(String name) throws IOException {
        this.serverSocket = new LocalServerSocket(name);
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
    }

    private void dispatchAll(Event event, ClientHandler excludedHandler) throws IOException {
        synchronized (this.sockets) {
            for (ClientHandler handler : this.sockets) {
                try {
                    if (!handler.equals(excludedHandler)) {
                        handler.dispatchEvent(event);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
            this.setName("ClientHandler["
                    + socket.getRemoteSocketAddress().getName()
                    + "]"
            );
            this.setDaemon(true);
            this.start();
        }

        @Override
        public void run() {
            while (!isInterrupted() && !this.socket.isClosed()) {
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

        private void dispatchEvent(Event event) throws IOException {
            this.eventWriter.writeEvent(event);
        }
    }


}
