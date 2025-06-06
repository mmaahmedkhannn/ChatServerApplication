import java.io.*;
import java.net.*;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clients;

    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void sendRaw(byte[] data) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(data);
        os.flush();
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream os = socket.getOutputStream();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("[FILE]|")) {
                    String[] parts = line.split("\\|");
                    String filename = parts[1];
                    long filesize = Long.parseLong(parts[2]);

                    byte[] buffer = new byte[4096];
                    InputStream is = socket.getInputStream();

                    ByteArrayOutputStream fileContent = new ByteArrayOutputStream();
                    int read;
                    long totalRead = 0;
                    while (totalRead < filesize && (read = is.read(buffer)) > 0) {
                        fileContent.write(buffer, 0, read);
                        totalRead += read;
                    }
                    byte[] fileBytes = fileContent.toByteArray();

                    for (ClientHandler client : clients) {
                        if (client != this) {
                            client.sendMessage(line); // send file header
                            client.sendRaw(fileBytes); // send file bytes
                        }
                    }
                } else {
                    for (ClientHandler client : clients) {
                        if (client != this) {
                            client.sendMessage(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
        } finally {
            try {
                clients.remove(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
