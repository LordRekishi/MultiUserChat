package tech.fallqvist;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private ExecutorService executorService;
    private List<ClientHandler> clients;
    private Set<String> topicsServer;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        topicsServer = new HashSet<>();
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

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public Set<String> getTopicsServer() {
        return topicsServer;
    }

    public void setTopicsServer(Set<String> topicsServer) {
        this.topicsServer = topicsServer;
    }

    public void addTopicToServer(String topic) {
        topicsServer.add(topic);
    }

    public void removeTopicFromServer(String topic) {
        topicsServer.remove(topic);
    }

    public static void main(String[] args) {
        Server server = new Server(8080);
        server.start();
    }
}
