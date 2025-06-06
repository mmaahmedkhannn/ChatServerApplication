import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            username = reader.readLine();
            ChatServer.broadcast(username + " joined the chat!", this);
            
            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                ChatServer.broadcast(username + ": " + clientMessage, this);
            }
        } catch (IOException e) {
            System.out.println("Error in ClientHandler: " + e.getMessage());
        } finally {
            try {
                reader.close();
                writer.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            ChatServer.broadcast(username + " left the chat!", this);
        }
    }
    
    public void sendMessage(String message) {
        writer.println(message);
    }
}