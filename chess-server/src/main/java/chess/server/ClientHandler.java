package chess.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles individual client connections
 * Clean separation: one client per handler
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final String clientId;
    private final ChessServer server;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private volatile boolean running = true;
    private String playerRole = null;
    
    public ClientHandler(Socket socket, String clientId, ChessServer server) throws IOException {
        this.socket = socket;
        this.clientId = clientId;
        this.server = server;
        
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        
        System.out.println("✓ ClientHandler created for: " + clientId);
    }
    
    @Override
    public void run() {
        System.out.println("ClientHandler started for: " + clientId);
        
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                // Forward message to server for processing
                server.handleClientMessage(clientId, message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Client communication error (" + clientId + "): " + e.getMessage());
            }
        } finally {
            close();
            server.removeClient(clientId);
            System.out.println("ClientHandler stopped for: " + clientId);
        }
    }
    
    /**
     * Send message to this client
     */
    public void sendMessage(String message) {
        if (writer != null && !socket.isClosed()) {
            writer.println(message);
            System.out.println("→ [TO " + clientId + "] " + message);
        }
    }
    
    /**
     * Check if handler is still running
     */
    public boolean isRunning() {
        return running && !socket.isClosed();
    }
    
    /**
     * Get client ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Close all resources
     */
    public void close() {
        running = false;
        
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing reader for " + clientId + ": " + e.getMessage());
        }
        
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing writer for " + clientId + ": " + e.getMessage());
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for " + clientId + ": " + e.getMessage());
        }
        
        System.out.println("✓ ClientHandler resources closed for: " + clientId);
    }
    
    /**
     * Set the player role for this client
     */
    public void setPlayerRole(String role) {
        this.playerRole = role;
    }
    
    /**
     * Get the player role for this client
     */
    public String getPlayerRole() {
        return playerRole;
    }
}
