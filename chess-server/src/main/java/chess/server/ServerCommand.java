package chess.server;

/**
 * Minimal command representation for server - only what's needed for validation
 * Server doesn't need all the complex command parsing, just basic validation
 */
public class ServerCommand {
    private final String rawCommand;
    private CommandType commandType;
    private String keyInput;
    private Player player;
    
    public enum CommandType {
        KEY_INPUT,
        MOVE,
        JUMP,
        GAME_CONTROL
    }
    
    public enum Player {
        WHITE,
        BLACK,
        SYSTEM
    }
    
    public ServerCommand(String rawCommand) {
        this.rawCommand = rawCommand;
        parseCommand();
    }
    
    /**
     * Simple command parsing - just enough for server validation
     */
    private void parseCommand() {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            this.commandType = CommandType.GAME_CONTROL;
            this.keyInput = "INVALID";
            this.player = Player.SYSTEM;
            return;
        }
        
        String[] parts = rawCommand.split("_");
        if (parts.length >= 2) {
            // Parse player (first character)
            String playerStr = parts[0];
            if (playerStr.equals("W")) {
                this.player = Player.WHITE;
            } else if (playerStr.equals("B")) {
                this.player = Player.BLACK;
            } else {
                this.player = Player.SYSTEM;
            }
            
            // Parse command type and action
            this.keyInput = rawCommand.substring(2); // Remove "W_" or "B_"
            
            if (keyInput.startsWith("MOVEMENT_") || keyInput.equals("SELECT_OR_MOVE") || 
                keyInput.equals("HOVER_TO_SELECT")) {
                this.commandType = CommandType.KEY_INPUT;
            } else if (keyInput.equals("JUMP")) {
                this.commandType = CommandType.JUMP;
            } else if (keyInput.startsWith("MOVE")) {
                this.commandType = CommandType.MOVE;
            } else {
                this.commandType = CommandType.GAME_CONTROL;
            }
        } else {
            // Game control commands
            this.commandType = CommandType.GAME_CONTROL;
            this.keyInput = rawCommand;
            this.player = Player.SYSTEM;
        }
    }
    
    public String getRawCommand() { return rawCommand; }
    public CommandType getCommandType() { return commandType; }
    public String getKeyInput() { return keyInput; }
    public Player getPlayer() { return player; }
    
    @Override
    public String toString() {
        return "ServerCommand{" +
                "raw='" + rawCommand + '\'' +
                ", type=" + commandType +
                ", key='" + keyInput + '\'' +
                ", player=" + player +
                '}';
    }
}
