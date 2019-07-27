import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int port = 7373;
    private static ServerSocket serverSocket;
    private static ArrayList<Socket> clientSockets = new ArrayList<>();

    static class InputProcessor implements Runnable {
        Socket clientSocket;
        BufferedReader in;
        BufferedWriter out;
        InputProcessor(Socket socket) {
            clientSocket = socket;
            System.out.println(clientSocket);
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("Error! Unable to create io streams");
            }
        }

        @Override
        public void run() {
            while (clientSocket.isConnected()) {
               try {
                   // get the length of the message
                   int rpart = in.read();
                   if (rpart == -1) {
                       clientSocket.close();
                       clientSockets.remove(clientSocket);
                       break;
                   }
                   int lpart = in.read();
                   int length = rpart + (lpart << 8);

                   // get the message
                   StringBuilder sb = new StringBuilder();
                   for (int i = 0; i < length; i++)
                       sb.append((char)in.read());
                   System.out.println(sb.toString());

               } catch (IOException e) {}
            }
        }
    }

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error! Unable to start the server on port " + port);
            return;
        }

        ExecutorService ex = Executors.newCachedThreadPool();
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                ex.execute(new InputProcessor(clientSocket));
            } catch (IOException e) {
                System.out.println("Error! Unable to create the connection");
            }
        }
    }
}
