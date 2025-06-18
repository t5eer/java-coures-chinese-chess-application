package src.ChineseChess.v4_2;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Chessboard {
    public Chess[][] board= new Chess[9][10];// 10 x 9的棋盘
    private int currentPlayer; // 0:红方，1：黑方
    private List<ChessMove> moveHistory = new ArrayList<>();// 移动历史记录
    public Chess blackBoss;
    public Chess redBoss;

    public List<ChessMove> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(List<ChessMove> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Chess getChessFromP(int x, int y){
        return board[x - 1][y - 1];
    }

    public Chess getChess(int x, int y){
        return board[x][y];
    }

    public void initChess(){
        String[] names = {"che","ma","xiang","shi","boss","shi","xiang","ma","che","pao","pao","bing","bing","bing","bing","bing"};
        Point[] points = {
                // 上半部分棋子网格坐标
                new Point(1,1),new Point(2,1),new Point(3,1),new Point(4,1),
                new Point(5,1),new Point(6,1),new Point(7,1),new Point(8,1),
                new Point(9,1),new Point(2,3),new Point(8,3),new Point(1,4),
                new Point(3,4),new Point(5,4),new Point(7,4),new Point(9,4),
        };
        // 先创黑棋
        for (int i = 0;i<names.length;i++){
            Chess c = new Chess(names[i],1,points[i]);
            c.setInitP(c.getP());
            int x = c.getP().x - 1;
            int y = c.getP().y - 1;
            board[x][y] = c;
            System.out.println(c.toString());
        }
        // 再创红棋
        for (int i = 0;i<names.length;i++){
            Chess c = new Chess(names[i],0);
            c.setP(c.reverse(points[i]));
            c.setInitP(c.getP());
            int x = c.getP().x - 1;
            int y = c.getP().y - 1;
            board[x][y] = c;
            System.out.println(c.toString());
        }
        blackBoss = board[4][0];
        System.out.println(blackBoss.toString());
        redBoss = board[4][9];
        System.out.println(redBoss.toString());

    }

    /**
     * 棋盘备份
     * @return
     */
    public Chessboard copy() {
        Chessboard newBoard = new Chessboard();
        newBoard.currentPlayer = this.currentPlayer;
        newBoard.moveHistory = new ArrayList<>(this.moveHistory); // 浅拷贝历史记录

        // 深拷贝棋盘上的每个棋子
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                Chess original = this.board[x][y];
                if (original != null) {
                    Chess copyChess = new Chess(original.getName(), original.getColor());
                    copyChess.setP(new Point(original.getP()));
                    copyChess.setInitP(new Point(original.getInitP()));
                    newBoard.board[x][y] = copyChess;

                    // 更新将帅引用，要确保新棋盘得将帅引用指向新复制的对象
                    if (original == this.redBoss) {
                        newBoard.redBoss = copyChess;
                    } else if (original == this.blackBoss) {
                        newBoard.blackBoss = copyChess;
                    }
                }
            }
        }

        // 更新将帅的引用
//        newBoard.redBoss = redBoss; // 红帅固定位置
//        newBoard.blackBoss = blackBoss; // 黑将固定位置
        return newBoard;
    }

    /**
     * 移动（这步棋）是否符合棋规，供controller层使用
     * @param move
     * @param selected
     * @return
     */
    public boolean isValidMove(ChessMove move, Chess selected, boolean isAIUse){
        // 先检查基本规则
        if (!isValidMoveWithCheck(move, selected)){
            return false;
        }

        // 临时应用移动，检查移动后是否会造成将军或飞将
        applyMove(move, true);
        boolean isCheck = isCheck(selected.getColor(), true);
        undoMove(move);
        if (isCheck){
            System.out.println("检测到移动后引起将军，非法移动！");
            // 是玩家移动造成的，进行语音提示
            if (!isAIUse){
                File file = new File("music" + File.separator + "tip_boss.mp3");
                Music.playMusic(file);
            }
            return false;// 检测到移动后引起将军，非法移动！
        }

        return true;
    }

    // 不考虑将军检测
    private boolean isValidMoveWithCheck(ChessMove move, Chess selected){
        if (!selected.isValidMove(move) || move.getTo().equals(move.getFrom())){
            return false;// 棋子起点重点重合，非法！
        }
        if (move.getCaptured() != null && move.getChess().getColor() == move.getCaptured().getColor()){
            return false;// 棋子吃己方棋子，非法
        }

        switch (selected.getName()) {
            case "che" -> {
                return getCount(move) == 0;
            }
            case "ma" -> {
                return !isObstructed(move);
            }
            case "xiang" -> {
                return !isObstructed(move) && !isCrossRiver2(move);
            }
            case "shi" -> {
                return isInPalace(move);
            }
            case "boss" -> {

                return isInPalace(move);
            }
            case "pao" -> {
                Chess c = getChessFromP(move.getTo().x, move.getTo().y);
                if (c != null){
                    // 吃子
                    return getCount(move) == 1;
                }else {
                    // 移动
                    return getCount(move) == 0;
                }
            }
            case "bing" -> {
                if (isCrossRiver1(move)){
                    // 过河兵
                    return !isBackward(move);
                }else {
                    // 没过河
                    return isForward(move);
                }
            }
        }


        return false;
    }

    /**
     * 检查是否有将军和飞将
     * @param color 走棋方的颜色
     * @return
     */
    public boolean isCheck(int color, boolean isDeduce)
    {
        // 1、检查是否被其他棋子将军
        Chess king = (color == 0) ? redBoss : blackBoss;

        int enemyColor = 1 - color;
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                Chess chess = this.board[x][y];
                if (chess != null && chess.getColor() == enemyColor) {
                    ChessMove tMove = new ChessMove(
                            chess,
                            chess.getP(),
                            king.getP(),
                            king
                    );
                    if (isValidMoveWithCheck(tMove, chess)){
                        if (!isDeduce){
                            File file = new File("music" + File.separator + "jiangjun.mp3");
                            Music.playMusic(file);
                        }
                        return true;
                    }
                }
            }
        }

        // 2、检查将帅是否直接对面（飞将)
        return redBoss.getP().x == blackBoss.getP().x && getCount(new ChessMove(redBoss, redBoss.getP(), blackBoss.getP(), null)) == 0;
    }

    /**
     * 实施移动，主要是改变棋子的位置
     * @param move 棋子移动的封装类
     */
    public void applyMove(ChessMove move, boolean isDeduce){
        // 检查是否吃掉将帅
        if (move.getCaptured() != null){
            if (move.getCaptured() == redBoss){
                redBoss = null;
            } else if (move.getCaptured() == blackBoss){
                blackBoss = null;
            }
        }

        board[move.getFrom().x - 1][move.getFrom().y - 1] = null;
        board[move.getTo().x - 1][move.getTo().y - 1] = move.getChess();
        move.getChess().setP(move.getTo());
        moveHistory.add(move); // 将这一步棋加入棋谱中
        currentPlayer = 1 - currentPlayer; // 切换玩家

        // 将军播放“将军”
        if (!isCheck(currentPlayer, isDeduce)){
            // 在不将军的情况下吃子播放“吃”
            if(!isDeduce && move.getCaptured() != null){
                File file = new File("music" + File.separator + "chi.mp3");
                Music.playMusic(file);
            }
        }
    }

    /**
     * 撤销移动
     * @param move
     */
    public void undoMove(ChessMove move)
    {
        board[move.getFrom().x - 1][move.getFrom().y - 1] = move.getChess();
        board[move.getTo().x - 1][move.getTo().y - 1] = move.getCaptured();
        move.getChess().setP(move.getFrom());
        moveHistory.remove(move); // 撤销移动历史
        currentPlayer = 1 - currentPlayer; // 切换玩家

        // 恢复棋盘将帅引用
        if (move.getCaptured() != null && move.getCaptured().getName().equals("boss")){
            if (move.getCaptured().getColor() == 0){
                redBoss = move.getCaptured();
            } else {
                blackBoss = move.getCaptured();
            }
        }
    }



    /**
     * 判断棋子初始是在棋盘的上半部分还是下半部分
     * @return 1：上 2：下
     */
    private int isUpOrDown(ChessMove move){
        if (move.getChess().getInitP().y < 6){
            // 上面
            return 1;
        }else {
            // 下面
            return 2;
        }
    }

    /**
     * 判断移动是否在王宫范围内
     * @param move 封装类move
     * @return
     */
    protected boolean isInPalace(ChessMove move){
        if (move.getTo().x < 4 || move.getTo().x > 6){
            return false;
        }
        int flag = isUpOrDown(move);
        if (flag == 1){
            // 上面
            if (move.getTo().y < 1 || move.getTo().y > 3){
                return false;
            }
        }else if (flag == 2){
            // 下面
            if (move.getTo().y < 8 || move.getTo().y > 10){
                return false;
            }
        }

        return true;
    }

    /**
     * 判断位置是否过河 看起点版
     * @param move 封装类move
     * @return
     */
    protected boolean isCrossRiver1(ChessMove move){
        int flag = isUpOrDown(move);
        if (flag == 1){
            // 起始在上面
            return move.getFrom().y > 5;
        }else {
            // 起始在下面
            return move.getFrom().y <= 5;
        }
    }

    /**
     * 判断移动是否过河 看终点版
     * @param move 封装类move
     * @return
     */
    protected boolean isCrossRiver2(ChessMove move){
        int flag = isUpOrDown(move);
        if (flag == 1){
            // 起始在上面
            return move.getTo().y > 5;
        }else {
            // 起始在下面
            return move.getTo().y <= 5;
        }
    }

    /**
     * 判断是否被蹩脚
     * @param move 封装类move
     * @return
     */
    protected boolean isObstructed(ChessMove move){
        Point obstructP = new Point();
        switch (move.getChess().getName()){
            case "xiang" ->{
                obstructP.x = (move.getFrom().x + move.getTo().x)/2;
                obstructP.y = (move.getFrom().y + move.getTo().y)/2;
                return getChessFromP(obstructP.x, obstructP.y) != null;

            }
            case "ma" ->{
                int flag = line(move);
                if (flag == 0){
                    obstructP.y = (move.getFrom().y + move.getTo().y)/2;
                    obstructP.x = move.getFrom().x;
                    return getChessFromP(obstructP.x, obstructP.y) != null;
                }else if (flag == -1){
                    obstructP.x = (move.getFrom().x + move.getTo().x)/2;
                    obstructP.y = move.getFrom().y;
                    return getChessFromP(obstructP.x, obstructP.y) != null;
                }
            }
        }
        return false;
    }

    /**
     * 计算起点到移动目标点之间的棋子个数 不包括起点和终点 (在移动逻辑内，终点必无棋子，故不用考虑，而起点必有自身）
     * @param move 封装类move
     * @return 棋子个数
     */
    protected int getCount(ChessMove move){
        int count = 0;
        int start = 0;
        int end = 0;
        Point tempP = new Point();//用来筛查中间点是否存在棋子的临时变量
        int flag = line(move);
        if (flag == 2) {
            //上下走
            tempP.x = move.getFrom().x;
            if (move.getTo().y > move.getFrom().y){
                //从上往下走
                start = move.getFrom().y + 1;
                end = move.getTo().y;
            }else {
                //从右往左走
                start = move.getTo().y + 1;
                end = move.getFrom().y;
            }
            for (int i = start;i < end;i++){
                tempP.y = i;
                if (getChessFromP(tempP.x, tempP.y) != null){
                    count++;
                }
            }
        }else if (flag == 3){
            //左右走
            tempP.y = move.getFrom().y;
            if (move.getTo().x > move.getFrom().x){
                //从上往下走
                start = move.getFrom().x + 1;
                end = move.getTo().x;
            }else {
                //从下往上走
                start = move.getTo().x+ 1;
                end = move.getFrom().x;
            }
            for (int i = start;i < end;i++){
                tempP.x = i;
                if (getChessFromP(tempP.x, tempP.y) != null){
                    count++;
                }
            }
        }
        return count;
    }




    /**
     * 判断棋子的移动是否为前进
     * @param move 封装类move
     * @return
     */
    protected boolean isForward(ChessMove move){
        int flag = isUpOrDown(move);
        if (flag == 1 && move.getTo().y > move.getFrom().y){
            //上面
            return true;
        }else if (flag == 2 && move.getTo().y < move.getFrom().y){
            //下面
            return true;
        }
        return false;
    }

    /**
     * 判断棋子的移动是否为后退
     * @param move 封装类move
     * @return
     */
    protected boolean isBackward(ChessMove move){
        int flag = isUpOrDown(move);
        if (flag == 1 && move.getTo().y < move.getFrom().y){
            //上面
            return true;
        }else if (flag == 2 && move.getTo().y > move.getFrom().y){
            //下面
            return true;
        }
        return false;
    }

    /**
     * 判断移动是直线还是斜线还是日字或者都不是
     * @param move 封装类move
     * @return 1：正斜线, 2：沿y轴, 3：沿x轴, 0：y的日字, -1；x的日字, -2：都不是
     */
    protected int line(ChessMove move){
        if (Math.abs(move.getFrom().y - move.getTo().y) == Math.abs(move.getFrom().x - move.getTo().x)){
            //正斜线
            return 1;
        }else if (move.getFrom().x == move.getTo().x){
            //y
            return 2;
        }else if (move.getFrom().y == move.getTo().y){
            //x
            return 3;
        }else if ((Math.abs(move.getFrom().y - move.getTo().y) == 2 && Math.abs(move.getFrom().x - move.getTo().x) == 1)){
            //y的日字
            return 0;
        }else if (Math.abs(move.getFrom().y - move.getTo().y) == 1 && Math.abs(move.getFrom().x - move.getTo().x) == 2){
            //x的日字
            return -1;
        }else {
            return -2;
        }

    }
    //清空棋盘
    public void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = null;
            }
        }
    }

    // 放置棋子
    public void placeChess(int x, int y, Chess chess) {
        board[x][y] = chess;
        chess.setP(chess.getP());
    }



}
