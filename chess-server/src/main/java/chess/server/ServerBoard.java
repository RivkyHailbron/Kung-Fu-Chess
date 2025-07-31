package chess.server;

/**
 * Minimal Board representation for server - no graphics needed!
 * Only tracks piece positions for validation
 */
public class ServerBoard {
    private final int width;
    private final int height;
    private String[][] pieces; // Simple 2D array of piece types

    public ServerBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.pieces = new String[height][width];
        initializeBoard();
    }

    /**
     * Initialize standard chess starting position
     */
    private void initializeBoard() {
        // Clear board
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                pieces[row][col] = "EMPTY";
            }
        }

        // Set up starting pieces (simplified)
        // White pieces (bottom rows 6-7)
        pieces[7][0] = "WHITE_ROOK";
        pieces[7][1] = "WHITE_KNIGHT";
        pieces[7][2] = "WHITE_BISHOP";
        pieces[7][3] = "WHITE_QUEEN";
        pieces[7][4] = "WHITE_KING";
        pieces[7][5] = "W HITE_BISHOP";
        pieces[7][6] = "WHITE_KNIGHT";
        pieces[7][7] = "WHITE_ROOK";
        for (int col = 0; col < width; col++) {
            pieces[6][col] = "WHITE_PAWN";
        }

        // Black pieces (top rows 0-1)
        pieces[0][0] = "BLACK_ROOK";
        pieces[0][1] = "BLACK_KNIGHT";
        pieces[0][2] = "BLACK_BISHOP";
        pieces[0][3] = "BLACK_QUEEN";
        pieces[0][4] = "BLACK_KING";
        pieces[0][5] = "BLACK_BISHOP";
        pieces[0][6] = "BLACK_KNIGHT";
        pieces[0][7] = "BLACK_ROOK";
        for (int col = 0; col < width; col++) {
            pieces[1][col] = "BLACK_PAWN";
        }

        System.out.println("Server board initialized (" + width + "x" + height + ")");
    }

    /**
     * Get piece at position
     */
    public String getPieceAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return pieces[y][x];
        }
        return "OUT_OF_BOUNDS";
    }

    /**
     * Set piece at position
     */
    public void setPieceAt(int x, int y, String piece) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pieces[y][x] = piece;
        }
    }

    /**
     * Check if position is valid
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Get board state as string (for debugging)
     */
    public String getBoardState() {
        StringBuilder sb = new StringBuilder();
        sb.append("Server Board State:\n");
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                String piece = pieces[row][col];
                if (piece.equals("EMPTY")) {
                    sb.append(".. ");
                } else {
                    sb.append(piece.substring(0, Math.min(2, piece.length()))).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
