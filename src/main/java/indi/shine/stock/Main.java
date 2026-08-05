package indi.shine.stock;

import lombok.Getter;

import static indi.shine.stock.common.biz.DataCenterBiz.printCodeByPrice;

/**
 * @author xiezhenxiang 2022/6/24
 */
public class Main {

    @Getter
    public static class Rr {
        public Long totalVolCoin = 0L;
        public String code;
    }

    public static void main(String[] args) {
        printCodeByPrice(18.08);
    }
}
