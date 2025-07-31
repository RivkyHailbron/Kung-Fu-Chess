package chess.server;

import java.util.Map;

/**
 * Handles broadcasting messages to all clients
 * Clean separation: dedicated message broadcasting logic
 */
public class ServerMessageBroadcaster {
    
    private final Map<String, ClientHandler> clients;
    
    public ServerMessageBroadcaster(Map<String, ClientHandler> clients) {
        this.clients = clients;
    }
    
    /**
     * Broadcast message to all clients
     */
    public void broadcastMessage(String message) {
        broadcastMessage(message, null);
    }
    
    /**
     * Broadcast message to all clients except one
     */
    public void broadcastMessage(String message, String excludeClientId) {
        System.out.println("📢 [BROADCAST] " + message + 
                          (excludeClientId != null ? " (excluding " + excludeClientId + ")" : " (to all)"));
        
        int sentCount = 0;
        int totalClients = clients.size();
        
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            String clientId = entry.getKey();
            ClientHandler handler = entry.getValue();
            
            // Skip excluded client
            if (excludeClientId != null && clientId.equals(excludeClientId)) {
                continue;
            }
            
            // Send to client if handler is still active
            if (handler != null && handler.isRunning()) {
                try {
                    handler.sendMessage(message);
                    sentCount++;
                } catch (Exception e) {
                    System.err.println("Failed to send message to " + clientId + ": " + e.getMessage());
                }
            }
        }
        
        System.out.println("✓ [BROADCAST RESULT] Sent to " + sentCount + "/" + totalClients + " clients");
    }
    
    /**
     * Send message to specific client
     */
    public boolean sendToClient(String clientId, String message) {
        ClientHandler handler = clients.get(clientId);
        
        if (handler != null && handler.isRunning()) {
            try {
                handler.sendMessage(message);
                System.out.println("✓ [DIRECT MESSAGE] Sent to " + clientId + ": " + message);
                return true;
            } catch (Exception e) {
                System.err.println("Failed to send direct message to " + clientId + ": " + e.getMessage());
            }
        } else {
            System.err.println("Client " + clientId + " not found or not active");
        }
        
        return false;
    }
    
    /**
     * Get count of active clients
     */
    public int getActiveClientCount() {
        int count = 0;
        for (ClientHandler handler : clients.values()) {
            if (handler != null && handler.isRunning()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get total client count (including inactive)
     */
    public int getTotalClientCount() {
        return clients.size();
    }
}
