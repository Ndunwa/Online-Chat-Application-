import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to broadcast message to all clients
    static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != excludeUser) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // Method to remove a client handler from the set
    static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a unique username from the client
                out.println("Enter your username:");
                userName = in.readLine();

                System.out.println(userName + " has connected.");

                // Notify all users about the new connection
                broadcast(userName + " has joined the chat.", this);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                    System.out.println("Received from " + userName + ": " + message);
                    broadcast(userName + ": " + message, this);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                removeClient(this);
                broadcast(userName + " has left the chat.", this);
                System.out.println(userName + " has disconnected.");
            }
        }

        // Method to send message to this client
        void sendMessage(String message) {
            out.println(message);
        }
    }
}
