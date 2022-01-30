package tech.fallqvist;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private ExecutorService executorService;
    private List<ClientHandler> clients = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        executorService = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            acceptConnections(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptConnections(ServerSocket serverSocket) {
        while (true) {
            try {
                System.out.println("Waiting for client connection...");
                ClientHandler client = new ClientHandler(this, serverSocket.accept());
                executorService.submit(client);
                System.out.println("Accepted connection...");
                clients.add(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void setClients(List<ClientHandler> clients) {
        this.clients = clients;
    }

    public static void main(String[] args) {
     Server server = new Server(8080);
     server.start();
    }
}
