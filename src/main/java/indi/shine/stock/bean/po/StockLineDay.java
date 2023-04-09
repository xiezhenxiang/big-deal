package indi.shine.stock.bean.po;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiezhenxiang 2023/3/23
 */
@Getter
@Setter
public class StockLineDay {

    public String day;
    public Double price;
    public Double chg;
    public Double openPrice;
    public Double minPrice;
    public Double maxPrice;
    public Long vol;
    public Long volCoin;
}
