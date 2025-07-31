package chess.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChessServer {
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private ServerChessLogic gameLogic;
    private ServerMessageBroadcaster broadcaster;
    private final ServerConfig config;
    
    // Game state tracking
    private boolean gameStarted = false;
    private boolean whitePlayerConnected = false;
    private boolean blackPlayerConnected = false;
    private final java.util.List<String> gameMoves = new java.util.ArrayList<>();
    
    public ChessServer() {
        this.config = ServerConfig.getInstance();
    }
    
    public static void main(String[] args) {
        ServerConfig config = ServerConfig.getInstance();
        int port = config.getPort();
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.exit(1);
            }
        }
        
        ChessServer server = new ChessServer();
        server.start(port);
    }
    
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            System.out.println("Chess Server started on port " + port);
            
            // Initialize minimal game logic - no graphics needed!
            gameLogic = new ServerChessLogic();
            System.out.println("Server chess logic initialized");
            
            // Initialize message broadcaster
            broadcaster = new ServerMessageBroadcaster(clients);
            System.out.println("Message broadcaster initialized");
            
            // Send game started signal
            ServerCommand gameStartCommand = new ServerCommand("GAME_CONTROL:GAME_STARTED");
            gameLogic.processValidCommand(gameStartCommand);
            
            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewConnection(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server startup error: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    private void handleNewConnection(Socket clientSocket) {
        String clientId = "Client-" + clientSocket.getRemoteSocketAddress().toString();
        System.out.println("New client connected: " + clientId);
        
        if (clients.size() >= config.getMaxClients()) {
            try {
                clientSocket.close();
                System.out.println("Rejected client (max " + config.getMaxPlayers() + " players): " + clientId);
            } catch (IOException e) {
                System.err.println("Error closing excess client: " + e.getMessage());
            }
            return;
        }
        
        try {
            ClientHandler handler = new ClientHandler(clientSocket, clientId, this);
            
            Thread clientThread = new Thread(handler);
            clientThread.start();
            
            // Wait a bit for the connection to be established
            Thread.sleep(config.getConnectionSetupDelayMs());
            
            clients.put(clientId, handler);
            
            // Determine player role based on game state
            String playerRole;
            if (!gameStarted) {
                // First game - assign based on connection order
                playerRole = (clients.size() == 1) ? config.getPlayer1Color() : config.getPlayer2Color();
                if ("WHITE".equals(playerRole)) {
                    whitePlayerConnected = true;
                } else {
                    blackPlayerConnected = true;
                }
            } else {
                // Game already started - assign missing color
                if (!whitePlayerConnected) {
                    playerRole = "WHITE";
                    whitePlayerConnected = true;
                } else if (!blackPlayerConnected) {
                    playerRole = "BLACK";
                    blackPlayerConnected = true;
                } else {
                    // Both connected - shouldn't happen
                    playerRole = "SPECTATOR";
                }
            }
            
            handler.sendMessage("PLAYER_ROLE:" + playerRole);
            handler.setPlayerRole(playerRole); // Store role in handler
            
            System.out.println("Client connected successfully: " + clientId + " as " + playerRole + " (Total: " + clients.size() + "/" + config.getMaxPlayers() + ")");
            System.out.println("Game status: Started=" + gameStarted + ", WHITE=" + whitePlayerConnected + ", BLACK=" + blackPlayerConnected);
            
            // Send appropriate message based on game state
            if (!gameStarted && clients.size() == 1) {
                // First game, first player - show waiting message
                handler.sendMessage("WAITING_FOR_PLAYER:" + config.getWaitingMessage());
                System.out.println("First player connected. Waiting for second player...");
            } else if (!gameStarted && clients.size() == config.getMaxPlayers()) {
                // First game, both players connected - start the game
                
                // Wait a bit more for player roles to be processed
                Thread.sleep(config.getConnectionStartDelayMs());
                
                // Clear waiting message for all players and start game
                broadcastMessage("CLEAR_WAITING");
                Thread.sleep(config.getConnectionClearDelayMs());
                broadcastMessage("GAME_START");
                gameStarted = true;
                System.out.println("Both players connected. Game started!");
            } else if (gameStarted && whitePlayerConnected && blackPlayerConnected) {
                // Game already started, player reconnected - send current game state
                System.out.println("Player reconnected. Sending current game state...");
                
                // Send all previous moves to sync the reconnected player
                for (String move : gameMoves) {
                    handler.sendMessage(move);
                    Thread.sleep(50); // Small delay between moves for processing
                }
                
                // Send game start signal to ensure UI is ready
                handler.sendMessage("GAME_START");
                System.out.println("Game state synchronized for reconnected player: " + playerRole);
            } else if (gameStarted && clients.size() == 1) {
                // Game started but only one player - show waiting message
                handler.sendMessage("WAITING_FOR_PLAYER:" + config.getWaitingMessage());
                System.out.println("Player reconnected. Waiting for other player...");
            }
            
        } catch (Exception e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }
    
    public void handleClientMessage(String clientId, String message) {
        System.out.println("Received from " + clientId + ": " + message);
        
        try {
            // Parse command from client
            ServerCommand command = parseCommand(message);
            if (command != null) {
                System.out.println("Processing command from " + clientId + ": " + message);
                
                // Validate command using minimal logic
                boolean isValid = gameLogic.isValidCommand(command);
                
                if (isValid) {
                    // Process the valid command
                    gameLogic.processValidCommand(command);
                    
                    // Save the move for game state synchronization (only after game started)
                    if (gameStarted && !message.contains("GAME_CONTROL")) {
                        gameMoves.add(message);
                        System.out.println("Move saved to game state: " + message + " (Total moves: " + gameMoves.size() + ")");
                    }
                    
                    // Command is valid - send original command back to all clients
                    System.out.println("✓ [SERVER BROADCAST] Command valid, broadcasting approved command to all clients");
                    broadcastMessage(message); // Send original command, not board state
                } else {
                    System.out.println("Command invalid, sending rejection to sender");
                    ClientHandler sender = clients.get(clientId);
                    if (sender != null) {
                        sender.sendMessage("COMMAND_REJECTED:" + message);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message from " + clientId + ": " + e.getMessage());
        }
    }
    
    private ServerCommand parseCommand(String message) {
        try {
            // Simply create command from string
            ServerCommand command = new ServerCommand(message);
            return command;
        } catch (Exception e) {
            System.err.println("Error parsing command: " + message + " - " + e.getMessage());
        }
        return null;
    }
    
    public void broadcastMessage(String message) {
        if (broadcaster != null) {
            broadcaster.broadcastMessage(message);
        }
    }
    
    public void broadcastMessage(String message, String excludeClientId) {
        if (broadcaster != null) {
            broadcaster.broadcastMessage(message, excludeClientId);
        }
    }
    
    public void removeClient(String clientId) {
        // Track which color disconnected
        ClientHandler handler = clients.get(clientId);
        if (handler != null) {
            String disconnectedRole = handler.getPlayerRole();
            if ("WHITE".equals(disconnectedRole)) {
                whitePlayerConnected = false;
                System.out.println("WHITE player disconnected");
            } else if ("BLACK".equals(disconnectedRole)) {
                blackPlayerConnected = false;
                System.out.println("BLACK player disconnected");
            }
        }
        
        clients.remove(clientId);
        System.out.println("Client disconnected: " + clientId + " (Remaining: " + clients.size() + "/" + config.getMaxPlayers() + ")");
        System.out.println("Color status: WHITE=" + whitePlayerConnected + ", BLACK=" + blackPlayerConnected);
        
        if (gameStarted && clients.size() == 1) {
            // Game started but one player disconnected - show waiting message
            broadcastMessage("WAITING_FOR_PLAYER:" + config.getWaitingMessage());
            System.out.println("Player disconnected during game. Remaining player waiting for reconnection...");
        } else if (clients.size() == 0) {
            // All players disconnected
            broadcastMessage("PLAYER_DISCONNECTED");
            System.out.println("All players disconnected.");
        }
    }
    
    public void stop() {
        running = false;
        
        // Close all client connections
        for (ClientHandler client : clients.values()) {
            client.close();
        }
        clients.clear();
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        
        System.out.println("Chess Server stopped");
    }
}
