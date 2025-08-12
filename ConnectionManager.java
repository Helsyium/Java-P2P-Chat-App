import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ConnectionManager {
    private final P2PChatApp app;
    private final Set<Socket> activeConnections = ConcurrentHashMap.newKeySet();
    private final Set<PrintWriter> outputStreams = ConcurrentHashMap.newKeySet();
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public ConnectionManager(P2PChatApp app) {
        this.app = app;
    }
    
    // Tüm bağlantıları kapatma metodu
    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            for (Socket socket : activeConnections) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            threadPool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sunucu modunu başlatma metodu
    public void startServer(int port) {
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                app.appendMessage("Sunucu başlatıldı. Bağlantılar dinleniyor...");
                
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        activeConnections.add(clientSocket);
                        app.appendMessage("Yeni bağlantı: " + clientSocket.getInetAddress().getHostAddress());
                        
                        // Her yeni bağlantı için ayrı bir iş parçacığı başlat
                        handleNewConnection(clientSocket);
                    } catch (SocketException e) {
                        if (!serverSocket.isClosed()) {
                            app.appendMessage("Sunucu hatası: " + e.getMessage());
                        }
                        break;
                    } catch (IOException e) {
                        app.appendMessage("Bağlantı hatası: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                app.appendMessage("Sunucu başlatılamadı: " + e.getMessage());
            }
        });
    }

    // İstemci modunda bir eşe (peer) bağlanma metodu
    public void connectToPeer(String ipAddress, int port) {
        threadPool.execute(() -> {
            try {
                Socket peerSocket = new Socket(ipAddress, port);
                activeConnections.add(peerSocket);
                app.appendMessage(ipAddress + " adresine bağlantı kuruldu!");
                handleNewConnection(peerSocket);
            } catch (IOException e) {
                app.appendMessage(ipAddress + " adresine bağlanılamadı: " + e.getMessage());
            }
        });
    }
    
    // Yeni bağlantıyı işleme metodu
    private void handleNewConnection(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            outputStreams.add(out);
            
            // Her bağlantı için ayrı bir mesaj dinleyici başlat
            MessageListener messageListener = new MessageListener(app, socket, this);
            messageListener.start();
            
        } catch (IOException e) {
            app.appendMessage("Bağlantı hatası: " + e.getMessage());
            closeConnection(socket);
        }
    }
    
    // Bağlantıyı kapatma metodu
    public void closeConnection(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) {
                activeConnections.remove(socket);
                socket.close();
                app.appendMessage("Bağlantı kapatıldı: " + socket.getInetAddress().getHostAddress());
            }
        } catch (IOException e) {
            app.appendMessage("Bağlantı kapatılırken hata: " + e.getMessage());
        }
    }

    // Tüm bağlı düğümlere mesaj gönderme metodu
    public void broadcastMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        for (PrintWriter out : outputStreams) {
            try {
                out.println(message);
            } catch (Exception e) {
                app.appendMessage("Mesaj gönderilirken hata: " + e.getMessage());
                // Hatalı çıktı akışlarını temizle
                outputStreams.remove(out);
            }
        }
    }
}
