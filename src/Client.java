import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;

public class Client {
    private static Socket clientSocket;
    private static String host = "localhost";
    private static int port = 7373;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static Frame frame;

    static class Frame {
        JFrame frame;
        JTextArea messages;
        JTextArea message;
        JButton sendBtn;
        Frame() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    frame = new JFrame("Chat");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(500, 400);
                    frame.setLayout(new FlowLayout());
                    messages = new JTextArea(15, 40);
                    messages.setEditable(false);
                    frame.add(messages);
                    message = new JTextArea(8, 34);
                    frame.add(message);
                    sendBtn = new JButton("Send");
                    frame.add(sendBtn);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            });
        }
    }

    public static void main(String[] args) {
        frame = new Frame();

        try {
            clientSocket = new Socket(host, port);
        } catch (IOException e) {
            System.out.println("Error! Unable to connect to " + host + ":" + port);
        }

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error! Unable to create io channels");
        }

    }
}
