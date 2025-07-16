package indi.shine.stock.strategy;/*
package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.BuyPoint;
import indi.shine.stock.bean.po.DayKline;

import java.util.List;

*/
/**
 * @author xiezhenxiang 2023/5/19
 *//*

public class ContinueDeclineStrategy implements Strategy {

    public static void main(String[] args) {
        new ContinueDeclineStrategy().run();
    }

    @Override
    public void getBuyPoint(String code, List<DayKline> lineDays) {
        boolean flag = true;
        for (int i = lineDays.size() - 20; i < lineDays.size() - 2; i++) {
            DayKline lineDay = lineDays.get(i);
            if (lineDay.getChg() > 0) {
                flag = false;
                break;
            }
        }
        if (flag) {
            BUY_POINTS.add(new BuyPoint(code, lineDays.get(lineDays.size() - 1).getDay(), 0));
        }
    }
}
*/
