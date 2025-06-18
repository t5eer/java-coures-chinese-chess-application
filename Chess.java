package src.ChineseChess.v4_2;

import java.awt.*;
import java.io.File;

public class Chess {

    // 颜色 0为红 1为黑
    private int color;
    // 棋子的网格坐标
    private Point p;
    // 记录棋子的初始网格坐标
    private  Point initP;
    // 是啥棋子
    private String name;

    private final String suffix = ".png";

    public Chess(String name, int color, Point p,Point initP){
        this.name = name;
        this.color = color;
        this.p = p;
        this.initP = initP;
    }

    public Chess(String name, int color, Point p){
        this.name = name;
        this.color = color;
        this.p = p;
    }

    public Chess(String name, int color){
        this.name = name;
        this.color = color;
    }

    public Chess(){

    }

    @Override
    public String toString() {
        return "Chess{" +
                "color=" + color +
                ", p=" + p +
                ", initP=" + initP +
                ", name='" + name + '\'' +
                ", suffix='" + suffix + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getP() {
        return p;
    }

    public void setP(Point p) {

        this.p = new Point(p);

//        calXY(p);//当即将网格坐标改为绘制坐标
    }

    public Point getInitP() {
        return initP;
    }

    public void setInitP(Point initP) {
        this.initP = initP;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }




    public boolean isValidMove(ChessMove move){
        Point p = new Point();
        Point tp = new Point();
        p.x = move.getFrom().x;
        p.y = move.getFrom().y;
        tp.x = move.getTo().x;
        tp.y = move.getTo().y;
        switch (name) {
            case "che" -> {
                if(p.x == tp.x || p.y == tp.y)
                    return true;
                else return false;

            }
            case "ma" -> {
                if(Math.abs(p.x-tp.x) == 2 && Math.abs(p.y-tp.y) == 1)
                    return true;
                else if (Math.abs(p.x-tp.x) == 1 && Math.abs(p.y-tp.y) == 2) {
                    return true;
                }
                else return false;

            }
            case "xiang" -> {
                if(Math.abs(p.x-tp.x) == 2 &&Math.abs(p.y-tp.y) == 2)
                    return true;
                else return false;

            }
            case "shi" -> {
                if(Math.abs(p.x-tp.x)==1&&Math.abs(p.y-tp.y)==1)
                    return true;
                else return false;

            }
            case "boss" -> {
                if((Math.abs(p.x-tp.x)==1&&Math.abs(p.y-tp.y)==0)||((Math.abs(p.x-tp.x)==0&&Math.abs(p.y-tp.y)==1)))
                    return true;
                else return false;

            }
            case "pao" -> {
                if(p.x==tp.x||p.y==tp.y)
                    return true;
                else return false;
            }
            case "bing" -> {
                int dx = Math.abs(tp.x - p.x);
                int dy = Math.abs(tp.y - p.y);
                // 移动总步长必须为1
                if (dx + dy != 1) {
                    return false;
                }
                if(p.x==tp.x||p.y==tp.y)
                    return true;
                else return false;
            }
        }

        return false;
    }

    // 将棋子画在面板上的方法
    public Image getImage(){
        String path = "pic" + File.separator + name + color + suffix;

        return Toolkit.getDefaultToolkit().getImage(path);
    }


    public Point reverse(Point p){
        return new Point(10 - p.x,11 - p.y);
    }


}
