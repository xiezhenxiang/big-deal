package indi.shine.stock;

import indi.shine.stock.common.biz.TradeStatusBiz;

/**
 * @author xiezhenxiang 2022/6/24
 */
public class Main {

    public static void main(String[] args) {

        if (!TradeStatusBiz.isTradeDay()) {
            return;
        }
    }
}
