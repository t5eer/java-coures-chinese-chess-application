package src.ChineseChess.v4_2;
/*
这一版本较上一版本优化了：
1、ai计算的中间过程不再会显示在棋盘上
2、ai一般不会不再应将了，且将死后ai不会再下棋
3、发现copy棋盘中将帅引用有问题，并且吃掉将帅后会真的将redBoss和blackBoss赋为null，使isGameOver方法开始有效
 */

import javax.swing.*;
import java.awt.*;



public class MainFrame extends JFrame{
    protected GameController controller;
    protected GamePanel gamePanel;
    protected Chessboard chessboard;
    private ChessAI ai;
    public boolean isRedPlayer;

    public MainFrame(){
        // 初始化 MVC 组件
        this.chessboard = new Chessboard();
        this.chessboard.initChess();

        this.gamePanel = new GamePanel();
        this.controller = new GameController(this.chessboard, this.gamePanel,this);
        this.gamePanel.setController(this.controller);

        // 设置窗口属性
        setTitle("中国象棋");
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 设置菜单和布局
        setupMenu();
        setupLayout();

        setVisible(true);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("游戏");

        JMenuItem modeItem = new JMenuItem("游戏模式");
        JMenuItem saveItem = new JMenuItem("保存游戏");
        JMenuItem loadItem = new JMenuItem("导入游戏");


        gameMenu.add(modeItem);
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);

        modeItem.addActionListener(e -> {
            controller.handleSet();
        });
        saveItem.addActionListener(e -> {
            controller.saveChessGame();
        });
        loadItem.addActionListener(e -> {
            controller.loadChessGame();
        });

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }


    private void setupLayout() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(gamePanel, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(200, 0)); // 固定宽度
        sidePanel.setBorder(BorderFactory.createTitledBorder("功能栏"));


        JButton undoButton = new JButton("悔棋");
        sidePanel.add(undoButton);

        getContentPane().add(sidePanel, BorderLayout.EAST);
        undoButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        undoButton.addActionListener(e -> {
            controller.undoMove();
        });
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}

