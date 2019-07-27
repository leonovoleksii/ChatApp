import java.awt.event.*;
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
                    messages.setLineWrap(true);
                    messages.setMaximumSize(new Dimension(500, 300));
                    frame.add(messages);
                    message = new JTextArea(8, 34);
                    message.setMaximumSize(new Dimension(400, 100));
                    message.setLineWrap(true);
                    frame.add(message);
                    sendBtn = new JButton("Send");
                    sendBtn.addActionListener(new ButtonListener());
                    frame.add(sendBtn);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            });
        }

        class ButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Send")) {
                    String m = message.getText();
                    int mLength = m.length();
                    try {
                        // send the length of the message
                        out.write(mLength & 255);
                        out.write((mLength >> 8) & 255);
                        // send the message
                        out.write(m);
                        out.flush();
                    } catch (IOException exc) {
                        System.out.println("Error! Unable to send a message");
                    }
                    message.setText("");
                }
            }
        }

        void appendMessage(String s) {
            messages.append(s);
        }
    }

    static class InputProcessor implements Runnable {
        Frame frame;
        InputProcessor(Frame frame) { this.frame = frame; }

        public void run() {
            try {
                while (clientSocket.isConnected()) {
                    // get the length of the message
                    int rpart = in.read();
                    if (rpart == -1) {
                        clientSocket.close();
                        break;
                    }
                    int lpart = in.read();
                    int mlength = rpart + (lpart << 8);

                    // get the message
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mlength; i++) {
                        sb.append((char)in.read());
                    }
                    frame.appendMessage(sb.toString() + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error! Unable to read a message from server");
            }
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

        Thread t = new Thread(new InputProcessor(frame));
        t.setDaemon(true);
        t.start();
    }
}
