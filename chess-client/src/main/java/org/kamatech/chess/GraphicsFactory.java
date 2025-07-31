package org.kamatech.chess;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.kamatech.chess.api.IGraphicsFactory;

public class GraphicsFactory implements IGraphicsFactory {
    // Static field to hold current player info for display
    private static String currentPlayerInfo = null;
    
    /**
     * Set the current player info to display on screen
     */
    public static void setCurrentPlayerInfo(String playerColor) {
        currentPlayerInfo = playerColor;
    }
    
    /**
     * Clear the current player info
     */
    public static void clearCurrentPlayerInfo() {
        currentPlayerInfo = null;
    }
    // Sprite cache and timing for animations
    private static final Map<String, List<BufferedImage>> spriteCache = new HashMap<>();
    private static final Map<String, Long> stateEnterTime = new HashMap<>();

    /**
     * Create a Graphics handler for a Piece instance.
     */
    public Graphics createGraphics(Piece piece) {
        return new Graphics(piece);
    }

    /**
     * @deprecated Legacy method, cannot infer Piece context.
     */
    @Deprecated
    public Graphics createGraphics(String pieceType, String state) {
        // Legacy support: produce no-op graphics
        return new Graphics(null);
    }

    /**
     * Get current sprite frame for a piece based on its state and elapsed time
     * This method was moved from Game.java to separate graphics concerns
     */
    public static BufferedImage getSpriteForPiece(Piece piece) {
        String pieceId = piece.getId();
        // Determine folder name based on state
        State.PieceState ps = piece.getState().getCurrentState();
        String stateName = mapStateToSpriteName(ps);
        String cacheKey = pieceId + "_" + stateName;

        List<BufferedImage> frames = spriteCache.get(cacheKey);
        if (frames == null) {
            frames = loadSprites(pieceId, stateName);
            spriteCache.put(cacheKey, frames);
            stateEnterTime.put(cacheKey, System.currentTimeMillis());
        }

        if (frames.isEmpty()) {
            return null;
        }

        long elapsed = System.currentTimeMillis() - stateEnterTime.get(cacheKey);
        long stateDuration;
        if (piece.getState().getCurrentState() == State.PieceState.REST) {
            stateDuration = 10000; // STANDARD_COOLDOWN_MS
        } else {
            stateDuration = 16; // UPDATE_INTERVAL_MS
        }
        double frameDuration = (double) stateDuration / frames.size();
        int index = (int) ((elapsed / frameDuration) % frames.size());
        return frames.get(index);
    }

    /**
     * Map piece state to sprite folder name
     */
    private static String mapStateToSpriteName(State.PieceState state) {
        switch (state) {
            case REST:
                return "long_rest";
            case SHORT_REST:
                return "short_rest";
            case EXHAUST:
                return "exhaust";
            default:
                return state.name().toLowerCase();
        }
    }

    /**
     * Load all sprite frames for a given piece state
     * This method was moved from Game.java to separate graphics concerns
     */
    private static List<BufferedImage> loadSprites(String pieceId, String stateFolder) {
        List<BufferedImage> frames = new ArrayList<>();
    
        String folderPath = "pieces/" + pieceId + "/states/" + stateFolder + "/sprites";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    
        try {
            URL dirURL = classLoader.getResource(folderPath);
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                File dir = new File(dirURL.toURI());
                File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
                if (files != null) {
                    Arrays.sort(files); // sort by name (e.g. 1.png, 2.png...)
                    for (File f : files) {
                        try {
                            frames.add(ImageIO.read(f));
                        } catch (IOException e) {
                            System.err.println("Error reading sprite: " + f.getName());
                        }
                    }
                }
            } else {
                System.err.println("Sprites folder not found: " + folderPath);
            }
        } catch (Exception e) {
            System.err.println("Could not load sprites: " + e.getMessage());
        }
    
