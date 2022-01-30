package tech.fallqvist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String userLoggedIn;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String input;
            while ((input = in.readLine()) != null) {
                String[] tokens = input.split(" ");
                if (tokens.length > 0) {
                    String cmd = tokens[0];
                    if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("logoff")) {
                        System.out.println("Connection closed...");
                        break;
                    } else if (cmd.equalsIgnoreCase("login")) {
                        handleLogin(out, tokens);
                    } else {
                        out.println("Unknown " + cmd + "\n");
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

    private void handleLogin(PrintWriter out, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String userName = tokens[1];
            String password = tokens[2];

            if ((userName.equalsIgnoreCase("guest") &&
                    password.equalsIgnoreCase("guest")) ||
                    (userName.equalsIgnoreCase("patrik") &&
                    password.equalsIgnoreCase("patrik"))) {
                out.println("Login OK!\n");
                this.userLoggedIn = userName;

                out.println(userName + " is Online!");
                System.out.println("User " + userName + " successfully logged in!");
            } else {
                out.println("Login ERROR!\n");
            }
        }
    }

    public String getUserLoggedIn() {
        return userLoggedIn;
    }

    public void setUserLoggedIn(String userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }
}
