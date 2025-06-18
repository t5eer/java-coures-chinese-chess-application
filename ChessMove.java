package src.ChineseChess.v4_2;

import java.awt.*;

// 棋子移动的封装类
public class ChessMove {
    private final Chess chess;
    private final Point from;
    private final Point to;
    private final Chess captured;

    public ChessMove(Chess chess, Point from, Point to, Chess captured){
        this.chess = chess;
        this.from = new Point(from);
        this.to = new Point(to);
        this.captured = captured;
    }

    public Chess getChess() {
        return chess;
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public Chess getCaptured() {
        return captured;
    }

    @Override
    public String toString() {
        return "ChessMove{" +
                "chess=" + chess +
                ", from=" + from +
                ", to=" + to +
                ", captured=" + captured +
                '}';
    }
}
