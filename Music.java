package src.ChineseChess.v4_2;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Music {
    public static void playMusic(File file) {
        System.out.println("尝试播放文件: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("错误: 文件不存在!");
            return;
        }

        try {
            // 使用BufferedInputStream提高性能
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            Player player = new Player(bis);

            // 播放音乐并捕获播放完成事件
            new Thread(() -> {
                try {
                    System.out.println("开始播放音乐...");
                    player.play();
                    System.out.println("音乐播放完成!");
                } catch (JavaLayerException e) {
                    System.out.println("播放错误: " + e.getMessage());
                }
            }).start();

//            // 让主线程等待一段时间，确保音乐能播放
//            try {
//                Thread.sleep(1000); // 等待10秒，根据音乐长度调整
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

        } catch (FileNotFoundException e) {
            System.out.println("文件未找到: " + e.getMessage());
        } catch (JavaLayerException e) {
            System.out.println("解码错误: " + e.getMessage());
        }
    }
}
