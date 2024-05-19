import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
    private JFrame frame;
    private JTextField cardidField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton checkBalanceButton;
    private JButton withdrawButton;
    private JTextArea resultArea;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean loggedIn = false;

    public ClientMain() {
        // 初始化连接
        try {
            socket = new Socket("192.168.0.104", 2525);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建界面
        frame = new JFrame("ATM System");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 添加组件
        cardidField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        checkBalanceButton = new JButton("Check Balance");
        withdrawButton = new JButton("Withdraw Money");
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);

        JLabel cardIdLabel = new JLabel("Enter card id:");
        JLabel passwordLabel = new JLabel("Enter password:");

        // 设置字体和颜色
        cardIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkBalanceButton.setFont(new Font("Arial", Font.BOLD, 14));
        withdrawButton.setFont(new Font("Arial", Font.BOLD, 14));
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));

        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        checkBalanceButton.setBackground(new Color(70, 130, 180));
        checkBalanceButton.setForeground(Color.WHITE);
        withdrawButton.setBackground(new Color(70, 130, 180));
        withdrawButton.setForeground(Color.WHITE);

        // 添加组件到面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(cardIdLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(cardidField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(checkBalanceButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(withdrawButton, gbc);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // 添加事件监听器
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cardid = cardidField.getText();
                String password = new String(passwordField.getPassword());
                if (password.isEmpty()) {
                    resultArea.setText("Password cannot be empty. Please enter a password.");
                    return;
                }
                login(cardid, password);
            }
        });

        checkBalanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loggedIn) {
                    String cardid = cardidField.getText();
                    checkBalance(cardid);
                } else {
                    resultArea.setText("Please login first.");
                }
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loggedIn) {
                    String cardid = cardidField.getText();
                    String amountString = JOptionPane.showInputDialog(frame, "Enter the amount to withdraw:");
                    if (amountString != null) {
                        double amount = Double.parseDouble(amountString);
                        withdrawMoney(cardid, amount);
                    }
                } else {
                    resultArea.setText("Please login first.");
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendByeSignal();
            }
        });

        // 显示界面
        frame.setVisible(true);
    }

    private void login(String cardid, String password) {
        out.println("HELO " + cardid);
        try {
            String response = in.readLine();
            if ("500 AUTH REQUIRE".equals(response)) {
                out.println("PASS " + password);
                //out.println(cardid);
                //out.println(password);
                response = in.readLine();
                if ("525 OK!".equals(response)) {
                    loggedIn = true;
                    resultArea.setText("525 OK!");
                    enableButtons(true);
                } else {
                    resultArea.setText("401 ERROR!");
                }
            } else {
                resultArea.setText("401 ERROR:Server is not ready for login.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkBalance(String cardid) {
        out.println("BALA");
        //out.println(cardid);
        try {
            String balance = in.readLine();
            resultArea.setText("Your current balance is: " + balance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void withdrawMoney(String cardid, double amount) {
        out.println("WDRA " + amount);
       // out.println(cardid);
        //out.println(amount);
        try {
            String response = in.readLine();
            resultArea.setText(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendByeSignal() {
        out.println("BYE");
        try {
            String response = in.readLine();
            if ("BYE".equals(response)) {
                socket.close();
                frame.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableButtons(boolean enable) {
        checkBalanceButton.setEnabled(enable);
        withdrawButton.setEnabled(enable);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientMain();
            }
        });
    }
}
