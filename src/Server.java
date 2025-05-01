import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static List<PrintWriter> clientWriters = new ArrayList<>();
    static Map<String, PrintWriter> userWriters = new HashMap<>();
    static Map<String, Set<String>> groupMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                this.username = in.readLine();
                System.out.println(username + " has joined the chat.");

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                synchronized (userWriters) {
                    userWriters.put(username, out);
                }

                String msg;

                while ((msg = in.readLine()) != null) {
                    System.out.println("Received from " + username + ": " + msg);

                    if (msg.startsWith("/create_group")) {
                        String[] tokens = msg.split("\\s+");

                        if (tokens.length < 3) {
                            out.println("Usage: /create_group <group_name> <user1> <user2> ...");
                            continue;
                        }

                        String groupName = tokens[1];
                        Set<String> groupMembers = new HashSet<>();

                        groupMembers.add(username);
                        for (int i = 2; i < tokens.length; i++) {
                            groupMembers.add(tokens[i]);
                        }

                        synchronized (groupMap) {
                            groupMap.put(groupName, groupMembers);
                        }

                        out.println("Group '" + groupName + "' created and members added.");
                        continue;
                    }

                    if (msg.startsWith("@")) {
                        int spaceIndex = msg.indexOf(' ');
                        if (spaceIndex != -1) {
                            String target = msg.substring(1, spaceIndex);
                            String message = msg.substring(spaceIndex + 1);

                            synchronized (userWriters) {
                                if (userWriters.containsKey(target)) {
                                    PrintWriter targetOut = userWriters.get(target);
                                    targetOut.println("[Private from " + username + "]: " + message);
                                    out.println("[Private to " + target + "]: " + message);
                                    continue;
                                }
                            }

                            synchronized (groupMap) {
                                if (groupMap.containsKey(target)) {
                                    Set<String> members = groupMap.get(target);

                                    if (!members.contains(username)) {
                                        out.println("You are not a member of group '" + target + "'.");
                                        continue;
                                    }
                                    
                                    for (String member : members) {
                                        if (!member.equals(username)) {
                                            PrintWriter writer = userWriters.get(member);
                                            if (writer != null) {
                                                writer.println("[" + target + "] " + username + ": " + message);
                                            }
                                        }
                                    }
                                    continue;
                                }
                            }

                            out.println("No user or group named '" + target + "' found.");
                            continue;
                        } else {
                            out.println("Invalid format. Use: @name message");
                            continue;
                        }
                    }

                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            if (writer != out) {
                                writer.println("[" + username + "]: " + msg);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }

                synchronized (userWriters) {
                    userWriters.remove(username);
                }

                System.out.println(username + " has disconnected.");
            }
        }
    }
}
