package chess.server;

public class ServerChessLogic {
    
    private final ServerBoard board;
    private boolean gameStarted;
    private ServerCommand.Player currentTurn;
    
    public ServerChessLogic() {
        this.board = new ServerBoard(8, 8);
        this.gameStarted = false;
        this.currentTurn = ServerCommand.Player.WHITE;
        System.out.println("ServerChessLogic initialized");
    }
    
    public boolean isValidCommand(ServerCommand command) {
        if (command == null) {
            return false;
        }
        System.out.println("Validating: " + command.getRawCommand());
        return true;
    }
    
    public void processValidCommand(ServerCommand command) {
        System.out.println("Processing: " + command.getRawCommand());
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public ServerCommand.Player getCurrentTurn() {
        return currentTurn;
    }
    
    public String getBoardState() {
        return board.getBoardState();
    }
    
    public void resetGame() {
        gameStarted = false;
        currentTurn = ServerCommand.Player.WHITE;
    }
}
