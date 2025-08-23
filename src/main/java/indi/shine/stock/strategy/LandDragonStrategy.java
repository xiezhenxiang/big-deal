package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.DayKline;
import indi.shine.stock.common.biz.DataCenterBiz;

import java.util.List;

/**
 * @author xiezhenxiang 2025/7/23
 */
public class LandDragonStrategy implements Strategy{

    public static void main(String[] args) {
        new LandDragonStrategy().run();
    }

    @Override
    public void getBuyPoint(String code) {
        List<DayKline> klines = DataCenterBiz.dayKlines(code, 70);
        for (int i = 0; i < klines.size(); i++) {
            System.out.println(klines.get(0).getDay());
        }
        System.out.println(klines.get(klines.size() - 1).getDay());
    }
}
