package org.kamatech.chess.listeners;

import org.kamatech.chess.events.EventListener;
import org.kamatech.chess.events.PieceCapturedEvent;
public class ScoreTracker implements EventListener<PieceCapturedEvent> {
    private int score = 0;

    @Override
    public void onEvent(PieceCapturedEvent event) {
        switch (event.pieceType) {
            case "Pawn":
                score += 1;
                break;
            case "Knight":
            case "Bishop":
                score += 3;
                break;
            case "Rook":
                score += 5;
                break;
            case "Queen":
                score += 9;
                break;
        }
        System.out.println("Score updated: " + score);
    }

    public int getScore() {
        return score;
    }
}
