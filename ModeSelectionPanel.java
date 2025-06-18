package src.ChineseChess.v4_2;

import javax.swing.*;
import java.awt.*;

public class ModeSelectionPanel extends JDialog {
    private boolean isAiEnabled = false;
    private boolean isRedPlayer = true;
    private boolean isConfirmed = false;

    private JRadioButton rbAI, rb2P, redBtn, blackBtn;

    public ModeSelectionPanel(JFrame parent) {
        super(parent, "选择游戏模式", true); // 模态对话框
        setSize(400, 250);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // ======= 顶部区域：选择模式 =======
        JPanel modePanel = new JPanel(new FlowLayout());
        rbAI = new JRadioButton("人机对战");
        rb2P = new JRadioButton("双人对战");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(rbAI);
        modeGroup.add(rb2P);
        modePanel.add(rbAI);
        modePanel.add(rb2P);
        add(modePanel, BorderLayout.NORTH);

        // ======= 中部区域：执棋方选择，仅对AI模式可见 =======
        JPanel sidePanel = new JPanel(new FlowLayout());
        JLabel sideLabel = new JLabel("你执：");
        redBtn = new JRadioButton("红棋");
        blackBtn = new JRadioButton("黑棋");
        ButtonGroup sideGroup = new ButtonGroup();
        sideGroup.add(redBtn);
        sideGroup.add(blackBtn);
        sidePanel.add(sideLabel);
        sidePanel.add(redBtn);
        sidePanel.add(blackBtn);
        sidePanel.setVisible(false); // 默认不显示
        add(sidePanel, BorderLayout.CENTER);

        // 模式监听器
        rbAI.addActionListener(e -> sidePanel.setVisible(true));
        rb2P.addActionListener(e -> sidePanel.setVisible(false));

        // ======= 底部：确认取消 =======
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 确认按钮逻辑
        confirmButton.addActionListener(e -> {
            if (!rbAI.isSelected() && !rb2P.isSelected()) {
                JOptionPane.showMessageDialog(this, "请选择游戏模式");
                return;
            }
            if (rbAI.isSelected()) {
                if (!redBtn.isSelected() && !blackBtn.isSelected()) {
                    JOptionPane.showMessageDialog(this, "请选择执棋方！");
                    return;
                }
                isAiEnabled = true;
                isRedPlayer = redBtn.isSelected();
            } else {
                isAiEnabled = false;
            }
            isConfirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    public boolean isAIEnabled() {
        return isAiEnabled;
    }

    public boolean isRedPlayer() {
        return isRedPlayer;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}
