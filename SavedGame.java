package src.ChineseChess.v4_2;

import java.util.ArrayList;
import java.util.List;

public class SavedGame {
    private int currentPlayer;
    private boolean isAiEanbled;
    private List<Chess> chessList;
    private List<ChessMove> moveHistory = new ArrayList<>();
    private Chess blackBoss;
    private Chess redBoss;


    public SavedGame() {}

    public Chess getBlackBoss() {
        return blackBoss;
    }

    public Chess getRedBoss() {
        return redBoss;
    }

    public void setBlackBoss(Chess blackBoss) {
        this.blackBoss = blackBoss;
    }

    public void setRedBoss(Chess redBoss) {
        this.redBoss = redBoss;
    }

    public boolean isAiEanbled() {
        return isAiEanbled;
    }

    public List<ChessMove> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(List<ChessMove> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public void setAiEanbled(boolean aiEanbled) {
        isAiEanbled = aiEanbled;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public List<Chess> getChessList() {
        return chessList;
    }

    public void setChessList(List<Chess> chessList) {
        this.chessList = chessList;
    }
}