        return frames;
    }

    /**
     * Draw a piece with fallback graphics if sprite loading fails
     */
    public static void drawPieceFallback(Graphics2D g2d, Piece piece, int x, int y, int cellWidth, int cellHeight) {
        String pieceId = piece.getId();
        Color pieceColor = piece.isWhite() ? Color.WHITE : Color.BLACK;

        // Set piece color based on type
        if (pieceId.startsWith("K"))
            pieceColor = piece.isWhite() ? Color.YELLOW : Color.ORANGE;
        else if (pieceId.startsWith("Q"))
            pieceColor = piece.isWhite() ? Color.PINK : Color.MAGENTA;
        else if (pieceId.startsWith("R"))
            pieceColor = piece.isWhite() ? Color.CYAN : Color.BLUE;
        else if (pieceId.startsWith("B"))
            pieceColor = piece.isWhite() ? Color.GREEN : Color.DARK_GRAY;
        else if (pieceId.startsWith("N"))
            pieceColor = piece.isWhite() ? Color.LIGHT_GRAY : Color.GRAY;
        else if (pieceId.startsWith("P"))
            pieceColor = piece.isWhite() ? Color.WHITE : Color.BLACK;

        g2d.setColor(pieceColor);
        g2d.fillOval(x + 10, y + 10, cellWidth - 20, cellHeight - 20);

        // Draw piece border
        g2d.setColor(Color.RED);
        g2d.drawOval(x + 10, y + 10, cellWidth - 20, cellHeight - 20);

        // Draw piece ID
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String displayId = pieceId.length() > 2 ? pieceId.substring(0, 2) : pieceId;
        g2d.drawString(displayId, x + cellWidth / 2 - 10, y + cellHeight / 2 + 5);
    }

    /**
     * Draw hover effects for a piece
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawHoverEffect(Graphics2D g2d, int x, int y, int cellWidth, int cellHeight) {
        // Draw THICK PURPLE BORDER around the piece (over the sprite)
        g2d.setColor(new Color(138, 43, 226)); // Blue violet color - distinct from selection colors
        g2d.setStroke(new BasicStroke(8.0f)); // Very thick border
        g2d.drawRect(x + 2, y + 2, cellWidth - 4, cellHeight - 4);

        // Draw SECOND border for even more visibility
        g2d.setColor(new Color(186, 85, 211)); // Medium orchid - lighter purple
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawRect(x + 6, y + 6, cellWidth - 12, cellHeight - 12);

        // Reset stroke
        g2d.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draw selection border for a piece
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawSelectionBorder(Graphics2D g2d, int x, int y, int cellWidth, int cellHeight,
            boolean isWhite) {
        if (isWhite) {
            g2d.setColor(new Color(0, 255, 127)); // Bright spring green for white player
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
        } else {
            g2d.setColor(new Color(255, 20, 147)); // Deep pink for black player
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(x + 3, y + 3, cellWidth - 6, cellHeight - 6);
        }
        g2d.setStroke(new BasicStroke(1)); // Reset stroke
    }

    /**
     * Draw cursor indicators for both players
     */
    private static void drawCursorIndicators(Graphics2D g2d, int cellWidth, int cellHeight,
            int whiteCursorX, int whiteCursorY, int blackCursorX, int blackCursorY) {
        
        // Draw white cursor (bright cyan border - more vibrant)
        int whiteX = whiteCursorX * cellWidth;
        int whiteY = whiteCursorY * cellHeight;
        g2d.setColor(new Color(0, 191, 255, 255)); // Deep sky blue - bright and distinctive
        g2d.setStroke(new BasicStroke(1.5f)); // Even thinner stroke
        g2d.drawRect(whiteX + 3, whiteY + 3, cellWidth - 6, cellHeight - 6);
        
        // Draw white cursor corner markers - more prominent
        g2d.setColor(new Color(224, 255, 255, 255)); // Light cyan corners
        g2d.setStroke(new BasicStroke(2.0f));
        int cornerSize = 10;
        // Top-left corner
        g2d.drawLine(whiteX + 3, whiteY + 3, whiteX + 3 + cornerSize, whiteY + 3);
        g2d.drawLine(whiteX + 3, whiteY + 3, whiteX + 3, whiteY + 3 + cornerSize);
        // Top-right corner
        g2d.drawLine(whiteX + cellWidth - 3 - cornerSize, whiteY + 3, whiteX + cellWidth - 3, whiteY + 3);
        g2d.drawLine(whiteX + cellWidth - 3, whiteY + 3, whiteX + cellWidth - 3, whiteY + 3 + cornerSize);

        // Draw black cursor (bright orange border) - only if different position
        if (blackCursorX != whiteCursorX || blackCursorY != whiteCursorY) {
            int blackX = blackCursorX * cellWidth;
            int blackY = blackCursorY * cellHeight;
            g2d.setColor(new Color(255, 140, 0, 255)); // Dark orange - very vibrant and different
            g2d.setStroke(new BasicStroke(1.5f)); // Even thinner stroke
            g2d.drawRect(blackX + 3, blackY + 3, cellWidth - 6, cellHeight - 6);
            
            // Draw black cursor corner markers - more prominent
            g2d.setColor(new Color(255, 69, 0, 255)); // Red-orange corners
            g2d.setStroke(new BasicStroke(2.0f));
            // Bottom-left corner
            g2d.drawLine(blackX + 3, blackY + cellHeight - 3 - cornerSize, blackX + 3, blackY + cellHeight - 3);
            g2d.drawLine(blackX + 3, blackY + cellHeight - 3, blackX + 3 + cornerSize, blackY + cellHeight - 3);
            // Bottom-right corner
            g2d.drawLine(blackX + cellWidth - 3 - cornerSize, blackY + cellHeight - 3, blackX + cellWidth - 3, blackY + cellHeight - 3);
            g2d.drawLine(blackX + cellWidth - 3, blackY + cellHeight - 3 - cornerSize, blackX + cellWidth - 3, blackY + cellHeight - 3);
        }
        
        // Reset stroke
        g2d.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Draw the complete board with all pieces
     * This method was moved from Game.java to separate graphics concerns
     */
    public static void drawGameBoard(Graphics2D g2d, Board board, Map<String, Piece> pieces,
            String hoveredPieceWhite, String hoveredPieceBlack,
            String selectedPieceWhite, String selectedPieceBlack,
            boolean whiteInMovementMode, boolean blackInMovementMode,
            double whiteVisualX, double whiteVisualY, double blackVisualX, double blackVisualY,
            int whiteCursorX, int whiteCursorY, int blackCursorX, int blackCursorY,
            int panelWidth, int panelHeight) {
        drawGameBoard(g2d, board, pieces, hoveredPieceWhite, hoveredPieceBlack,
                     selectedPieceWhite, selectedPieceBlack, whiteInMovementMode, blackInMovementMode,
                     whiteVisualX, whiteVisualY, blackVisualX, blackVisualY,
                     whiteCursorX, whiteCursorY, blackCursorX, blackCursorY,
                     panelWidth, panelHeight, null);
    }
    
    public static void drawGameBoard(Graphics2D g2d, Board board, Map<String, Piece> pieces,
            String hoveredPieceWhite, String hoveredPieceBlack,
            String selectedPieceWhite, String selectedPieceBlack,
            boolean whiteInMovementMode, boolean blackInMovementMode,
            double whiteVisualX, double whiteVisualY, double blackVisualX, double blackVisualY,
            int whiteCursorX, int whiteCursorY, int blackCursorX, int blackCursorY,
            int panelWidth, int panelHeight, String currentPlayerColor) {
        // Draw board background
        g2d.drawImage(board.getImage().getImage(), 0, 0, panelWidth, panelHeight, null);

        // Calculate cell dimensions
        int cellWidth = panelWidth / board.getWidthCells();
        int cellHeight = panelHeight / board.getHeightCells();

        // Draw cursor indicators
        drawCursorIndicators(g2d, cellWidth, cellHeight, 
                           whiteCursorX, whiteCursorY, blackCursorX, blackCursorY);

        // Draw all pieces
        drawAllPieces(g2d, pieces, cellWidth, cellHeight,
                hoveredPieceWhite, hoveredPieceBlack,
                selectedPieceWhite, selectedPieceBlack,
                whiteInMovementMode, blackInMovementMode,
                whiteVisualX, whiteVisualY, blackVisualX, blackVisualY);
        
        // Draw player info if available
        if (currentPlayerInfo != null) {
            drawPlayerInfo(g2d, currentPlayerInfo, "", panelWidth, panelHeight);
        } else if (currentPlayerColor != null) {
            drawPlayerInfo(g2d, currentPlayerColor, "", panelWidth, panelHeight);
        }
    }

    /**
     * Draw all pieces with their sprites, hover effects, and selection borders
     */
    private static void drawAllPieces(Graphics2D g2d, Map<String, Piece> pieces, int cellWidth, int cellHeight,
            String hoveredPieceWhite, String hoveredPieceBlack,
            String selectedPieceWhite, String selectedPieceBlack,
            boolean whiteInMovementMode, boolean blackInMovementMode,
            double whiteVisualX, double whiteVisualY, double blackVisualX, double blackVisualY) {
        // FIRST: Draw all pieces with sprites and hover effects
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            String key = entry.getKey();
            Piece piece = entry.getValue();

            // Calculate piece position in pixels
            int x = (int) (piece.getX() * cellWidth);
            int y = (int) (piece.getY() * cellHeight);

            // Check if this piece is hovered (but not if it's already selected)
            boolean isHovered = (key.equals(hoveredPieceWhite) && !key.equals(selectedPieceWhite))
                    || (key.equals(hoveredPieceBlack) && !key.equals(selectedPieceBlack));

            // Draw sprite
            BufferedImage spriteImage = getSpriteForPiece(piece);
            if (spriteImage != null) {
                // Draw real sprite image first
                g2d.drawImage(spriteImage, x + 5, y + 5, cellWidth - 10, cellHeight - 10, null);

                // THEN draw hover border OVER the sprite for maximum visibility
                if (isHovered) {
                    drawHoverEffect(g2d, x, y, cellWidth, cellHeight);
                }
            } else {
                // Fallback to colored circles if sprite loading fails
                drawPieceFallback(g2d, piece, x, y, cellWidth, cellHeight);
            }

            // Draw remaining rest time for any state with non-zero cooldown
            drawRemainingTime(g2d, piece, x, y, cellWidth, cellHeight);
        }

        // SECOND: Draw selection borders
        drawSelectionBorders(g2d, pieces, cellWidth, cellHeight, selectedPieceWhite, selectedPieceBlack,
                whiteInMovementMode, blackInMovementMode,
                whiteVisualX, whiteVisualY, blackVisualX, blackVisualY);
    }

    /**
     * Draw remaining time for pieces in cooldown states
     */
    private static void drawRemainingTime(Graphics2D g2d, Piece piece, int x, int y, int cellWidth, int cellHeight) {
        long remMs = piece.getState().getRemainingStateTime();
        if (remMs > 0) {
            String remText = String.format("%.1f", remMs / 1000.0);
            g2d.setColor(new Color(0, 0, 255, 200));
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(remText, x + cellWidth / 2 - 10, y + cellHeight / 2 + 5);
        }
    }

    /**
     * Draw selection borders for selected pieces
     */
    private static void drawSelectionBorders(Graphics2D g2d, Map<String, Piece> pieces, int cellWidth, int cellHeight,
            String selectedPieceWhite, String selectedPieceBlack,
            boolean whiteInMovementMode, boolean blackInMovementMode,
            double whiteVisualX, double whiteVisualY, double blackVisualX, double blackVisualY) {
        for (Map.Entry<String, Piece> entry : pieces.entrySet()) {
            String key = entry.getKey();
            Piece piece = entry.getValue();

            // Calculate piece position - ALWAYS use visual position if available for selected pieces
            int x, y;
            if (key.equals(selectedPieceWhite) && whiteVisualX >= 0 && whiteVisualY >= 0) {
                // White piece - use visual position (whether in movement mode or not)
                x = (int) (whiteVisualX * cellWidth);
                y = (int) (whiteVisualY * cellHeight);
            } else if (key.equals(selectedPieceBlack) && blackVisualX >= 0 && blackVisualY >= 0) {
                // Black piece - use visual position (whether in movement mode or not)
                x = (int) (blackVisualX * cellWidth);
                y = (int) (blackVisualY * cellHeight);
            } else {
                // Not selected or no visual position - use real position
                x = (int) (piece.getX() * cellWidth);
                y = (int) (piece.getY() * cellHeight);
            }

            // Draw selection borders based on the actual piece color property, not its ID
            // White player can only highlight white pieces
            if (key.equals(selectedPieceWhite) && piece.isWhite()) {
                drawSelectionBorder(g2d, x, y, cellWidth, cellHeight, true);
            }

            // Black player can only highlight black pieces
            if (key.equals(selectedPieceBlack) && !piece.isWhite()) {
                drawSelectionBorder(g2d, x, y, cellWidth, cellHeight, false);
            }
        }
    }
    
    /**
     * Draw player information on screen
     */
    public static void drawPlayerInfo(Graphics2D g2d, String playerColor, String playerControls, 
                                     int panelWidth, int panelHeight) {
        // Save original settings
        java.awt.Color originalColor = g2d.getColor();
        java.awt.Font originalFont = g2d.getFont();
        
        // Set font for player info
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        
        // Determine colors and text based on player
        String playerText;
        String controlsText;
        java.awt.Color playerBgColor;
        java.awt.Color textColor = java.awt.Color.BLACK;
        
        if ("WHITE".equals(playerColor)) {
            playerText = "White Player";
            controlsText = "WASD + C + Space";
            playerBgColor = new java.awt.Color(255, 255, 255, 200); // Semi-transparent white
        } else {
            playerText = "Black Player";
            controlsText = "Arrows + V + Enter";
            playerBgColor = new java.awt.Color(100, 100, 100, 200); // Semi-transparent gray
            textColor = java.awt.Color.WHITE;
        }
        
        // Calculate text dimensions
        int playerTextWidth = fm.stringWidth(playerText);
        int controlsTextWidth = fm.stringWidth(controlsText);
        int maxTextWidth = Math.max(playerTextWidth, controlsTextWidth);
        int textHeight = fm.getHeight();
        
        // Position info box EXTREMELY far right - almost off screen!
        int boxWidth = maxTextWidth + 20;
        int boxHeight = textHeight * 2 + 20;
        int boxX = panelWidth - 50; // VERY close to right edge!
        int boxY = panelHeight / 2 - boxHeight / 2; // Same vertical position
        
        // Draw background box
        g2d.setColor(playerBgColor);
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        // Draw border
        g2d.setColor(java.awt.Color.BLACK);
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        // Draw text
        g2d.setColor(textColor);
        int textX = boxX + (boxWidth - playerTextWidth) / 2;
        int textY = boxY + textHeight;
        g2d.drawString(playerText, textX, textY);
        
        // Draw controls text
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        controlsTextWidth = fm.stringWidth(controlsText);
        textX = boxX + (boxWidth - controlsTextWidth) / 2;
        textY += textHeight;
        g2d.drawString(controlsText, textX, textY);
        
        // Restore original settings
        g2d.setColor(originalColor);
        g2d.setFont(originalFont);
    }
}
