package chess;

import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;

import org.kamatech.chess.Command;
import org.kamatech.chess.Game;

/**
 * Handles processing approved commands from server
 * Clean separation: Server → Queue → Game Execution
 */
public class ClientCommandProcessor {
    private final Game game;
    private final Command.Player playerColor;
    private final BlockingQueue<Command> commandsFromServer;
    private Thread processorThread;
    private volatile boolean running;
    
    public ClientCommandProcessor(Game game, Command.Player playerColor, 
                                 BlockingQueue<Command> commandsFromServer) {
        this.game = game;
        this.playerColor = playerColor;
        this.commandsFromServer = commandsFromServer;
        this.running = false;
    }
    
    /**
     * Start the processor thread
     */
    public void start() {
        if (processorThread != null && processorThread.isAlive()) {
            return; // Already running
        }
        
        running = true;
        processorThread = new Thread(this::runProcessorLoop);
        processorThread.setDaemon(true);
        processorThread.setName("ServerCommandProcessor-" + playerColor);
        processorThread.start();
        
        System.out.println("✓ ClientCommandProcessor started for player: " + playerColor);
    }
    
    /**
     * Stop the processor thread
     */
    public void stop() {
        running = false;
        if (processorThread != null && processorThread.isAlive()) {
            processorThread.interrupt();
        }
    }
    
    /**
     * Main processor loop - reads approved commands and executes them
     */
    private void runProcessorLoop() {
        System.out.println("Command processor started for player: " + playerColor);
        
        while (running) {
            try {
                // Read approved command from queue (blocks until there's a command)
                Command command = commandsFromServer.take();
                
                // Execute the approved command in the game
                System.out.println("✓ [SERVER → GAME] Processing approved server command: " + command.getRawCommand());
                
                // Convert the command back to keyboard event and execute in original game
                executeApprovedCommand(command);
                
            } catch (InterruptedException e) {
                System.out.println("Command processor interrupted");
                break;
            } catch (Exception e) {
                System.err.println("Error processing approved command: " + e.getMessage());
                e.printStackTrace();
            }
        }

        
        System.out.println("Command processor stopped");
    }
    
    /**
     * Execute approved command from server in the game
     */
    private void executeApprovedCommand(Command command) {
        try {
            System.out.println("🔍 DEBUG: Processing command: " + command.getRawCommand());
            System.out.println("🔍 DEBUG: Command type: " + command.getCommandType());
            System.out.println("🔍 DEBUG: Key input: " + command.getKeyInput());
            System.out.println("🔍 DEBUG: Player: " + command.getPlayer());
            
            // Convert the command back to simulated keyboard event
            KeyEvent keyEvent = convertCommandToKeyEvent(command);
            
            if (keyEvent != null) {
                System.out.println("🔍 DEBUG: Converted to KeyEvent - keyCode: " + keyEvent.getKeyCode());
                
                // Use the dedicated method for server-approved commands
                if (game instanceof ClientGame) {
                    ((ClientGame) game).executeServerApprovedCommand(keyEvent);
                } else {
                    game.handleRawKeyPressed(keyEvent);
                }
                System.out.println("✅ Executed approved command in game: " + command.getRawCommand());
            } else {
                System.out.println("❌ Could not convert command to key event: " + command.getRawCommand());
            }
        } catch (Exception e) {
            System.err.println("Error executing approved command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Convert command back to keyboard event
     */
    private KeyEvent convertCommandToKeyEvent(Command command) {
        if (command.getCommandType() != Command.CommandType.KEY_INPUT) {
            return null;
        }
        
        String keyInput = command.getKeyInput();
        Command.Player player = command.getPlayer();
        
        // Mapping of commands to keys
        int keyCode = getKeyCodeForCommand(keyInput, player);
        if (keyCode == -1) {
            return null;
        }
        
        // Create simulated keyboard event
        return new KeyEvent(
            new java.awt.Button(), // simple component as source
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0, // modifiers
            keyCode,
            KeyEvent.CHAR_UNDEFINED
        );
    }
    
    /**
     * Get key code according to command and player
     */
    private int getKeyCodeForCommand(String keyInput, Command.Player player) {
        switch (keyInput) {
            case "MOVEMENT_UP":
                return (player == Command.Player.WHITE) ? KeyEvent.VK_W : KeyEvent.VK_UP;
            case "MOVEMENT_DOWN":
                return (player == Command.Player.WHITE) ? KeyEvent.VK_S : KeyEvent.VK_DOWN;
            case "MOVEMENT_LEFT":
                return (player == Command.Player.WHITE) ? KeyEvent.VK_A : KeyEvent.VK_LEFT;
            case "MOVEMENT_RIGHT":
                return (player == Command.Player.WHITE) ? KeyEvent.VK_D : KeyEvent.VK_RIGHT;
            case "SELECT_OR_MOVE":
                return (player == Command.Player.WHITE) ? KeyEvent.VK_SPACE : KeyEvent.VK_ENTER;
            case "JUMP":
                // Left Shift for WHITE, Right Shift for BLACK
                return (player == Command.Player.WHITE) ? KeyEvent.VK_SHIFT : KeyEvent.VK_SHIFT;
            case "HOVER_TO_SELECT":
                return (player == Command.Player.WHITE) ? KeyEvent.VK_C : KeyEvent.VK_V;
            default:
                return -1;
        }
    }
    
    /**
     * Check if processor is running
     */
    public boolean isRunning() {
        return running && processorThread != null && processorThread.isAlive();
    }
}
