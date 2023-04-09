package indi.shine.stock.strategy;

import ai.plantdata.script.util.other.ThreadUtil;
import indi.shine.stock.bean.CodeHammer;
import indi.shine.stock.bean.po.StockLineDay;

import java.util.ArrayList;
import java.util.List;

import static indi.shine.stock.bean.Constant.TRADE_DAYS_OF_YEAR;
import static indi.shine.stock.crawler.StockHistoryCrawler.allStockCodes;
import static indi.shine.stock.crawler.StockHistoryCrawler.stockLineDays;

/**
 * @author xiezhenxiang 2023/4/7
 */
public class HammerStrategy {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ThreadUtil threadUtil = new ThreadUtil(15, 10);
        List<CodeHammer> ls = new ArrayList<>();
        for (String code : allStockCodes()) {
            threadUtil.execute(() -> {
                List<StockLineDay> lineDays = stockLineDays(code, false);
                if (lineDays.size() > TRADE_DAYS_OF_YEAR) {
                    StockLineDay lineDay = lineDays.get(lineDays.size() - 1);
                    double candleLen = Math.abs(lineDay.openPrice - lineDay.price);
                    double minCandle = Math.min(lineDay.openPrice, lineDay.price);
                    double downShadowLen = Math.abs(lineDay.minPrice - minCandle);
                    if (candleLen != 0 && downShadowLen != 0 && downShadowLen >= candleLen * 2) {
                        double times = downShadowLen / candleLen;
                        ls.add(new CodeHammer(code, times));
                    }
                }
            });
        }
        threadUtil.closeWithSafe();
        long end = System.currentTimeMillis();
        System.out.println("cost: " + ((end -start) / 1000 * 1.0 / 60) + "min");
        ls.sort((a, b) -> {
            if (a.times == b.times) {
                return 0;
            }
            return a.times > b.times ? -1 : 1;
        });
        System.out.println(ls.size());
    }
}
