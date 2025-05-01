import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        try {

            Socket socket = new Socket("localhost", 1234);
            System.out.println("Connected to server!");

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Enter your username: ");
            String username = keyboard.readLine();
            out.println(username);

            new Thread(() -> {
                String serverMsg;
                try {
                    while ((serverMsg = serverIn.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            String msg;
            while ((msg = keyboard.readLine()) != null) {
                out.println(msg);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
