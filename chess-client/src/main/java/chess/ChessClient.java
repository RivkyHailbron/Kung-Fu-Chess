package chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;

import org.kamatech.chess.Command;

public class ChessClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean connected = false;
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private ClientGame game;
    private volatile org.kamatech.chess.Command.Player assignedPlayerColor = null;
    private JDialog waitingDialog = null;
    
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 8080;
        
        if (args.length > 0) {
            String[] parts = args[0].split(":");
            serverAddress = parts[0];
            if (parts.length > 1) {
                try {
                    serverPort = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port: " + parts[1]);
                    System.exit(1);
                }
            }
        }
        
        ChessClient client = new ChessClient();
        client.start(serverAddress, serverPort);
    }
    
    public void start(String serverAddress, int serverPort) {
        try {
            // Connect to server
            System.out.println("Connecting to " + serverAddress + ":" + serverPort + "...");
            connect(serverAddress, serverPort);
            
            // Run message reading loop from server
            // Game will start only after receiving player role
            handleServerMessages();
            
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
    
    public void connect(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        connected = true;
        
        System.out.println("Connected to server successfully!");
    }
    
    private void initializeAndShowGame() {
        if (assignedPlayerColor == null) {
            System.err.println("Cannot initialize game - no player color assigned");
            return;
        }

        try {
            // Create the game but don't start it
            System.out.println("Creating board image...");
            org.kamatech.chess.Img boardImg = new org.kamatech.chess.Img();

            // Try multiple ways to load the board image
            boolean boardLoaded = false;
            
            // Method 1: Try from resources
            try {
                java.net.URL boardPath = getClass().getClassLoader().getResource("board.png");
                if (boardPath != null) {
                    System.out.println("Found board.png at: " + boardPath);
                    // Convert URL to proper file path for Img.read()
                    java.io.File boardFile = new java.io.File(boardPath.toURI());
                    boardImg.read(boardFile.getAbsolutePath(), new java.awt.Dimension(800, 800), true, null);
                    boardLoaded = true;
                    System.out.println("Successfully loaded board image from resources");
                }
            } catch (Exception e) {
                System.out.println("Method 1 failed: " + e.getMessage());
            }
            
            // Method 2: Try direct resource stream if method 1 failed
            if (!boardLoaded) {
                try {
                    java.io.InputStream boardStream = getClass().getClassLoader().getResourceAsStream("board.png");
                    if (boardStream != null) {
                        java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(boardStream);
                        if (bufferedImage != null) {
                            // Resize to 800x800
                            java.awt.image.BufferedImage resizedImage = new java.awt.image.BufferedImage(800, 800, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                            java.awt.Graphics2D g2d = resizedImage.createGraphics();
                            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g2d.drawImage(bufferedImage, 0, 0, 800, 800, null);
                            g2d.dispose();
                            
                            boardImg.setImage(resizedImage);
                            boardLoaded = true;
                            System.out.println("Successfully loaded board image from stream");
                        }
                        boardStream.close();
                    }
                } catch (Exception e) {
                    System.out.println("Method 2 failed: " + e.getMessage());
                }
            }
            
            if (!boardLoaded) {
                System.out.println("Could not load board.png, creating default chess board...");
                // Create a default chess board pattern
                java.awt.image.BufferedImage defaultBoard = new java.awt.image.BufferedImage(800, 800, java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2d = defaultBoard.createGraphics();
                
                // Light squares
                g2d.setColor(new java.awt.Color(240, 217, 181));
                g2d.fillRect(0, 0, 800, 800);
                
                // Dark squares
                g2d.setColor(new java.awt.Color(181, 136, 99));
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if ((row + col) % 2 == 1) {
                            g2d.fillRect(col * 100, row * 100, 100, 100);
                        }
                    }
                }
                g2d.dispose();
                
                boardImg.setImage(defaultBoard);
                System.out.println("Created default chess board pattern");
            }

            System.out.println("Creating board object...");
            org.kamatech.chess.Board board = new org.kamatech.chess.Board(100, 100, 1, 1, 8, 8, boardImg);
            org.kamatech.chess.api.IGraphicsFactory graphicsFactory = new org.kamatech.chess.GraphicsFactory();
            org.kamatech.chess.api.IPhysicsFactory physicsFactory = new org.kamatech.chess.PhysicsFactory();
            org.kamatech.chess.api.IPieceFactory pieceFactory = new org.kamatech.chess.PieceFactory(graphicsFactory, physicsFactory);

            game = new ClientGame(board, pieceFactory, graphicsFactory, physicsFactory, this, assignedPlayerColor, commandQueue);

            // Start the game immediately to show the board
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("Starting GUI for player: " + assignedPlayerColor);
                    game.startGame();
                    System.out.println("Game board displayed - waiting for other player...");
                } catch (Exception e) {
                    System.err.println("Game error: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("Error initializing game: " + e.getMessage());
            e.printStackTrace();
        }
    }    private void startGameLogicAfterBothPlayersConnected() {
        if (game == null) {
            System.err.println("Cannot start game logic - game not initialized");
            return;
        }

        // Start the game logic components in Swing Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Starting game logic for player: " + assignedPlayerColor);
                
                // Start the command processing components if they exist
                if (game instanceof ClientGame) {
                    ClientGame clientGame = (ClientGame) game;
                    // Enable the game logic (this method should be added to ClientGame)
                    clientGame.enableGameplay();
                }
                
                System.out.println("Game logic started - gameplay enabled!");
            } catch (Exception e) {
                System.err.println("Game logic error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void handleServerMessages() {
        try {
            String message;
            while (connected && (message = reader.readLine()) != null) {
                System.out.println("Received from server: " + message);
                
                // Process special messages
                if (message.equals("GAME_START")) {
                    System.out.println("Both players connected - Game starting!");
                    // Now we can actually start the game logic
                    if (game != null) {
                        startGameLogicAfterBothPlayersConnected();
                    } else {
                        System.err.println("Cannot start game - game not initialized");
                    }
                    continue;
                } else if (message.equals("PLAYER_DISCONNECTED")) {
                    System.out.println("Other player disconnected!");
                    continue;
                } else if (message.startsWith("INVALID_COMMAND:")) {
                    System.out.println("Server rejected command: " + message.substring("INVALID_COMMAND:".length()));
                    continue;
                } else if (message.startsWith("PLAYER_ROLE:")) {
                    String role = message.substring("PLAYER_ROLE:".length());
                    System.out.println("Assigned player role: " + role);
                    
                    // Set color according to role from server
                    if (role.equals("WHITE")) {
                        assignedPlayerColor = org.kamatech.chess.Command.Player.WHITE;
                    } else if (role.equals("BLACK")) {
                        assignedPlayerColor = org.kamatech.chess.Command.Player.BLACK;
                    }
                    
                    // Create and start the game immediately to show the board
                    initializeAndShowGame();
                    continue;
                } else if (message.startsWith("WAITING_FOR_PLAYER:")) {
                    String waitingMessage = message.substring("WAITING_FOR_PLAYER:".length());
                    System.out.println("Server says: " + waitingMessage);
                    showWaitingMessage(waitingMessage);
                    continue;
                } else if (message.equals("CLEAR_WAITING")) {
                    System.out.println("Clearing waiting message...");
                    hideWaitingMessage();
                    continue;
                } else if (message.equals("PLAYER_DISCONNECTED")) {
                    System.out.println("Other player disconnected");
                    showWaitingMessage("השחקן השני התנתק<br>ממתין לשחקן חדש...");
                    continue;
                }
                
                // This is an approved command from server - convert message to command and add to queue
                try {
                    Command command = new Command(message);
                    commandQueue.offer(command);
                    System.out.println("Added approved command to queue: " + message);
                } catch (Exception e) {
                    System.err.println("Error parsing approved server command: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Lost connection to server: " + e.getMessage());
            }
        } finally {
            connected = false;
        }
    }
    
    public void sendCommandToServer(String command) {
        if (connected && writer != null) {
            writer.println(command);
            System.out.println("Sent to server: " + command);
        } else {
            System.err.println("Not connected to server!");
        }
    }
    
    public void sendCommandToServer(org.kamatech.chess.Command command) {
        if (command != null) {
            sendCommandToServer(command.toString());
        }
    }
    
    public void sendCommand(Command command) {
        if (command != null) {
            sendCommandToServer(command.toString());
        }
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    public void disconnect() {
        connected = false;
        
        try {
            if (reader != null) reader.close();
        } catch (IOException e) {
            System.err.println("Error closing reader: " + e.getMessage());
        }
        
        try {
            if (writer != null) writer.close();
        } catch (Exception e) {
            System.err.println("Error closing writer: " + e.getMessage());
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        
        // Close waiting dialog if open
        hideWaitingMessage();
        
        System.out.println("Disconnected from server");
    }
    
    /**
     * Show waiting message dialog
     */
    private void showWaitingMessage(String message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (waitingDialog != null) {
                hideWaitingMessage();
            }
            
            JFrame parentFrame = null;
            if (game != null) {
                parentFrame = game.getFrame();
            }
            
            waitingDialog = new JDialog(parentFrame, "ממתין לשחקן", true);
            waitingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            JLabel label = new JLabel("<html><div style='text-align: center;'>" + 
                                    message + "<br><br>אנא המתן...</div></html>");
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setForeground(new Color(0, 100, 200));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            waitingDialog.add(label);
            waitingDialog.setSize(new Dimension(300, 150));
            waitingDialog.setLocationRelativeTo(parentFrame);
            waitingDialog.setResizable(false);
            
            System.out.println("Showing waiting dialog: " + message);
            waitingDialog.setVisible(true);
        });
    }
    
    /**
     * Hide waiting message dialog
     */
    private void hideWaitingMessage() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (waitingDialog != null) {
                System.out.println("Hiding waiting dialog");
                waitingDialog.setVisible(false);
                waitingDialog.dispose();
                waitingDialog = null;
            }
        });
    }
}
