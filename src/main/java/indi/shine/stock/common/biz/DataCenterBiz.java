package indi.shine.stock.common.biz;

import ai.plantdata.script.util.other.CollectionUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import indi.shine.stock.bean.po.DayKline;
import indi.shine.stock.env.EnvConfig;
import org.bson.Document;
import org.bson.conversions.Bson;

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

    public static List<DayKline> dayKlines(String code, Integer lastDays) {
        List<DayKline> klines = new ArrayList<>();
        final MongoCursor<Document> cursor = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB)
                .find(new Document("_id", code)).projection(Projections.slice("klines", lastDays)).cursor();
        if (cursor.hasNext()) {
            List<String> ls = cursor.next().getList("klines", String.class);
            for (String l : ls) {
                klines.add(parseDayKline(l));
            }
        }
        return klines;
    }

    public static DayKline parseDayKline(String kline) {
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

    public static void printMaxPriceCode(Double maxPrice) {
        List<String> ls = new ArrayList<>();
        MongoCollection<Document> collection = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB);
        Bson projection = Projections.slice("klines", 1);
        MongoCursor<Document> cursor = collection.find().projection(projection).cursor();
        cursor.forEachRemaining(s -> {
            List<String> klines = s.getList("klines", String.class);
            if(!CollectionUtil.isEmpty(klines)) {
                DayKline k = parseDayKline(klines.get(0));
                if (k.maxPrice.equals(maxPrice)) {
                    String code = s.getString("_id");
                    System.out.println(code + " " + codeName(code));
                }
            }
        });
    }

    public static String codeName(String code) {
        return MONGO_UTIL.find(BIG_DEAL_DB, STOCKS_TB, Filters.eq("_id", code)).next().getString("name");
    }

    public static void printCodeByM5(double m5) {
        MongoCollection<Document> collection = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB);
        Bson projection = Projections.slice("klines", 5);
        MongoCursor<Document> cursor = collection.find().projection(projection).cursor();
        cursor.forEachRemaining(s -> {
            String code = s.getString("_id");
            List<String> klines = s.getList("klines", String.class);
            if(!CollectionUtil.isEmpty(klines)) {
                double total = 0;
                for (String kline : klines) {
                    DayKline k = parseDayKline(kline);
                    total += k.price;
                }
                double mm5 = Math.round(total / klines.size() * 100.0) / 100.0;
                if (mm5 == m5) {
                    System.out.println(code + " " + codeName(code));
                }
            }
        });
    }

    public static void printCodeByPrice(Double price) {
        MongoCollection<Document> collection = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB);
        Bson projection = Projections.slice("klines", 1);
        MongoCursor<Document> cursor = collection.find().projection(projection).cursor();
        cursor.forEachRemaining(s -> {
            List<String> klines = s.getList("klines", String.class);
            if(!CollectionUtil.isEmpty(klines)) {
                DayKline k = parseDayKline(klines.get(0));
                if (k.price.equals(price)) {
                    String code = s.getString("_id");
                    System.out.println(code + " " + codeName(code));
                }
            }

        });
    }
}
