package src.ChineseChess.v4_2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private Chessboard chessboard;
    private GamePanel gamePanel;
    private ChessAI AI;
    private ModeSelectionPanel modeSelectionPanel;
    private boolean aiEnabled = false;// 是否启用ai

    private MainFrame mainFrame;
    private Chess selectedChess; // 保存当前选中的棋子
    private Point fromPoint; // 帮助记录move的起点


    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public GameController(Chessboard chessboard, GamePanel gamePanel, MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.chessboard = chessboard;
        this.gamePanel = gamePanel;
        this.AI = new ChessAI(1, 4);
        gamePanel.setChessboard(chessboard); // View绑定Model
    }

    public void  setAiColor(boolean color) {
        if(color)
        {
            AI.setAiColor(1);
        }else
        {
            AI.setAiColor(0);
        }
    }

    //处理点击事件
    public void handleClick(Point cP) {
        System.out.println("用户点击了点（" + cP.getX() + "," + cP.getY() + ")");
        // 在玩家回合（非ai回合）才会执行
        if (chessboard.getCurrentPlayer() != AI.getAiColor() || !aiEnabled) {
            // 尚未拿起棋子时执行
            if (selectedChess == null) {
                selectedChess = chessboard.getChessFromP(cP.x, cP.y);
                fromPoint = cP;
                if (selectedChess != null) {
                    gamePanel.highlightChess(cP);
                }
                // 一方下棋时，只能选中己方的棋子
                if (selectedChess != null && selectedChess.getColor() != chessboard.getCurrentPlayer()) {
                    selectedChess = null;
                    fromPoint = null;
                    gamePanel.clearHighlight();
                }
            } else {
                // 已拿着一个棋子时执行
                Chess tc = chessboard.getChessFromP(cP.x, cP.y);
                ChessMove move = new ChessMove(selectedChess, fromPoint, cP, tc);
                if (tc != null && tc.getColor() == chessboard.getCurrentPlayer()) {
                    // 在重新选择
                    selectedChess = tc;
                    fromPoint = cP;
                    gamePanel.clearHighlight();
                    gamePanel.highlightChess(cP);
                    System.out.println("重新选择");

                } else {
                    // 在移动或是吃子
                    if (chessboard.isValidMove(move, selectedChess, false)) {
                        chessboard.applyMove(move, false);
                        System.out.println("移动/吃子！");

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        gamePanel.repaint();

                        // 到这里说明已放下这个棋子，清除选中状态
                        selectedChess = null;
                        fromPoint = null;
                        gamePanel.clearHighlight();
                    }
                }
            }
            // 检查是否轮到AI
            checkAITurn();
        }
    }

    // 添加AI启用/禁用方法
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;

    }


    protected void checkAITurn(){
        if (chessboard.getCurrentPlayer() == AI.getAiColor() && aiEnabled){
            startAIMove();
            System.out.println("start ai move");
        }
    }

    private void startAIMove(){
        new SwingWorker<Void, Void>(){

            @Override
            protected Void doInBackground() throws Exception {
                System.out.println("开始AI思考...");
                ChessMove aiMove = AI.findBestMove(chessboard);
                if (aiMove != null){
                    System.out.printf("ai认为最好的一步棋：%s(%d,%d)->(%d,%d)\n",
                            aiMove.getChess().getName(), aiMove.getFrom().x,aiMove.getFrom().y, aiMove.getTo().x, aiMove.getTo().y);
                    chessboard.applyMove(aiMove, false);
                } else {
                    System.out.println("AI未找到合法移动!");
                    setAIEnabled(false);
                    JOptionPane.showMessageDialog(mainFrame,"AI认输\n你赢了~","提示",JOptionPane.INFORMATION_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                gamePanel.repaint();
                super.done();
                // 移动后继续检测是否需要ai行动（双ai对下情形）
                checkAITurn();
            }
        }.execute();
    }




    protected void handleSet() {
        ModeSelectionPanel dialog = new ModeSelectionPanel(mainFrame);
        dialog.setVisible(true); // 阻塞直到关闭

        if (dialog.isConfirmed()) {
            if (dialog.isAIEnabled()) {
                setAIEnabled(true);
                mainFrame.isRedPlayer = dialog.isRedPlayer();
                restartGame(true);
            } else {
                setAIEnabled(false);
                restartGame(false);
            }
        }
    }


    public void restartGame(boolean enableAI) {
        // 从容器中移除旧的 gamePanel
        mainFrame.getContentPane().remove(gamePanel);

        // 重新创建 Model 和 View（推荐直接重新创建）
        Chessboard newchessboard = new Chessboard();
        newchessboard.initChess();

        GamePanel newgamePanel = new GamePanel();
        GameController newcontroller = new GameController(newchessboard, newgamePanel, mainFrame);
        newgamePanel.setController(newcontroller);
        newcontroller.setAIEnabled(enableAI);
        newcontroller.setAiColor(mainFrame.isRedPlayer);
        //this.chessboard.setCurrentPlayer(isRedPlayer);


        // 添加新的 gamePanel
        mainFrame.getContentPane().add(newgamePanel, BorderLayout.CENTER);

        mainFrame.chessboard = newchessboard;
        mainFrame.gamePanel = newgamePanel;
        mainFrame.controller = newcontroller;


        // 刷新 UI（重绘）
        mainFrame.revalidate(); // 通知布局管理器更新布局
        mainFrame.repaint();// 重新绘制界面
        mainFrame.controller.checkAITurn();
    }

    // 悔棋按键的实现
    public void undoMove() {
        int size = chessboard.getMoveHistory().size();
        if(aiEnabled)
        {

            chessboard.undoMove(chessboard.getMoveHistory().get(size - 1));
            size = chessboard.getMoveHistory().size();
            chessboard.undoMove(chessboard.getMoveHistory().get(size - 1));

        }else {
            chessboard.undoMove(chessboard.getMoveHistory().get(size - 1));
        }
        gamePanel.repaint();


    }


    //保存棋盘为Json文件
    public void saveChessGame()  {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile();
            try{
                List<Chess> datalist = new ArrayList<>();

                for(int i=0;i<9;i++)
                {
                    for(int j=0;j<10;j++)
                    {
                        Chess c = chessboard.getChess(i,j);
                        if(c!=null)
                        {
                            datalist.add(new Chess(c.getName(),c.getColor(),c.getP(),c.getInitP()));
                        }
                    }
                }

                SavedGame savedGame = new SavedGame();
                savedGame.setCurrentPlayer(chessboard.getCurrentPlayer());
                savedGame.setAiEanbled(isAiEnabled());
                savedGame.setMoveHistory(chessboard.getMoveHistory());
                savedGame.setRedBoss(chessboard.redBoss);
                savedGame.setBlackBoss(chessboard.blackBoss);
                savedGame.setChessList(datalist);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer=new FileWriter(file))
                {
                    gson.toJson(savedGame,writer);
                }
                JOptionPane.showMessageDialog(mainFrame, "棋局保存成功！");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame, "保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }

        }



    }

    //读取Json文件
    public void loadChessGame()
    {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(mainFrame);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile();
            try{
                Gson gson = new Gson();
                try(Reader reader = new FileReader(file)){
                    SavedGame savedGame = gson.fromJson(reader, SavedGame.class);
                    chessboard.clearBoard();
                    for(Chess data : savedGame.getChessList())
                    {
                        Chess c = new Chess(data.getName(),data.getColor(),data.getP(),data.getInitP());
                        chessboard.placeChess(data.getP().x-1,data.getP().y-1,c);
                        if (data.getName().equals("boss")) {
                            if (data.getColor() == 0) {
                                chessboard.redBoss = c;
                            } else {
                                chessboard.blackBoss = c;
                            }
                        }

                    }
                    chessboard.setCurrentPlayer(savedGame.getCurrentPlayer());
                    aiEnabled = savedGame.isAiEanbled();
                    chessboard.setMoveHistory(savedGame.getMoveHistory());

                    System.out.println(" ");
                    System.out.println("hasgfhgasdfgdgfdsghfgsdhifgihdgfhgdfgDGFYIdgifg");
                    System.out.println("redBoss is null? " + (chessboard.redBoss == null));
                    System.out.println("blackBoss is null? " + (chessboard.blackBoss == null));
                }
                gamePanel.repaint();
                JOptionPane.showMessageDialog(mainFrame, "棋局载入成功！");
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(mainFrame, "载入失败：" + ex.getMessage());
            }
        }

    }

}
