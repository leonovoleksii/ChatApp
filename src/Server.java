import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static int port = 7373;
    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error! Unable to start the server on port " + port);
            return;
        }
        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Error! Unable to create the connection");
            }
        }
    }
}
