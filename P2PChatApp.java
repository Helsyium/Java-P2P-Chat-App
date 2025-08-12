/* 
P2PChatApp.java dosyasında neler oluyor?

main metodu, uygulamayı başlattığında bir giriş penceresi açıyor. Burada diğer bilgisayarın IP adresini giriyorsun.

Eğer bir IP adresi girersen, uygulama o adrese bağlanmaya çalışıyor (istemci modu).

Eğer boş bırakırsan, uygulama bağlantı beklemeye başlıyor (sunucu modu).

appendMessage metodu, başka sınıflardan gelen mesajları sohbet alanına eklememizi sağlıyor.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
        String message = messageField.getText();
        if (!message.trim().isEmpty() && connectionManager != null) {
            connectionManager.sendMessage(message);
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
            String peerIp = JOptionPane.showInputDialog("Bağlanılacak bilgisayarın IP adresini girin (Bağlantı beklemek için boş bırakın):");
            
            ConnectionManager manager = new ConnectionManager(app);
            app.setConnectionManager(manager);
            
            // Eğer bir IP adresi girilirse istemci modunda çalışır
            if (peerIp != null && !peerIp.trim().isEmpty()) {
                manager.connectToPeer(peerIp, 8080);
            } else {
                // IP adresi girilmezse sunucu modunda çalışır
                manager.startServer(8080);
            }
        });
    }
}