import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class MessageListener extends Thread {
    private final P2PChatApp app;
    private final Socket socket;
    private final ConnectionManager connectionManager;

    public MessageListener(P2PChatApp app, Socket socket, ConnectionManager connectionManager) {
        this.app = app;
        this.socket = socket;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            String peerAddress = socket.getInetAddress().getHostAddress();
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if ((line = in.readLine()) == null) {
                        break; // Bağlantı koptu
                    }
                    app.appendMessage(peerAddress + ": " + line);
                } catch (SocketException e) {
                    if (!socket.isClosed()) {
                        app.appendMessage("Bağlantı hatası: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            app.appendMessage("Giriş akışı hatası: " + e.getMessage());
        } finally {
            connectionManager.closeConnection(socket);
        }
    }
}
