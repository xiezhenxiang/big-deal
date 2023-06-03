package indi.shine.stock.bean.po;

import lombok.AllArgsConstructor;

/**
 * @author xiezhenxiang 2023/5/19
 */
@AllArgsConstructor
public class BuyPoint {

    public String code;
    public String day;
    public double score;
    public double price;
    public long findTime = System.currentTimeMillis();

    public BuyPoint(String code, String day, double score, double price) {
        this.code = code;
        this.day = day;
        this.score = score;
        this.price = price;
    }
}
