package indi.shine.stock.util;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * @author xiezhenxiang 2024/7/16
 */
public class RobotUtil {

    public static final Robot ROBOT;

    static {
        try {
            ROBOT = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }


    public static Color getPixelColor(int x, int y) {
        return ROBOT.getPixelColor(x, y);
    }

    // 点击鼠标左键
    public static void keyMaskLeft(int x, int y) {
        ROBOT.mouseMove(x, y);
        // 按下鼠标左键
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        // 松下鼠标左键
        ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 点击鼠标右键
    public static void keyMaskRight(int x, int y) {
        ROBOT.mouseMove(x, y);
        // 按下鼠标右键
        ROBOT.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        // 松下鼠标右键
        ROBOT.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
