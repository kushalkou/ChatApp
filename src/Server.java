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

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverSocket.close();
                    System.out.println("Server socket closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

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

                    if (msg.startsWith("/")) {
                        String[] tokens = msg.split("\\s+", 2);
                        String command = tokens[0];

                        if (command.equals("/create_group")) {
                            if (tokens.length < 2) {
                                out.println("Usage: /create_group <group_name> <member1> <member2> ...");
                                continue;
                            }

                            String[] groupTokens = tokens[1].split("\\s+");
                            String groupName = groupTokens[0];

                            synchronized (groupMap) {
                                if (groupMap.containsKey(groupName)) {
                                    out.println("Group '" + groupName + "' already exists.");
                                } else {
                                    Set<String> members = new HashSet<>(Arrays.asList(groupTokens));
                                    members.add(username);
                                    groupMap.put(groupName, members);
                                    out.println("Group '" + groupName + "' created and you were added!");
                                }
                            }
                        }

                        else if (command.equals("/join_group")) {
                            if (tokens.length < 2) {
                                out.println("Usage: /join_group <group_name>");
                                continue;
                            }

                            String groupName = tokens[1];
                            synchronized (groupMap) {
                                if (groupMap.containsKey(groupName)) {
                                    groupMap.get(groupName).add(username);
                                    out.println("You joined group: " + groupName);
                                } else {
                                    out.println("Group '" + groupName + "' does not exist.");
                                }
                            }
                        }

                        else if (command.equals("/online")) {
                            synchronized (userWriters) {
                                out.println("Online users:");
                                for (String user : userWriters.keySet()) {
                                    out.println("- " + user);
                                }
                            }
                        }

                        else if (command.equals("/groups")) {
                            synchronized (groupMap) {
                                out.println("Groups you are a part of:");
                                for (String groupName : groupMap.keySet()) {
                                    Set<String> members = groupMap.get(groupName);
                                    if (members.contains(username)) {
                                        out.println("- " + groupName);
                                    }
                                }
                            }
                        }

                        else {
                            out.println("Unknown command.");
                        }
                    }

                    else if (msg.startsWith("@")) {
                        int spaceIndex = msg.indexOf(' ');
                        if (spaceIndex != -1) {
                            String target = msg.substring(1, spaceIndex);
                            String message = msg.substring(spaceIndex + 1);

                            boolean isGroup;
                            synchronized (groupMap) {
                                isGroup = groupMap.containsKey(target);
                            }

                            if (isGroup) {
                                synchronized (groupMap) {
                                    Set<String> members = groupMap.get(target);
                                    if (members.contains(username)) {
                                        for (String member : members) {
                                            if (!member.equals(username)) {
                                                PrintWriter writer = userWriters.get(member);
                                                if (writer != null) {
                                                    writer.println("[" + target + "] " + username + ": " + message);
                                                }
                                            }
                                        }
                                    } else {
                                        out.println("You are not a member of group '" + target + "'.");
                                    }
                                }
                            } else {
                                PrintWriter targetOut;
                                synchronized (userWriters) {
                                    targetOut = userWriters.get(target);
                                }

                                if (targetOut != null) {
                                    targetOut.println("[Private from " + username + "]: " + message);
                                    out.println("[Private to " + target + "]: " + message);
                                } else {
                                    out.println("User or group '" + target + "' not found.");
                                }
                            }
                        } else {
                            out.println("Invalid @ format. Use: @username message OR @groupname message");
                        }
                    }

                    else {
                        synchronized (userWriters) {
                            for (Map.Entry<String, PrintWriter> entry : userWriters.entrySet()) {
                                if (!entry.getKey().equals(username)) {
                                    entry.getValue().println("[" + username + "]: " + msg);
                                }
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
