package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.BuyPoint;
import indi.shine.stock.bean.po.DayKline;

import java.util.List;

import static indi.shine.stock.common.biz.DataCenterBiz.dayKlines;

/**
 * 无为釜底抽薪策略
 * 1、涨停首版、非ST、非一字板、非T字板、非连板、成交量要放大释放？
 * 2、涨停第二天跳空高开闭市收阴收跌，实体跌幅在3%以内，分歧量大于涨停板成交量，非天量
 * 3、涨停第三天跳空低开闭市收阴收跌，实体跌幅大于3%，洗盘量相比分歧量缩量
 * 4、止盈：实体涨幅5%以上，盘中拉升不涨停；或涨停后第二天没有继续涨停
 * 5、止损：收盘前跌破涨停板最低为
 *
 * @author xiezhenxiang 2023/6/2
 */
public class TopFootTopStrategy implements Strategy {

    public static void main(String[] args) {
        new TopFootTopStrategy().run();
    }

    @Override
    public void getBuyPoint(String code) {
        int crossDay = 10;
        List<DayKline> lineDays = dayKlines(code, crossDay);
        for (int i = 2; i < lineDays.size(); i++) {
            DayKline day1 = lineDays.get(i);
            DayKline day2 = lineDays.get(i - 1);
            DayKline day3 = lineDays.get(i - 2);
            // 涨停天
            if (day1.getChg() < 9.8) {
                continue;
            }
            // 分歧天
            double volChg = (day2.getVol() - day1.getVol()) * 1.0 / day1.getVol();
            if (day2.getOpenPrice() < day1.getPrice()
                    || day2.getChg() > 0 || day2.getChg() < -3.1
                    || volChg > 1.6) {
                continue;
            }
            // 洗盘天
            if (day3.getOpenPrice() > day2.getPrice()
                    || day3.getChg() > -3 || day3.getVol() > day2.getVol()) {
                continue;
            }
            BUY_POINTS.add(new BuyPoint(code, day3.getDay(), 0, day3.getPrice()));
        }
    }
}
