package indi.shine.stock.env;

import ai.plantdata.script.util.database.MongoUtil;
import ai.plantdata.script.util.other.HttpUtil;
import ai.plantdata.script.util.other.ThreadUtil;

import java.util.Random;

/**
 * @author xiezhenxiang 2022/10/9
 */
public class EnvConfig {

    static {
        HttpUtil.setReadTimeout(15000);
        HttpUtil.setRetryNum(3);
    }

    private static final String MONGO_ADDRESS = "117.72.179.51:19130";
    public static final MongoUtil MONGO_UTIL = MongoUtil.getInstance(MONGO_ADDRESS, "root", "root@hiekn");
    public static final String BIG_DEAL_DB = "big_deal";
    public static final String STOCKS_TB = "stocks";
    public static final String STOCKS_DAY_KLINE_TB = "day_klines";

    public static final ThreadUtil THREAD_UTIL = new ThreadUtil(2, 20);

    public static final Random RANDOM = new Random();

    private static final String DAY_K_URL = "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s.%s&ut=fa5fd1943c7b386f172d6893dbfba10b" +
            "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60," +
            "f61&klt=101&fqt=1&end=20500101&lmt=%s";

    private static final String MINUTE_K_URL = "https://4.push2.eastmoney.com/api/qt/stock/trends2/sse?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f17&fields2=f51,f52,f53,f54,f55,f56,f57,f58&mpi=1000&ut=fa5fd1943c7b386f172d6893dbfba10b&" +
            "secid=%s.%s&ndays=1&iscr=0&iscca=0&wbp2u=8888326347002756|0|1|0|web";

    public static String kLineUrl(String code) {
       Integer t = code.startsWith("6") ? 1 : 0;
       // 近三年的
       return String.format(DAY_K_URL, t, code, 750) + "&_=" + System.currentTimeMillis();
    }

    public static String mkLineUrl(String code) {
       Integer t = code.startsWith("6") ? 1 : 0;
       return String.format(MINUTE_K_URL, t, code);
    }
}
