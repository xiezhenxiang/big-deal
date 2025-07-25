package indi.shine.stock.strategy;

import indi.shine.stock.bean.po.BuyPoint;

import java.util.ArrayList;
import java.util.List;

import static indi.shine.stock.common.biz.DataCenterBiz.allStockCodes;
import static indi.shine.stock.env.EnvConfig.THREAD_UTIL;

/**
 * @author xiezhenxiang 2023/5/19
 */
public interface Strategy {

    long START = System.currentTimeMillis();
    List<BuyPoint> BUY_POINTS = new ArrayList<>();

    default List<String> getCodes() {
        return allStockCodes();
    }

    default void run() {
        List<String> codes = getCodes();
        int cnt = 0;
        for (String code : codes) {
            THREAD_UTIL.execute(() -> {
                getBuyPoint(code);
            });
            if (++ cnt % 100 == 0) {
                System.out.println("process: " + cnt + "/" + codes.size());
            }
        }
        THREAD_UTIL.closeWithSafe();
        printResult();
    }

    default void printResult() {
        BUY_POINTS.sort((o1, o2) -> Double.compare(o2.score, o1.score));
        for (BuyPoint buyPoint : BUY_POINTS) {
            System.out.println(buyPoint.code + " " + buyPoint.day + " " + buyPoint.score + " " + buyPoint.price);
        }
        System.out.println("cost: " + ((System.currentTimeMillis() - START) / 1000 * 1.0 / 60) + "min");
        System.out.println("命中: " + BUY_POINTS.size());
    }

    void getBuyPoint(String code);
}
