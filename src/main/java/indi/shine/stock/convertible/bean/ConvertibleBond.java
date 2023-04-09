package indi.shine.stock.convertible.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 可转债
 * @author xiezhenxiang 2022/6/23
 */
@Getter
@Setter
public class ConvertibleBond {

    /** 代码 */
    private String code;
    private String name;
    /** 双低值 */
    private Double doubleLow;
    /** 到期时间 */
    private String expireDate;
    /** 上市时间 */
    private String listingDate;
    /** 父股票代码 */
    private String parentCode;
    /** 父股票名称 */
    private String parentName;
    /** 最新赎回执行日 */
    private String redeemDate;
    /** 现价 */
    private Double nowPrice = 0.0;
    /** 转股溢价率 */
    private Double premiumRate;
}
