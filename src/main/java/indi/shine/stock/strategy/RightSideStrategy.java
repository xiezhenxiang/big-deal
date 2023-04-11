package indi.shine.stock.strategy;

import ai.plantdata.script.util.other.ThreadUtil;
import indi.shine.stock.bean.po.StockLineDay;
import org.apache.kafka.common.metrics.stats.Min;

import java.util.List;

import static indi.shine.stock.bean.Constant.TRADE_DAYS_OF_MONTH;
import static indi.shine.stock.bean.Constant.TRADE_DAYS_OF_YEAR;
import static indi.shine.stock.crawler.StockHistoryCrawler.allStockCodes;
import static indi.shine.stock.crawler.StockHistoryCrawler.stockLineDays;

/**
 * @author xiezhenxiang 2023/3/23
 */
public class RightSideStrategy {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ThreadUtil threadUtil = new ThreadUtil(10, 10);
        for (String code : allStockCodes()) {
            threadUtil.execute(() -> {
                List<StockLineDay> lineDays = stockLineDays(code, false);
                if (lineDays.size() > TRADE_DAYS_OF_YEAR * 2 && check(code, lineDays)) {
                    //System.out.println(code);
                }
            });
        }
        threadUtil.closeWithSafe();
        long end = System.currentTimeMillis();
        System.out.println("cost: " + ((end -start) / 1000 * 1.0 / 60) + "min");
    }

    private static boolean check(String code, List<StockLineDay> lineDays) {

        int start = lineDays.size() - TRADE_DAYS_OF_MONTH * 2;
        Double startPrice = lineDays.get(start).getPrice();
        Double endPrice = lineDays.get(lineDays.size() - 1).getPrice();
        double sf = (endPrice - startPrice) / startPrice;
        if (sf < 0.15) {
            return false;
        }
        double[] startPoint = {0, startPrice}, endPoint = {TRADE_DAYS_OF_MONTH * 2, endPrice};
        int x = 0;
        double sum = 0;
        for(int i = start; i < lineDays.size(); i ++) {
            sum += shortestDistanceToLine(startPoint, endPoint, new double[]{x, lineDays.get(i).getPrice()});
            x ++;
        }
        double avg = sum / x;
        if (code.equals("000063")) {
            System.out.println("1111");
        }
        System.out.println(code + " " + sf + " " + avg);
        return false;
    }

    public static double shortestDistanceToLine(double[] pointA, double[] pointB, double[] pointC) {
        // 计算直线方程系数
        double a = pointB[1] - pointA[1];
        double b = pointA[0] - pointB[0];
        double c = pointB[0] * pointA[1] - pointA[0] * pointB[1];
        // 计算点C到直线的距离
        return Math.abs(a * pointC[0] + b * pointC[1] + c) / Math.sqrt(a * a + b * b);
    }
    
    /** 是否左侧拐点 */
    private static boolean isLeftTurnPoint(String code, List<StockLineDay> lineDays, int index) {
        String day = lineDays.get(index).getDay();
        int tryDistance = TRADE_DAYS_OF_MONTH * 3;
        Double lastPrice = lineDays.get(index).getPrice(), startPrice = lastPrice;
        Double price;
        for (; index >= lineDays.size() - tryDistance; index -= 5) {
            price = lineDays.get(index).getPrice();
            if (price < lastPrice) {
                return false;
            }
            lastPrice = price;
        }
        double chg = (startPrice - lastPrice) % lastPrice;
        if (Math.abs(chg) < 20) {
            return false;
        }
        return true;
    }

    /** 是否右侧拐点 */
    private static boolean isRightTurnPoint(String code, List<StockLineDay> lineDays, int index) {
        String day = lineDays.get(index).getDay();
        Double lastPrice = lineDays.get(index).getPrice(), startPrice = lastPrice;
        Double price;
        for (; index < lineDays.size(); index += 5) {
            price = lineDays.get(index).getPrice();
            if (price < lastPrice) {
                return false;
            }
            lastPrice = price;
        }
        double chg = (lastPrice - startPrice) % lastPrice;
        if (Math.abs(chg) < 10) {
            return false;
        }
        System.out.println("code: " + code + " have right turn point at " + day + " chg: " + chg);
        return true;
    }
}
