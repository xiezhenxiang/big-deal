package indi.shine.stock.strategy;

import ai.plantdata.script.util.other.ThreadUtil;
import indi.shine.stock.bean.CodeHammer;
import indi.shine.stock.bean.po.BuyPoint;
import indi.shine.stock.bean.po.StockLineDay;

import java.util.ArrayList;
import java.util.List;

import static indi.shine.stock.bean.Constant.TRADE_DAYS_OF_YEAR;
import static indi.shine.stock.crawler.StockHistoryCrawler.allStockCodes;
import static indi.shine.stock.crawler.StockHistoryCrawler.stockLineDays;

/**
 * 多腿支撑策略
 * @author xiezhenxiang 2023/4/7
 */
public class HammerStrategy implements Strategy {

    @Override
    public void getBuyPoint(String code) {
        List<StockLineDay> lineDays = stockLineDays(code, false);
        StockLineDay lineDay = lineDays.get(0);
        double score = hammerScore(lineDay);
        if (score > 0) {
            BUY_POINTS.add(new BuyPoint(code, lineDay.getDay(), score, lineDay.getPrice()));
        }
    }

    public static void main(String[] args) {
        new HammerStrategy().run();
    }

    private static double hammerScore(StockLineDay lineDay) {
        double bodySize = Math.abs(lineDay.getOpenPrice() - lineDay.getPrice());
        double upperShadow = lineDay.getMaxPrice() - Math.max(lineDay.getOpenPrice(), lineDay.getPrice());
        double lowerShadow = Math.min(lineDay.getOpenPrice(), lineDay.getPrice()) - lineDay.getMinPrice();
        final double deep = Math.abs(lineDay.getOpenPrice() - lineDay.getMinPrice()) / lineDay.getOpenPrice();
        if (deep < 0.02 || lineDay.getChg() < 0) {
            return 0;
        }
        final double b1 = lowerShadow / bodySize;
        final double b2 = lowerShadow / upperShadow;
        if (b1 > 3 && b2 > 2) {
            return b1;
        }
        return 0;
    }
}
