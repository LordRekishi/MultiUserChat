package tech.fallqvist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String userLoggedIn;
    private Set<String> topicsClient;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.clientSocket = socket;
        this.topicsClient = new HashSet<>();
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            send("Welcome, please Login:\n");

            String input;
            while ((input = in.readLine()) != null) {
                String[] tokens = input.split(" ", 3);
                if (tokens.length > 0) {
                    String cmd = tokens[0];
                    if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("logoff")) {
                        handleLogoff();
                        System.out.println("Connection closed...");
                        break;
                    } else if (cmd.equalsIgnoreCase("login")) {
                        handleLogin(tokens);
                    } else if (cmd.equalsIgnoreCase("msg")) {
                        handleMessage(tokens);
                    } else if (cmd.equalsIgnoreCase("join")) {
                        handleJoin(tokens);
                    } else if (cmd.equalsIgnoreCase("leave")) {
                        handleLeave(tokens);
                    } else if (cmd.equalsIgnoreCase("remove")) {
                        handleRemove(tokens);
                    } else {
                        send("Unknown " + cmd + "\n");
                    }
                }
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogoff() throws IOException {
        server.removeClient(this);
        for (ClientHandler client : server.getClients()) {
            if (!client.getUserLoggedIn().equalsIgnoreCase(userLoggedIn)) {
                client.send(userLoggedIn + " is Offline!\n");
            }
        }

        in.close();
        out.close();
        clientSocket.close();
    }

    private void handleLogin(String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String userName = tokens[1];
            String password = tokens[2];

            this.userLoggedIn = userName;

            if ((userName.equalsIgnoreCase("guest") &&
                    password.equalsIgnoreCase("guest")) ||
                    (userName.equalsIgnoreCase("patrik") &&
                            password.equalsIgnoreCase("patrik"))) {

                send("Login OK!\n");

                for (ClientHandler client : server.getClients()) {
                    if (!client.getUserLoggedIn().equalsIgnoreCase(userLoggedIn)) {
                        client.send(userLoggedIn + " is Online!\n");
                    }
                }
                for (ClientHandler client : server.getClients()) {
                    if (!client.getUserLoggedIn().equalsIgnoreCase(userLoggedIn)) {
                        send(client.getUserLoggedIn() + " is Online!\n");
                    }
                }
            } else {
                send("Login ERROR!\n");
            }
        }
    }

    private void handleMessage(String[] tokens) {
        if (tokens.length == 3) {
            String sendTo = tokens[1];
            String message = tokens[2];

            for (ClientHandler client : server.getClients()) {
                if (sendTo.startsWith("#")) {
                    if (client.isMemberOfTopic(sendTo) && !client.equals(this)) {
                        client.send("msg " + sendTo + ":" + userLoggedIn + " " + message);
                    }

                } else {
                    if (sendTo.equalsIgnoreCase(client.userLoggedIn)) {
                        client.send("msg " + userLoggedIn + " " + message);
                    }
                }
            }
        } else {
            send("No message specified, try again...\n");
        }
    }

    private boolean isMemberOfTopic(String topic) {
        return topicsClient.contains(topic);
    }

    private void send(String message) {
        out.println(message);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicsClient.add(topic);
            if (!server.getTopicsServer().contains(topic)) {
                server.addTopicToServer(topic);
            }
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicsClient.remove(topic);
        }
    }

    private void handleRemove(String[] tokens) throws IOException {
        if (tokens.length > 1) {
            send("\nAre you sure you want to remove topic? (Y/N)");
            if (in.readLine().equalsIgnoreCase("y")) {
                String topic = tokens[1];
                for (ClientHandler client : server.getClients()) {
                    if (client.getTopicsClient().contains(topic)) {
                        client.removeTopicFromClient(topic);
                    }
                }
                server.removeTopicFromServer(topic);
                send(topic + " has been removed!\n");
            } else {
                send("\nCancelling removing topic!\n");
            }
        }
    }

    public String getUserLoggedIn() {
        return userLoggedIn;
    }

    public void setUserLoggedIn(String userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }

    public Set<String> getTopicsClient() {
        return topicsClient;
    }

    public void setTopicsClient(Set<String> topicsClient) {
        this.topicsClient = topicsClient;
    }

    public void removeTopicFromClient(String topic) {
        topicsClient.remove(topic);
    }
}
