public class ChatLauncher {
    public static void main(String[] args) {
        // Start the server in a new thread
        new Thread(() -> {
            ChatServer.main(null);
        }).start();

        // Delay slightly so server starts first
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Launch Client 1
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ChatClientGUI("Cliet").setVisible(true);
        });

        // Launch Client 2
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ChatClientGUI("Server").setVisible(true);
        });
    }
}
