package chess;

import java.awt.event.KeyEvent;

import org.kamatech.chess.Command;

/**
 * Converts keyboard events to commands
 * Clean separation of keyboard input logic
 */
public class KeyboardCommandConverter {
    
    private final Command.Player myPlayerColor;
    
    public KeyboardCommandConverter(Command.Player myPlayerColor) {
        this.myPlayerColor = myPlayerColor;
    }
    
    /**
     * Convert keyboard event to command
     */
    public Command convertKeyEventToCommand(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Check if this is a key for my player
        Command.Player player = determinePlayerFromKeyCode(keyCode);
        if (player != myPlayerColor) {
            return null; // Don't send commands for the other player
        }
        
        // Convert key to command
        if (Command.isWhiteMovementKey(keyCode) || Command.isBlackMovementKey(keyCode)) {
            String direction = Command.keyCodeToDirection(keyCode);
            return Command.createKeyInput("MOVEMENT_" + direction, player);
        }
        
        if (Command.isWhiteHoverKey(keyCode) || Command.isBlackHoverKey(keyCode)) {
            String direction = Command.getHoverDirection(keyCode);
            return Command.createKeyInput("HOVER_" + direction, player);
        }
        
        if (Command.isNumberKey(keyCode)) {
            int pieceIndex = Command.numberKeyToIndex(keyCode);
            return Command.createKeyInput("SELECT_PIECE_" + pieceIndex, player);
        }
        
        // Handle special keys
        if (keyCode == KeyEvent.VK_SPACE) {
            if (player == Command.Player.WHITE) {
                return Command.createKeyInput("SELECT_OR_MOVE", player);
            }
        } else if (keyCode == KeyEvent.VK_ENTER) {
            if (player == Command.Player.BLACK) {
                return Command.createKeyInput("SELECT_OR_MOVE", player);
            }
        } else if (keyCode == KeyEvent.VK_SHIFT) {
            int loc = e.getKeyLocation();
            if ((loc == KeyEvent.KEY_LOCATION_LEFT && player == Command.Player.WHITE) ||
                (loc == KeyEvent.KEY_LOCATION_RIGHT && player == Command.Player.BLACK)) {
                return Command.createKeyInput("JUMP", player);
            }
        } else if (keyCode == KeyEvent.VK_C) {
            if (player == Command.Player.WHITE) {
                return Command.createKeyInput("HOVER_TO_SELECT", player);
            }
        } else if (keyCode == KeyEvent.VK_V) {
            if (player == Command.Player.BLACK) {
                return Command.createKeyInput("HOVER_TO_SELECT", player);
            }
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            return Command.createGameControl("END_GAME");
        }
        
        return null;
    }
    
    /**
     * Determine player according to key
     */
    private Command.Player determinePlayerFromKeyCode(int keyCode) {
        // WASD keys and additional keys for white player
        if (Command.isWhiteMovementKey(keyCode) || Command.isWhiteHoverKey(keyCode) ||
            keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_C) {
            return Command.Player.WHITE;
        }
        
        // Arrow keys and additional keys for black player
        if (Command.isBlackMovementKey(keyCode) || Command.isBlackHoverKey(keyCode) ||
            keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_V) {
            return Command.Player.BLACK;
        }
        
        // Number keys - depends on my player
        if (Command.isNumberKey(keyCode)) {
            return myPlayerColor;
        }
        
        // SHIFT keys - for JUMP, we need to use myPlayerColor and check location later
        if (keyCode == KeyEvent.VK_SHIFT) {
            return myPlayerColor;
        }
        
        // General keys
        return Command.Player.SYSTEM;
    }
}
