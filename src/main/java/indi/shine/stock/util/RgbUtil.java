package indi.shine.stock.util;

import java.awt.*;

/**
 * @author xiezhenxiang 2024/7/15
 */
public class RgbUtil {

    public static boolean match(int x, int y, int redRgb, int greenRgb, int blueRgb) {
        Color color = RobotUtil.getPixelColor(x, y);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return Math.abs(red - redRgb) + Math.abs(green - greenRgb) + Math.abs(blue - blueRgb) < 30;
    }
}
