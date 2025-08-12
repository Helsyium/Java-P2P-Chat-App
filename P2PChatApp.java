
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class P2PChatApp extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private ConnectionManager connectionManager;

    public P2PChatApp() {
        super("P2P Sohbet Uygulaması");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        JButton sendButton = new JButton("Gönder");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }
    
    // Mesaj gönderme metodu
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && connectionManager != null) {
            connectionManager.broadcastMessage(message);
            chatArea.append("Sen: " + message + "\n");
            messageField.setText("");
        }
    }

    // Arayüze mesaj eklemek için kullanılan metot
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    public void setConnectionManager(ConnectionManager manager) {
        this.connectionManager = manager;
    }
    
    // Uygulamayı başlatma metodu
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            P2PChatApp app = new P2PChatApp();
            
            // Kullanıcı adını al
            String username = JOptionPane.showInputDialog("Kullanıcı adınızı girin:");
            if (username == null || username.trim().isEmpty()) {
                username = "Kullanıcı" + System.currentTimeMillis() % 1000;
            }
            app.setTitle("P2P Sohbet - " + username);
            
            // Bağlantı yöneticisini başlat
            ConnectionManager manager = new ConnectionManager(app);
            app.setConnectionManager(manager);
            
            // Sunucu modunu başlat (her düğüm bir sunucu olacak)
            manager.startServer(8080);
            
            // Kullanıcıdan başka düğümlere bağlanma seçeneği sun
            while (true) {
                String peerIp = JOptionPane.showInputDialog(
                    "Bağlanmak istediğiniz bilgisayarın IP adresini girin:\n" +
                    "(Başka bağlantı yoksa veya işiniz bittiğinde boş bırakın)");
                
                if (peerIp == null || peerIp.trim().isEmpty()) {
                    break;
                }
                
                manager.connectToPeer(peerIp, 8080);
            }
            
            // Uygulama kapatılırken kaynakları temizle
            app.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    manager.shutdown();
                    System.exit(0);
                }
            });
        });
    }
}