import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager {

    private P2PChatApp app;
    private Socket socket;
    private PrintWriter out;
    private MessageListener messageListener;

    public ConnectionManager(P2PChatApp app) {
        this.app = app;
    }

    // Sunucu modunu başlatma metodu
    public void startServer(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                app.appendMessage("Bağlantı bekleniyor...");
                socket = serverSocket.accept(); // Bağlantı gelene kadar bekler
                app.appendMessage("Bağlantı kuruldu!");
                setupStreams();
                serverSocket.close();
            } catch (IOException e) {
                app.appendMessage("Sunucu hatası: " + e.getMessage());
            }
        }).start();
    }

    // İstemci modunda bir eşe (peer) bağlanma metodu
    public void connectToPeer(String ipAddress, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(ipAddress, port);
                app.appendMessage("Bağlantı kuruldu!");
                setupStreams();
            } catch (IOException e) {
                app.appendMessage("Bağlantı hatası: " + e.getMessage());
            }
        }).start();
    }
    
    // Veri akışlarını kurma ve dinleyiciyi başlatma metodu
    private void setupStreams() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        messageListener = new MessageListener(app, socket);
        messageListener.start();
    }

    // Mesaj gönderme metodu
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}