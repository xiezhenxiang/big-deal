package indi.shine.stock.env;

import ai.plantdata.script.util.database.MongoUtil;
import ai.plantdata.script.util.other.HttpUtil;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * @author xiezhenxiang 2022/10/9
 */
public class EnvConfig {

    private static final String MONGO_ADDRESS = "139.196.94.148:19130";
    public static final MongoUtil MONGO_UTIL = MongoUtil.getInstance(MONGO_ADDRESS, "root", "root@shine");
    public static final String BIG_DEAL_DB = "big_deal";
    public static final String STOCKS_TB = "stocks";
    public static final String STOCKS_HISTORY_TB = "history_line";

    private static final String STOCK_K_URL = "https://18.push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s.%s&ut=fa5fd1943c7b386f172d6893dbfba10b" +
            "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60," +
            "f61&klt=101&fqt=0&end=20500101&lmt=%s";

    static {
        HttpUtil.setReadTimeout(10000);
        HttpUtil.setRetryNum(Integer.MAX_VALUE);
        createUniqueIndex(BIG_DEAL_DB, STOCKS_HISTORY_TB, new Document("code", 1).append("day", 1));
    }

    public static String kLineUrl(String code) {
       Integer t = code.startsWith("6") ? 1 : 0;
       return String.format(STOCK_K_URL, t, code, 10000000);
    }

    private static void createUniqueIndex(String db, String tb, Bson index) {
        MONGO_UTIL.getClient().getDatabase(db).getCollection(tb).createIndex(index, new IndexOptions().unique(true));
    }
}
