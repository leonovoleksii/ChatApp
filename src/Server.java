import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int port = 7373;
    private static ServerSocket serverSocket;
    private static ArrayList<Socket> clientSockets = new ArrayList<>();
    private static TreeSet<String> names = new TreeSet<>();

    static class InputProcessor implements Runnable {
        Socket clientSocket;
        String name;
        BufferedReader in;
        BufferedWriter out;
        InputProcessor(Socket socket) {
            clientSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("Error! Unable to create io streams");
            }
        }

        private String getMessage() throws IOException {
            // get length of the name
            int rpart = in.read();
            if (rpart == -1) {
                clientSocket.close();
                clientSockets.remove(clientSocket);
                names.remove(name);
                return "is no longer in chat";
            }
            int lpart = in.read();
            int length = rpart + (lpart << 8);

            // get the name
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++)
                sb.append((char) in.read());

            return sb.toString();
        }

        private void sendMessageAll(String message) throws IOException {
            for (Socket client : clientSockets) {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                bw.write(message.length() & 255);
                bw.write((message.length() >> 8) & 255);
                bw.write(message);
                bw.flush();
            }
        }

        @Override
        public void run() {
            // logging in user
            do {
                try {
                    name = getMessage();
                    if (names.contains(name)) {
                        out.write(0);
                        out.flush();
                    }
                    System.out.println("Try to login with name " + name);
                } catch (IOException e) {
                    System.out.println("Unable to get the name of user");
                }
            } while (clientSocket.isConnected() && names.contains(name));

            // user logged in
            clientSockets.add(clientSocket);
            try {
                out.write(1);
                out.flush();
                names.add(name);
                System.out.println("Logged in");
                sendMessageAll(name + " logged in");
            } catch (IOException e) {
                System.out.println("Unable to send answer to user");
            }

            // getting messages from user
            while (!clientSocket.isClosed()) {
               try {
                   String message = "[" + name + "]" + ":\n" + getMessage();
                   sendMessageAll(message);

               } catch (IOException e) {
                   System.out.println("Error! Unpredicted io behavior");
               }
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
                ex.execute(new InputProcessor(clientSocket));
            } catch (IOException e) {
                System.out.println("Error! Unable to create the connection");
            }
        }
    }
}
