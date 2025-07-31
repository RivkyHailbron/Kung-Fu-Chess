package chess;

import java.util.concurrent.BlockingQueue;
import org.kamatech.chess.Command;

/**
 * Handles sending keyboard commands to server for validation
 * Clean separation: Keyboard → Queue → Server
 */
public class ClientCommandSender {
    private final ChessClient client;
    private final Command.Player playerColor;
    private final BlockingQueue<Command> keyboardInputQueue;
    private Thread senderThread;
    private volatile boolean running;
    
    public ClientCommandSender(ChessClient client, Command.Player playerColor, 
                              BlockingQueue<Command> keyboardInputQueue) {
        this.client = client;
        this.playerColor = playerColor;
        this.keyboardInputQueue = keyboardInputQueue;
        this.running = false;
    }
    
    /**
     * Start the sender thread
     */
    public void start() {
        if (senderThread != null && senderThread.isAlive()) {
            return; // Already running
        }
        
        running = true;
        senderThread = new Thread(this::runSenderLoop);
        senderThread.setDaemon(true);
        senderThread.setName("ServerSender-" + playerColor);
        senderThread.start();
        
        System.out.println("✓ ClientCommandSender started for player: " + playerColor);
    }
    
    /**
     * Stop the sender thread
     */
    public void stop() {
        running = false;
        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
        }
    }
    
    /**
     * Main sender loop - reads from queue and sends to server
     */
    private void runSenderLoop() {
        System.out.println("Server sender thread started for player: " + playerColor);
        
        while (running) {
            try {
                // Read command from keyboard input queue (blocks until there's a command)
                Command command = keyboardInputQueue.take();
                
                // Send command to server for validation
                client.sendCommandToServer(command.getRawCommand());
                System.out.println("✓ [QUEUE → SERVER] Sent to server for validation: " + command.getRawCommand());
                
            } catch (InterruptedException e) {
                System.out.println("Server sender thread interrupted");
                break;
            } catch (Exception e) {
                System.err.println("Error sending command to server: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Server sender thread stopped");
    }
    
    /**
     * Add command to sending queue
     */
    public void queueCommand(Command command) {
        try {
            keyboardInputQueue.offer(command);
            System.out.println("✓ [KEYBOARD → QUEUE] Added to keyboard queue: " + command.getRawCommand());
        } catch (Exception e) {
            System.err.println("✗ Error adding command to keyboard queue: " + e.getMessage());
        }
    }
    
    /**
     * Check if sender is running
     */
    public boolean isRunning() {
        return running && senderThread != null && senderThread.isAlive();
    }
}
