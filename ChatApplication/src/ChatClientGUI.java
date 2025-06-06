import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ChatClientGUI extends JFrame {
    private ChatClient client = new ChatClient();
    private JTextPane chatPane = new JTextPane();
    private JTextField inputField = new JTextField();
    private JButton sendButton = new JButton("Send");
    private JButton fileButton = new JButton("Send File");

    public ChatClientGUI(String title) {
        setTitle(title);
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatPane.setEditable(false);
        chatPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        chatPane.setBackground(Color.BLACK);
        chatPane.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        inputField.setBackground(Color.DARK_GRAY);
        inputField.setForeground(Color.WHITE);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        sendButton.setBackground(new Color(255, 215, 0));
        sendButton.setForeground(Color.BLACK);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        fileButton.setBackground(Color.ORANGE);
        fileButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.add(fileButton, BorderLayout.WEST);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        inputField.addActionListener(e -> send());
        sendButton.addActionListener(e -> send());
        fileButton.addActionListener(e -> sendFile());

        connectToServer();
    }

    private void connectToServer() {
        if (client.connect("127.0.0.1", 12345)) {
            appendMessage("Connected to server.", Color.GREEN);
            new Thread(() -> {
                try {
                    InputStream is = client.getInputStreamRaw();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("[FILE]|")) {
                            String[] parts = line.split("\\|");
                            String filename = parts[1];
                            long filesize = Long.parseLong(parts[2]);

                            FileOutputStream fos = new FileOutputStream("received_" + filename);
                            byte[] buffer = new byte[4096];
                            long totalRead = 0;
                            int read;
                            while (totalRead < filesize && (read = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, read);
                                totalRead += read;
                            }
                            fos.close();
                            appendMessage("Received file: " + filename, Color.MAGENTA);
                        } else {
                            Color color = line.startsWith("Client") ? Color.YELLOW : new Color(255, 68, 68);
                            appendMessage(line, color);
                        }
                    }
                } catch (Exception e) {
                    appendMessage("Connection lost.", Color.RED);
                }
            }).start();
        } else {
            appendMessage("Failed to connect to server.", Color.RED);
        }
    }

    private void send() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            String formatted = getTitle() + ": " + msg;
            client.sendMessage(formatted);
            appendMessage(formatted, Color.YELLOW);
            inputField.setText("");
        }
    }

    private void sendFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            appendMessage("Sending file: " + file.getName(), Color.CYAN);
            client.sendFile(file);
        }
    }

    private void appendMessage(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatPane.getStyledDocument();
            Style style = chatPane.addStyle("Color Style", null);
            StyleConstants.setForeground(style, color);
            try {
                doc.insertString(doc.getLength(), message + "\n", style);
                chatPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI("Client 1").setVisible(true));
    }
}
