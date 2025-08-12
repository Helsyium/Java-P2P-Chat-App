import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageListener extends Thread {

    private P2PChatApp app;
    private Socket socket;

    public MessageListener(P2PChatApp app, Socket socket) {
        this.app = app;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                app.appendMessage("Arkadaş: " + line);
            }
        } catch (IOException e) {
            app.appendMessage("Bağlantı koptu.");
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}