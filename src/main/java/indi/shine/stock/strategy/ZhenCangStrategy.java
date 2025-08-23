package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.BuyPoint;
import indi.shine.stock.bean.po.DayKline;

import java.util.List;

import static indi.shine.stock.common.biz.DataCenterBiz.dayKlines;

/**
 * @author xiezhenxiang 2023/6/2
 */
public class ZhenCangStrategy implements Strategy {

    public static void main(String[] args) {
        new ZhenCangStrategy().run();
    }

    @Override
    public void getBuyPoint(String code) {
        int crossDay = 10;
        List<DayKline> kLines = dayKlines(code, crossDay);
        if (kLines.size() < crossDay) {
            return;
        }
        for (int i = 0; i < crossDay - 1; i ++) {
            DayKline day = kLines.get(i);
            DayKline preDay = kLines.get(i + 1);
            if (day.getChg() < 1.5 || day.getChg() >= 5) {
                continue;
            }
            if (preDay.getChg() < -7 || preDay.getChg() > -4) {
                continue;
            }
            double score = preDay.getVol() * 1.0 / day.getVol();
            if (score < 1.5) {
                continue;
            }
            BUY_POINTS.add(new BuyPoint(code, day.getDay(), score, day.getPrice()));
        }
    }
}
