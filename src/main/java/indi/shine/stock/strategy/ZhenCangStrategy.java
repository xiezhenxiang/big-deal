package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.BuyPoint;
import indi.shine.stock.bean.po.StockLineDay;

import java.util.List;

import static indi.shine.stock.crawler.StockHistoryCrawler.stockLineDays;

/**
 * @author xiezhenxiang 2023/6/2
 */
public class ZhenCangStrategy implements Strategy {

    public static void main(String[] args) {
        new ZhenCangStrategy().run();
    }

    @Override
    public void getBuyPoint(String code) {
        List<StockLineDay> lineDays = stockLineDays(code, false);
        lineDays = lineDays.subList(8, lineDays.size());
        StockLineDay day = lineDays.get(0);
        StockLineDay preDay = lineDays.get(1);
        if (day.getChg() < 1.8 || day.getChg() >= 5) {
            return;
        }
        if (preDay.getChg() < -7 || preDay.getChg() > -4) {
            return;
        }
        double score = preDay.getVol() * 1.0 / day.getVol();
        if (score < 1.5) {
            return;
        }
        BUY_POINTS.add(new BuyPoint(code, day.getDay(), score, day.getPrice()));
    }
}
