import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class Client {
    private static Socket clientSocket;
    private static String host;
    private static int port;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static Frame frame;

    static class Frame {
        JFrame frame;
        JTextArea messages;
        JTextArea message;
        JButton sendBtn;
        JTextField name;
        JButton loginBtn;
        JLabel wrongLoginLabel;

        Frame() {
            try {
                SwingUtilities.invokeAndWait(new LoggerUI());
            } catch (Exception e) {}
            processName(this);
            try {
                SwingUtilities.invokeAndWait(new ChatUI());
            } catch (Exception e) {}
            processMessage(this);
        }

        class ButtonListener implements ActionListener {
            private void sendMessage(JTextComponent t) {
                String m = t.getText();
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
                t.setText("");
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Send")) {
                    sendMessage(message);
                }
                if (e.getActionCommand().equals("Login")) {
                    sendMessage(name);
                }
            }
        }

        class ChatUI implements Runnable {
            @Override
            public void run() {
                name.setVisible(false);
                wrongLoginLabel.setVisible(false);
                loginBtn.setVisible(false);
                frame.remove(name);
                frame.remove(wrongLoginLabel);
                frame.remove(loginBtn);

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
                frame.setVisible(true);
            }
        }

        class LoggerUI implements Runnable {
            @Override
            public void run() {
                frame = new JFrame("Chat");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new FlowLayout());
                frame.setSize(500, 400);
                wrongLoginLabel = new JLabel("User with this name already exists. Try again please");
                wrongLoginLabel.setVisible(false);
                frame.add(wrongLoginLabel);
                name = new JTextField("Name", 34);
                frame.add(name);
                loginBtn = new JButton("Login");
                loginBtn.addActionListener(new ButtonListener());
                frame.add(loginBtn);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        }

        void appendMessage(String s) {
            messages.append(s);
        }
    }

    public static void processMessage(Frame frame) {
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

    public static void processName(Frame frame) {
        int answer = 1;
        do {
            try {
                if (answer == 0) {
                    frame.wrongLoginLabel.setVisible(true);
                }
                answer = in.read();
            } catch (IOException e) {
                System.out.println("Unable to get answer from server");
            }
        } while (answer == 0);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage - java Client 'host ip' 'port'");
            return;
        }
        host = args[0];
        port = Integer.parseInt(args[1]);
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

        frame = new Frame();
    }
}
