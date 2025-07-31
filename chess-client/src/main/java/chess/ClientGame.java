package chess;

import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;

import org.kamatech.chess.Board;
import org.kamatech.chess.Command;
import org.kamatech.chess.Game;
import org.kamatech.chess.api.IGraphicsFactory;
import org.kamatech.chess.api.IPhysicsFactory;
import org.kamatech.chess.api.IPieceFactory;

/**
 * Client version of the game - new clean architecture
 * [Keyboard] → [Queue: Keyboard Input] → [Thread: Send to Server] → [Server]
 * [Game Loop] ← [Queue: Commands from Server] ← [Thread: Receive from Server] ← [Server validates & broadcasts]
 */
public class ClientGame extends Game {
    private final ChessClient client;
    private final Command.Player myPlayerColor;
    
    // Two separate queues for clean architecture
    private final BlockingQueue<Command> keyboardInputQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    private final BlockingQueue<Command> commandsFromServer;
    
    // Clean separation into dedicated classes
    private ClientCommandSender commandSender;
    private ClientCommandProcessor commandProcessor;
    private KeyboardCommandConverter keyboardConverter;
    
    // Gameplay state
    private volatile boolean gameplayEnabled = false;
    
    public ClientGame(Board board, IPieceFactory pieceFactory, 
                     IGraphicsFactory graphicsFactory, IPhysicsFactory physicsFactory,
                     ChessClient client, Command.Player playerColor, 
                     BlockingQueue<Command> commandsFromServer) {
        super(board, pieceFactory, graphicsFactory, physicsFactory);
        this.client = client;
        this.myPlayerColor = playerColor;
        this.commandsFromServer = commandsFromServer;
        
        // Initialize the clean architecture components
        this.commandSender = new ClientCommandSender(client, playerColor, keyboardInputQueue);
        this.commandProcessor = new ClientCommandProcessor(this, playerColor, commandsFromServer);
        this.keyboardConverter = new KeyboardCommandConverter(playerColor);
        
        // Set current player info in GraphicsFactory
        org.kamatech.chess.GraphicsFactory.setCurrentPlayerInfo(playerColor.toString());
    }
    
    /**
     * Starting the game on the client
     */
    @Override
    public void startGame() {
        // Set player info in GraphicsFactory
        org.kamatech.chess.GraphicsFactory.setCurrentPlayerInfo(getPlayerColorString());
        
        // Start the regular game with GUI
        super.startGame();
        
        // Start the clean architecture components but don't enable gameplay yet
        commandSender.start();
        commandProcessor.start();
        
        // Display waiting info on screen
        displayWaitingInfo();
    }
    
    /**
     * Display player info on screen
     */
    private void displayPlayerInfo() {
        String playerName = (myPlayerColor == Command.Player.WHITE) ? "White Player (WASD)" : "Black Player (Arrows)";
        System.out.println("=== " + playerName + " ===");
        
        if (myPlayerColor == Command.Player.WHITE) {
            System.out.println("Movement keys: WASD");
            System.out.println("Select piece: C (after navigating to piece)");
            System.out.println("Execute move: Space");
            System.out.println("How to play: WASD to navigate -> C to select -> WASD to move -> Space to execute");
        } else {
            System.out.println("Movement keys: Arrow keys");
            System.out.println("Select piece: V (after navigating to piece)");
            System.out.println("Execute move: Enter");
            System.out.println("How to play: Arrows to navigate -> V to select -> Arrows to move -> Enter to execute");
        }
        
        System.out.println("*** Commands are sent to server for approval and executed only if they are legal ***");
        System.out.println("*** NEW ARCHITECTURE: Keyboard → Queue → Thread → Server → Validate → Broadcast → Game ***");
    }
    
    /**
     * Display waiting info on screen
     */
    private void displayWaitingInfo() {
        String playerName = (myPlayerColor == Command.Player.WHITE) ? "White Player (WASD)" : "Black Player (Arrows)";
        System.out.println("=== " + playerName + " ===");
        System.out.println("BOARD DISPLAYED - Waiting for other player to connect...");
        System.out.println("You can see the board but cannot move pieces yet.");
        
        if (myPlayerColor == Command.Player.WHITE) {
            System.out.println("Your controls will be: WASD (movement), C (select), Space (execute)");
        } else {
            System.out.println("Your controls will be: Arrow Keys (movement), V (select), Enter (execute)");
        }
    }
    
    /**
     * Handle keyboard input - add to keyboard input queue
     */
    @Override
    public void handleRawKeyPressed(KeyEvent e) {
        // Check if gameplay is enabled
        if (!gameplayEnabled) {
            System.out.println("Gameplay not enabled yet - waiting for other player. Key ignored: " + e.getKeyCode());
            return;
        }
        
        // Convert keyboard input to command using the dedicated converter
        Command command = keyboardConverter.convertKeyEventToCommand(e);
        
        if (command != null && command.getPlayer() == myPlayerColor) {
            // Check if client is connected to server
            if (client != null && client.isConnected()) {
                // Server mode: Add command to sending queue - the command sender will handle it
                commandSender.queueCommand(command);
                System.out.println("🌐 Server mode: Sent command to server for validation: " + command.getRawCommand());
            } else {
                // Offline/Local mode: Execute directly without server validation
                System.out.println("🔧 Local mode: Server not connected - executing command directly");
                super.handleRawKeyPressed(e);
            }
        } else if (command != null) {
            System.out.println("Ignoring command for other player: " + command.getRawCommand());
        } else {
            // Not a converted command - pass to parent for processing
            super.handleRawKeyPressed(e);
        }
    }
    
    /**
     * Handle approved commands from server - bypass client-to-server logic
     * This method executes commands directly without sending them back to server
     */
    public void executeServerApprovedCommand(KeyEvent e) {
        // Call parent implementation directly - no server validation needed
        super.handleRawKeyPressed(e);
    }
    
    /**
     * Stop the game
     */
    @Override
    public void stopGame() {
        // Clear player info from GraphicsFactory
        org.kamatech.chess.GraphicsFactory.clearCurrentPlayerInfo();
        
        super.stopGame();
        
        // Stop the clean architecture components
        if (commandSender != null) {
            commandSender.stop();
        }
        
        if (commandProcessor != null) {
            commandProcessor.stop();
        }
    }
    
    /**
     * Get current player color
     */
    public String getPlayerColorString() {
        if (myPlayerColor == Command.Player.WHITE) {
            return "WHITE";
        } else if (myPlayerColor == Command.Player.BLACK) {
            return "BLACK";
        }
        return null;
    }
    
    /**
     * Enable gameplay when both players are connected
     */
    public void enableGameplay() {
        gameplayEnabled = true;
        System.out.println("Gameplay enabled for player: " + myPlayerColor);
        
        // Re-display player info to remind about controls
        displayPlayerInfo();
    }
    
    /**
     * Check if gameplay is enabled
     */
    public boolean isGameplayEnabled() {
        return gameplayEnabled;
    }
}
