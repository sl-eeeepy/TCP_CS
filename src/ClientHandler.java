import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    String card_id;

    double amount;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("BYE")) {
                    out.println("BYE");
                    break;
                }

                if (inputLine.substring(0,4).equals("HELO")) {
                    //判断账号是否在数据库
                    card_id = inputLine.substring(5);
                    if(DBHandler.checkid(card_id))
                        out.println("500 AUTH REQUIRED");
                } else if (inputLine.substring(0,4).equals("PASS")) {
                    //String cardid = in.readLine();
                    //String password = in.readLine();
                    String password = inputLine.substring(5);
                    System.out.println(card_id+password);
                    if (DBHandler.checkCredentials(card_id, password)) {
                        out.println("525 OK!");
                    } else {
                        out.println("401 ERROR!");
                    }
                } else if (inputLine.equals("BALA")) {
                    //String cardid = in.readLine();
                    double balance = DBHandler.getBalance(card_id);
                    out.println("AMNT:" + balance);
                } else if (inputLine.substring(0,4).equals("WDRA")) {
                    //amuount = Double.parseInterger(amuount.substring(5));
                     amount = Double.parseDouble(inputLine.substring(5));
                    String response = DBHandler.withdrawMoney(card_id, amount);
                    out.println(response);
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
