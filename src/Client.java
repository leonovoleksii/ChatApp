import java.io.IOException;
import java.net.Socket;

public class Client {
    private static Socket clientSocket;
    private static String host = "localhost";
    private static int port = 7373;

    public static void main(String[] args) {
        try {
            clientSocket = new Socket(host, port);
        } catch (IOException e) {
            System.out.println("Error! Unable to connect to " + host + ":" + port);
        }
    }
}
