package indi.shine.stock.convertible;

import ai.plantdata.script.util.other.HttpUtil;
import ai.plantdata.script.util.other.JacksonUtil;
import ai.plantdata.script.util.other.StringUtils;
import com.alibaba.fastjson.JSONObject;
import indi.shine.stock.convertible.bean.ConvertibleBond;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static indi.shine.stock.convertible.bean.Config.DRIVER_UTIL;

/**
 * 可转债爬虫
 * @author xiezhenxiang 2022/6/24
 */
@Slf4j
public class ConvertibleBondCrawler extends Thread {

    private static final String DATA_LIST_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get?sortColumns=PUBLIC_START_DATE&sortTypes=-1&pageSize=1&pageNumber=pageNo&reportName=RPT_BOND_CB_LIST&columns=ALL&quoteColumns=f2~01~CONVERT_STOCK_CODE~CONVERT_STOCK_PRICE%2Cf235~10~SECURITY_CODE~TRANSFER_PRICE%2Cf236~10~SECURITY_CODE~TRANSFER_VALUE%2Cf2~10~SECURITY_CODE~CURRENT_BOND_PRICE%2Cf237~10~SECURITY_CODE~TRANSFER_PREMIUM_RATIO%2Cf239~10~SECURITY_CODE~RESALE_TRIG_PRICE%2Cf240~10~SECURITY_CODE~REDEEM_TRIG_PRICE%2Cf23~01~CONVERT_STOCK_CODE~PBV_RATIO&quoteType=0&source=WEB&client=WEB";

    @Override
    public void start() {
        log.info("可转债数据开始爬取");
        List<ConvertibleBond> allData = getAllData();
        log.info("get num: " + allData.size());
        for (ConvertibleBond bond : allData) {
            Map<String, Object> po = JacksonUtil.readValue(JacksonUtil.writeValueAsString(bond), Map.class, String.class, Object.class);
            DRIVER_UTIL.update("delete from convertible_bond where code = '"+ bond.getCode() +"'" );
            DRIVER_UTIL.insertSelective("convertible_bond", po, true);
        }
        log.info("可转债爬取完毕");
    }

    private List<ConvertibleBond> getAllData() {
        List<ConvertibleBond> ls = new ArrayList<>();
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            String url = DATA_LIST_URL.replace("pageNo", i + "");
            String rs = null;
            while (StringUtils.isEmpty(rs)) {
                rs = HttpUtil.sendGet(url);
            }
            JSONObject result = JSONObject.parseObject(rs).getJSONObject("result");
            if (result == null) {
                break;
            }
            result.getJSONArray("data").toJavaList(JSONObject.class)
                    .stream()
                    .map(ConvertibleBondCrawler::toConvertibleBond)
                    .forEach(ls::add);
        }
        return ls;
    }

    private static ConvertibleBond toConvertibleBond(JSONObject obj) {
        ConvertibleBond bond = new ConvertibleBond();
        bond.setCode(obj.getString("SECURITY_CODE"));
        bond.setName(obj.getString("SECURITY_NAME_ABBR"));
        bond.setPremiumRate(obj.getDoubleValue("TRANSFER_PREMIUM_RATIO"));
        bond.setParentName(obj.getString("SECURITY_SHORT_NAME"));
        bond.setParentCode(obj.getString("CONVERT_STOCK_CODE"));
        bond.setExpireDate(obj.getString("EXPIRE_DATE"));
        bond.setListingDate(obj.getString("LISTING_DATE"));
        bond.setRedeemDate(obj.getString("EXECUTE_START_DATESH"));
        Object price = obj.get("CURRENT_BOND_PRICE");
        if (!"-".equals(price)) {
            bond.setNowPrice(obj.getDoubleValue("CURRENT_BOND_PRICE"));
            // 双低值=转债价格+转股溢价率*100
            bond.setDoubleLow(bond.getNowPrice() + bond.getPremiumRate());
        }
        return bond;
    }
}
