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
        ThreadUtil threadUtil = new ThreadUtil(15, 10);
        for (String code : allStockCodes()) {
            threadUtil.execute(() -> {
                List<StockLineDay> lineDays = stockLineDays(code, false);
                if (check(code, lineDays)) {
                    //System.out.println(code);
                }
            });
        }
        threadUtil.closeWithSafe();
        long end = System.currentTimeMillis();
        System.out.println("cost: " + ((end -start) / 1000 * 1.0 / 60) + "min");
    }

    private static boolean check(String code, List<StockLineDay> lineDays) {
        int tryDistance = TRADE_DAYS_OF_MONTH;
        if (lineDays.size() < TRADE_DAYS_OF_YEAR) {
            return false;
        }
        // 寻找拐点
        for (int i = lineDays.size() - 1; i >= lineDays.size() - tryDistance; i--) {
            if (isTurnPoint(code, lineDays, i)) {
                break;
            }
        }
        return false;
    }
    
    /** 是否拐点 */
    private static boolean isTurnPoint(String code, List<StockLineDay> lineDays, int index) {
        String day = lineDays.get(index).getDay();
        int tryDistance = TRADE_DAYS_OF_MONTH * 2;
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
        System.out.println("code: " + code + " have turn point at " + day + " chg: " + chg);
        return true;
    }
}
