import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public BufferedReader getInputStream() {
        return in;
    }

    public InputStream getInputStreamRaw() throws IOException {
        return socket.getInputStream();
    }

    public void sendFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            out.println("[FILE]|" + file.getName() + "|" + file.length());
            out.flush();

            byte[] buffer = new byte[4096];
            OutputStream os = socket.getOutputStream();
            int count;
            while ((count = fis.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            os.flush();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
