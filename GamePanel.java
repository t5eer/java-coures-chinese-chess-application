package src.ChineseChess.v4_2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class GamePanel extends JPanel {
    // 需高亮的坐标点
    private Point highlightedPoint;
    // 棋子的半径大小
    private static final int RADIUS = 30;
    // 棋盘的外边缘长度
    private static final int MARGIN = 40;
    // 棋子间的间距
    private static final int SPACING = 80;

    private Chessboard chessboard;// 从Model层获取数据

    public void setChessboard(Chessboard chessboard){
        this.chessboard = chessboard;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);

        // 给选中棋子打框
        if (highlightedPoint != null){
            drawSelectChessRect(highlightedPoint, g);
        }

        // 画提示：上步棋下的是啥
        int size = chessboard.getMoveHistory().size();
        if (size > 0){
            ChessMove lastMove = chessboard.getMoveHistory().get(size - 1);
            drawSelectChessRect(lastMove.getFrom(), g);
            drawSelectChessRect(lastMove.getTo(), g);
        }


    }

    private void drawBoard(Graphics g){
        // 要画图片的路径
        String bgPath = "pic" + File.separator + "qipan.jpg";
        // 创建默认Toolkit类的对象并根据路径获取图片
        Image bgimg = Toolkit.getDefaultToolkit().getImage(bgPath);
        // 将图片画再面板上
        g.drawImage(bgimg,0,0,725,800,this);
    }

    private void drawPieces(Graphics g){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 10; y++){
                Chess chess = chessboard.getChess(x, y);
                if (chess != null){
                    Image img = chess.getImage();
                    int screenX = MARGIN - RADIUS + SPACING * x;
                    int screenY = MARGIN - RADIUS + SPACING * y;
                    g.drawImage(img, screenX, screenY,2 * RADIUS,2 * RADIUS, this);
                }
            }
        }
    }

    // 画选择所选择棋子的边框 起提示作用
    public void drawSelectChessRect(Point p, Graphics g){
        int screenX = MARGIN - RADIUS + SPACING * (p.x - 1);
        int screenY = MARGIN - RADIUS + SPACING * (p.y - 1);
        g.drawRect(screenX, screenY,2 * RADIUS,2 * RADIUS);
    }

    // 由Controller调用，设置高亮位置
    public void highlightChess(Point point) {
        highlightedPoint = point;
        repaint();
    }

    // 清除高亮
    public void clearHighlight() {
        highlightedPoint = null;
        repaint();
    }

    // 事件处理（委托给Controller）
    public void setController(GameController controller){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Point p = getPointFromXY(e.getX(),e.getY());
                controller.handleClick(p);
            }
        });
    }

    // 静态方法又叫类方法 只能调用静态属性 调用时不仅可以用对象调用，也可直接用类名调用
    public static Point getPointFromXY(int x,int y){
        Point p = new Point();
        p.x = (x - MARGIN + RADIUS) / SPACING + 1;
        p.y = (y - MARGIN + RADIUS) / SPACING + 1;
        if (p.x<1 || p.x>9 || p.y<1 || p.y>10){
            return null;
        }
        return p;
    }


}
