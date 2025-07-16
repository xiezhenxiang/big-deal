package indi.shine.stock.common.biz;

import ai.plantdata.script.util.other.JacksonUtil;
import com.mongodb.client.MongoCursor;
import indi.shine.stock.bean.po.DayKline;
import indi.shine.stock.env.EnvConfig;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static indi.shine.stock.env.EnvConfig.*;

/**
 * @author xiezhenxiang 2025/7/15
 */
public class DataCenterBiz {

    /**
     * 所有股票代码
     */
    public static List<String> allStockCodes() {
        List<String> ls = new ArrayList<>();
        MongoCursor<Document> cursor = MONGO_UTIL.find(BIG_DEAL_DB, EnvConfig.STOCKS_TB, new Document());
        cursor.forEachRemaining(s -> {
            if (!s.getString("name").contains("ST")) {
                ls.add(s.getString("_id"));

            }
        });
        return ls;
    }

    public static List<DayKline> dayKlines(String code) {
        List<DayKline> klines = new ArrayList<>();
        MongoCursor<Document> cursor = MONGO_UTIL.find(BIG_DEAL_DB, STOCKS_DAY_KLINE_TB, new Document("_id", code));
        if (cursor.hasNext()) {
            String str = cursor.next().getString("klines");
            List<String> ls = JacksonUtil.readValue(str, List.class, String.class);
            for (String l : ls) {
                klines.add(parseDayKline(l));
            }
        }
        return klines;
    }

    private static DayKline parseDayKline(String kline) {
        String[] arr = kline.split(",");
        String day = arr[0];
        Double openPrice = Double.parseDouble(arr[1]);
        Double price = Double.parseDouble(arr[2]);
        Double maxPrice = Double.parseDouble(arr[3]);
        Double minPrice = Double.parseDouble(arr[4]);
        // 成交量
        Long vol = toLong(arr[5]);
        // 成交额
        Long volCoin = toLong(arr[6]);
        // 涨跌幅
        Double chg = Double.parseDouble(arr[8]);
        DayKline lineDay = new DayKline();
        lineDay.setDay(day);
        lineDay.setPrice(price);
        lineDay.setChg(chg);
        lineDay.setOpenPrice(openPrice);
        lineDay.setMinPrice(minPrice);
        lineDay.setMaxPrice(maxPrice);
        lineDay.setVol(vol);
        lineDay.setVolCoin(volCoin);
        return lineDay;
    }

    public static Long toLong(String str) {
        int lastIndex = str.contains(".") ? str.indexOf(".") : str.length();
        return Long.parseLong(str.substring(0, lastIndex));
    }
}
