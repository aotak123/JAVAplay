package 五子棋;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FiveChessFrame extends JFrame implements MouseListener, Runnable {
    // 取得屏幕大小，屏幕的高度和宽度
    int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    // 背景图片
    BufferedImage bgImage = null;
    // 用来保存棋子的坐标
    int x = 0;
    int y = 0;
    // 保存之前下过的全部棋子的坐标
    // 0:表示这个点并没有棋子 1：表示这个点是黑子 2：表示这个点是白子
    int[][] allChess = new int[19][19];
    // 标识当前应该是黑棋还是白棋下下一步
    boolean isBlack = true;
    // 标识当前游戏是否可以继续
    boolean canPlay = true;
    // 保存显示的提示信息
    String message = "黑方先行";
    // 保存最多拥有多少时间(秒)
    int maxTime = 0;
    // 做倒计时的线程类
    Thread t = new Thread(this);
    // 保存黑方与白方的剩余时间
    int blackTime = 0;
    int whiteTime = 0;
    // 保存双方剩余时间的显示信息
    String blackMessage = "无限制";
    String whiteMessage = "无限制";

    public FiveChessFrame() {
        // 设置标题
        this.setTitle("五子棋");
        // 设置窗体大小
        this.setSize(500, 530);
        // 设置窗体出现位置
        this.setLocation((width - 500) / 2, (height - 500) / 2);
        // 将窗体设置为大小不可变
        this.setResizable(false);
        // 将窗体的关闭方式设置为默认关闭后程序结束
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 为窗体加入监听器
        this.addMouseListener(this);
        // 将窗体显示出来
        this.setVisible(true);

        t.start();
        t.suspend();

        // 将背景图片显示到窗体中
//        try {
//            bgImage = ImageIO.read(new File("e:/Image/Five.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void paint(Graphics g) {
        // 双缓冲技术防止屏幕闪烁
        BufferedImage bi = new BufferedImage(500, 530, BufferedImage.TYPE_INT_ARGB);
        Graphics g2 = bi.createGraphics();
        g2.setColor(Color.BLACK);
        // 绘制背景
        g2.drawImage(bgImage, 0, 30, this);
        // 输出标题信息
        g2.setFont(new Font("黑体", Font.BOLD, 14));
        g2.drawString("游戏信息:" + message, 10, 50);
        // 输出时间信息
        g2.setFont(new Font("宋体", 0, 17));
        g2.drawString("黑方时间：" + blackMessage, 60, 490);
        g2.drawString("白方时间：" + whiteMessage, 290, 490);
        // 绘制棋盘
        for (int i = 0; i < 19; i++) {
            g2.drawLine(21, 84 + 20 * i, 377, 84 + 20 * i);
            g2.drawLine(20 + 20 * i, 85, 20 + 20 * i, 441);
        }
        // 标注点位
        g2.fillOval(78, 140, 6, 6);
        g2.fillOval(317, 140, 6, 6);
        g2.fillOval(317, 380, 6, 6);
        g2.fillOval(78, 380, 6, 6);
        g2.fillOval(198, 261, 6, 6);

        // 绘制全部棋子
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (allChess[i][j] == 1) {
                    // 黑子
                    int tempX = i * 20 + 21;
                    int tempY = j * 20 + 84;
                    g2.fillOval(tempX - 7, tempY - 7, 14, 14);

                }
                if (allChess[i][j] == 2) {
                    // 白子
                    int tempX = i * 20 + 21;
                    int tempY = j * 20 + 84;
                    g2.setColor(Color.WHITE);
                    g2.fillOval(tempX - 7, tempY - 7, 14, 14);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(tempX - 7, tempY - 7, 14, 14);
                }
            }
        }
        g.drawImage(bi, 0, 0, this);
    }

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        // 利用点击监听操作得到各个点的坐标
        if (canPlay == true) {
            x = e.getX();
            y = e.getY();
            if (x >= 20 && x <= 377 && y >= 84 && y <= 441) {
                x = (x - 20) / 20;
                y = (y - 84) / 20;
                if (allChess[x][y] == 0) {
                    // 判断当前要下的是什么演的的棋子
                    if (isBlack == true) {
                        allChess[x][y] = 1;
                        isBlack = false;
                        message = "轮到白方";
                    } else {
                        allChess[x][y] = 2;
                        isBlack = true;
                        message = "轮到黑方";
                    }
                    // 判断这个棋子是否和其他的棋子连成5连，即判断游戏是否结束
                    boolean winFlag = this.checkWin();
                    if (winFlag == true) {
                        JOptionPane.showMessageDialog(this, "游戏结束，" + (allChess[x][y] == 1 ? "黑方" : "白方") + "获胜！");
                        canPlay = false;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "当前位置已经有棋子，请重新落子！");
                }
                this.repaint();
            }
        }
        // 点击开始游戏按钮
        if (e.getX() >= 400 && e.getX() <= 480 && e.getY() >= 87 && e.getY() <= 122) {
            int result = JOptionPane.showConfirmDialog(this, "是否重新开始游戏？");
            if (result == 0) {
                // 现在重新开始游戏
                // 重新开始所要做的操作：
                // 1、把棋盘清空，将allChess这个数组中的全部数据归0；
                // 2、将游戏信息：的显示改回到开始位置
                // 3、将下一步下棋的人改为黑方
                for (int i = 0; i < 19; i++) {
                    for (int j = 0; j < 19; j++) {
                        allChess[i][j] = 0;
                    }
                }
                message = "黑方先行";
                isBlack = true;
                blackTime = maxTime;
                whiteTime = maxTime;
                if (maxTime > 0) {
                    blackMessage = maxTime / 3600 + ":" + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                            + (maxTime - maxTime / 60 * 60);
                    whiteMessage = maxTime / 3600 + ":" + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                            + (maxTime - maxTime / 60 * 60);
                    t.resume();
                } else {
                    blackMessage = "无限制";
                    whiteMessage = "无限制";
                }
                this.canPlay = true;
                this.repaint();
            }
        }
        // 点击游戏设置按钮
        if (e.getX() >= 400 && e.getX() <= 480 && e.getY() >= 150 && e.getY() <= 180) {
            String input = JOptionPane.showInputDialog("请输入游戏的最大时间（单位：分钟），如果输入0表示没有时间限制");
            try {
                maxTime = Integer.parseInt(input) * 60;
                if (maxTime < 0) {
                    JOptionPane.showMessageDialog(this, "请输入正确信息，不允许输入负数");
                }
                if (maxTime == 0) {
                    int result = JOptionPane.showConfirmDialog(this, "设置完成，是否重新开始游戏？");
                    if (result == 0) {
                        for (int i = 0; i < 19; i++) {
                            for (int j = 0; j < 19; j++) {
                                allChess[i][j] = 0;
                            }
                        }
                        message = "黑方先行";
                        isBlack = true;
                        blackTime = maxTime;
                        whiteTime = maxTime;
                        blackMessage = "无限制";
                        whiteMessage = "无限制";
                        this.canPlay = true;
                        this.repaint();
                    }
                }
                if (maxTime > 0) {
                    int result = JOptionPane.showConfirmDialog(this, "设置完成，是否重新开始游戏？");
                    if (result == 0) {
                        for (int i = 0; i < 19; i++) {
                            for (int j = 0; j < 19; j++) {
                                allChess[i][j] = 0;
                            }
                        }
                        message = "黑方先行";
                        isBlack = true;
                        blackTime = maxTime;
                        whiteTime = maxTime;
                        blackMessage = maxTime / 3600 + ":" + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                                + (maxTime - maxTime / 60 * 60);
                        whiteMessage = maxTime / 3600 + ":" + (maxTime / 60 - maxTime / 3600 * 60) + ":"
                                + (maxTime - maxTime / 60 * 60);
                        t.resume();
                        this.canPlay = true;
                        this.repaint();
                    }
                }
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(this, "请输入大于等于零的数字");
                e1.printStackTrace();
            }
        }
        // 点击游戏说明按钮
        if (e.getX() >= 400 && e.getX() <= 480 && e.getY() >= 210 && e.getY() <= 245) {
            JOptionPane.showMessageDialog(this, "这是一个五子棋游戏程序，黑白双方轮流下棋，当某一方连到五子时游戏结束。");
        }
        // 点击认输按钮
        if (e.getX() >= 400 && e.getX() <= 480 && e.getY() >= 280 && e.getY() <= 310) {
            int result = JOptionPane.showConfirmDialog(this, "是否确认认输");
            if (result == 0) {
                if (isBlack) {
                    JOptionPane.showMessageDialog(this, "黑方已经认输，游戏结束");
                } else {
                    JOptionPane.showMessageDialog(this, "白方已经认输，游戏结束");
                }
                canPlay = false;
            }
        }
        // 点击关于按钮
        if (e.getX() >= 400 && e.getX() <= 480 && e.getY() >= 340 && e.getY() <= 370) {
            JOptionPane.showMessageDialog(this, "本游戏由邢洪浩制作，有相关问题可以加微信：xhh19980926");
        }
        // 点击退出按钮
        if (e.getX() >= 400 && e.getX() <= 480 && e.getY() >= 400 && e.getY() <= 427) {
            JOptionPane.showMessageDialog(this, "游戏结束");
            System.exit(0);
        }
        x = (x - 20) / 20;
        y = (y - 84) / 20;
    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public boolean checkWin() {
        boolean flag = false;
        // 保存共有相同颜色多少棋子相连
        int count = 1;
        // 判断横向的是否有5个棋子相连，特点 纵坐标是相同的，即allChess[x][y]中y值是相同的
        int color = allChess[x][y];
        // 判断横向
        count = this.checkCount(1, 0, color);
        if (count >= 5) {
            flag = true;
        } else {
            // 判断纵向
            count = this.checkCount(0, 1, color);
            if (count >= 5) {
                flag = true;
            } else {
                // 判断右上、左下
                count = this.checkCount(1, -1, color);
                if (count >= 5) {
                    flag = true;
                } else {
                    // 判断右下、左上
                    count = this.checkCount(1, 1, color);
                    if (count >= 5) {
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    // 判断棋子连接的数量
    private int checkCount(int xChange, int yChange, int color) {
        int count = 1;
        int tempX = xChange;
        int tempY = yChange;
        while (x + xChange >= 0 && x + xChange <= 18 && y + yChange >= 0 && y + yChange <= 18
                && color == allChess[x + xChange][y + yChange]) {
            count++;
            if (xChange != 0)
                xChange++;
            if (yChange != 0) {
                if (yChange > 0)
                    yChange++;
                else {
                    yChange--;
                }
            }
        }
        xChange = tempX;
        yChange = tempY;
        while (x - xChange >= 0 && x - xChange <= 18 && y - yChange >= 0 && y - yChange <= 18
                && color == allChess[x - xChange][y - yChange]) {
            count++;
            if (xChange != 0)
                xChange++;
            if (yChange != 0) {
                if (yChange > 0)
                    yChange++;
                else {
                    yChange--;
                }
            }
        }
        return count;
    }

    public void run() {
        // 判断是否有时间限制
        if (maxTime > 0) {
            while (true) {
                if (isBlack) {
                    blackTime--;
                    if (blackTime == 0) {
                        JOptionPane.showMessageDialog(this, "黑方超时,游戏结束!");
                    }
                } else {
                    whiteTime--;
                    if (whiteTime == 0) {
                        JOptionPane.showMessageDialog(this, "白方超时,游戏结束!");
                    }
                }
                blackMessage = blackTime / 3600 + ":" + (blackTime / 60 - blackTime / 3600 * 60) + ":"
                        + (blackTime - blackTime / 60 * 60);
                whiteMessage = whiteTime / 3600 + ":" + (whiteTime / 60 - whiteTime / 3600 * 60) + ":"
                        + (whiteTime - whiteTime / 60 * 60);
                this.repaint();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(blackTime + " -- " + whiteTime);
            }
        }
    }
}
