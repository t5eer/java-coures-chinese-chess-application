package src.ChineseChess.v4_2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChessAI {
    private  int aiColor; // AI执棋颜色
    private final int maxDepth; // 搜索深度
    private final static int[][] BING_POSITION_SCORE = new int[9][10];

    public int getAiColor() {
        return aiColor;
    }
    public void setAiColor(int aiColor) {
        this.aiColor = aiColor;
        System.out.println(aiColor);
    }


    public ChessAI(int aiColor, int maxDepth){
        this.aiColor = aiColor;
        this.maxDepth = maxDepth;
        initBingPositionScore(); // 初始化位置权重表
    }

    // 初始化兵的位置价值
    private void initBingPositionScore(){
        for (int y = 0; y < 10; y++){
            for (int x = 0; x < 9; x++){
                BING_POSITION_SCORE[x][y] = y > 4 ? 200 : 100;
            }
        }
    }

    /**
     * 找到最好的一步棋 供controller使用
     * @param originalBoard
     * @return
     */
    public ChessMove findBestMove(Chessboard originalBoard){
        Chessboard boardCopy = originalBoard.copy();// 使用副本
        return alphaBetaSearch(boardCopy, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true).chessMove;
    }

    /**
     * Alpha-Beta搜索核心
     * @param board 棋盘（存有数据）
     * @param depth 搜索深度
     * @param alpha 当前已知走法，能保证自己获得的最低分数
     * @param beta 当前已知走法，对手能限制自己获得的最高分数
     * @param maximizingSelf 是否在让自己的利益最大化
     * @return SearchResult包含分数和那步最好的棋
     */
    private SearchResult alphaBetaSearch(Chessboard board, int depth, int alpha, int beta, boolean maximizingSelf){
        // 递归终止条件
        // 检查游戏是否结束
        if (isGameOver(board)) {
            // 如果当前轮到AI走但将帅已被吃，则评估为极低分
            int score = maximizingSelf ? Integer.MIN_VALUE + 100 : Integer.MAX_VALUE - 100;
            return new SearchResult(score, null);
        }

        if (depth == 0) {
            // 当达到搜索深度时进行盘面打分
            return new SearchResult(evaluateBoard(board), null);
        }

        List<ChessMove> moves = generateAllLegalMoves(board, maximizingSelf ? aiColor : 1 - aiColor);// 所有选择
        sortMoves(moves); // 排序以加速剪枝

        ChessMove bestMove = null;
        for (ChessMove move : moves){
            board.applyMove(move, true);
            int evaluation = -alphaBetaSearch(board, depth - 1, -beta, -alpha, !maximizingSelf).score;
            board.undoMove(move);

            if (evaluation > alpha){
                alpha = evaluation;
                bestMove = move;
            }
            if (alpha >= beta) break;
        }
        return new SearchResult(alpha, bestMove);
    }

    /**
     * 评估函数
     * @param board 棋盘
     * @return 分数
     */
    private int evaluateBoard(Chessboard board){
        int score = 0;
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 10; y++){
                Chess chess = board.getChess(x, y);
                if (chess != null){
                    int value = getPieceValue(chess) + getPositionScore(chess, x, y);
                    score += (chess.getColor() == aiColor) ? value : - value;
                }
            }
        }
        return score;
    }



    // 棋子的基础价值
    private int getPieceValue(Chess chess){
        return switch (chess.getName()){
            case "boss" -> 100000;
            case "che" -> 900;
            case "ma" -> 400;
            case "pao" -> 450;
            case "shi" -> 180;
            case "xiang" -> 200;
            case "bing" -> 100;
            default -> 0;
        };
    }

    // 位置得分
    private int getPositionScore(Chess chess, int x , int y){
        switch (chess.getName()){
            case "ma" -> {
                return (y > 3 && y < 6) ? 50 : 0;
            }
            case "che" -> {
                return (x == 4 || x == 6) ? 20 : 0;
            }
            case "bing" -> {
                return (chess.getColor() == aiColor) ? BING_POSITION_SCORE[x][y] :
                        BING_POSITION_SCORE[x][9 - y];
            }
            default -> {
                return 0;
            }
        }
    }

    /**
     * 生成所有可能的走法
     * @param board 棋盘（有数据）
     * @param color 当前谁走便生成谁的
     * @return 存着各走法的动态数组
     */
    protected static List<ChessMove> generateAllLegalMoves(Chessboard board, int color){
        List<ChessMove> moves = new ArrayList<>();
        for(int i = 0;i < 9; i++){
            for (int j = 0; j < 10; j++){
                Chess chess = board.getChess(i, j);
                if (chess != null && chess.getColor() == color){
                    for (int tx = 0; tx < 9; tx++){
                        for (int ty = 0; ty < 10; ty++){
                            Point to = new Point(tx + 1, ty + 1); // 完成从数组坐标到棋盘坐标的转化

                            ChessMove move = new ChessMove(chess, chess.getP(), to, board.getChessFromP(to.x, to.y));
                            if (board.isValidMove(move, chess, true)){
                                moves.add(move);
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    /**
     * 将更好的走法排在更前面，以使剪枝发挥最大威力
     * @param moves
     */
    private void sortMoves(List<ChessMove> moves){
        moves.sort((m1, m2) -> {
            int score1 = (m1.getCaptured() != null) ? getPieceValue(m1.getCaptured()) : 0;
            int score2 = (m2.getCaptured() != null) ? getPieceValue(m2.getCaptured()) : 0;
            return score2 - score1; // 降序排序
        });
    }

    // 判断是否结束
    private boolean isGameOver(Chessboard board){
        return board.redBoss == null || board.blackBoss == null;
    }

    // 内部类存储搜索结果
    private static class SearchResult{
        int score;
        ChessMove chessMove;
        SearchResult(int score, ChessMove chessMove){
            this.score = score;
            this.chessMove = chessMove;
        }
    }
}
