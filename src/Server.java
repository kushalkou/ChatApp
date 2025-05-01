import java.io.*;
import java.net.*;
import java.util.*;



public class Server{

    static List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args){
        try{
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started. Waiting for clients...");

            while(true){

                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start();

            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread{

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try{
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                this.username = in.readLine();
                System.out.println(username + " has joined the chat.");

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String msg;

                while((msg = in.readLine()) != null){
                    System.out.println("Recieved from " + username + ": " + msg);

                    synchronized (clientWriters){

                        for (PrintWriter writer : clientWriters){
                            if(writer != out)
                                writer.println("[" + username + "]: " + msg);
                        }
                    }
                }
            
            }
            catch(IOException e){
                e.printStackTrace();
            } finally {

                try{
                    socket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }

                synchronized (clientWriters){
                    clientWriters.remove(out);
                    if (out != null) {
                        clientWriters.remove(out);
                    }
                }
                System.out.println(username + " has disconnected.");

            }
        }
    }
}


