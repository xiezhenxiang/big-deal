package indi.shine.stock.strategy;/*
package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.BuyPoint;
import indi.shine.stock.bean.po.StockLineDay;

import java.util.List;

import static indi.shine.stock.bean.Constant.TRADE_DAYS_OF_MONTH;
import static indi.shine.stock.bean.Constant.TRADE_DAYS_OF_YEAR;

*/
/**
 * @author xiezhenxiang 2023/5/16
 *//*

public class TradeTurnoverStrategy implements Strategy {

    private int horizontalDays = TRADE_DAYS_OF_MONTH * 3 - 5;

    public static void main(String[] args) {
        new TradeTurnoverStrategy().run();
    }

    @Override
    public void getBuyPoint(String code, List<StockLineDay> lineDays) {
        String buyPoint = null;
        int cnt = 0;
        long totalCoin = 0;
        for (int i = lineDays.size() - TRADE_DAYS_OF_MONTH; i >= lineDays.size() - TRADE_DAYS_OF_YEAR - TRADE_DAYS_OF_MONTH; i--) {
            StockLineDay lineDay = lineDays.get(i);
            Long volCoin = lineDay.getVolCoin();
            if (volCoin < 200000000) {
                totalCoin += volCoin;
                if (buyPoint != null && ++cnt >= horizontalDays) {
                    synchronized (TradeTurnoverStrategy.class) {
                        BUY_POINTS.add(new BuyPoint(code, buyPoint, totalCoin / horizontalDays));
                        return;
                    }
                }
                continue;
            }
            buyPoint = lineDay.getChg() > 6 || lineDay.getChg() <= 0 ? null : lineDay.getDay();
            cnt = 0;
            totalCoin = 0;
        }
    }
}
*/
