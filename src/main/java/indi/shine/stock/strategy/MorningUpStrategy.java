package indi.shine.stock.strategy;

import ai.plantdata.script.util.other.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import indi.shine.stock.env.EnvConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static indi.shine.stock.env.EnvConfig.THREAD_UTIL;

/**
 * 不靠谱，暂无理论支撑
 * @author xiezhenxiang 2023/5/24
 */
@Slf4j
public class MorningUpStrategy implements Strategy {

    private static final String CANDIDATE_CODE_TB = "morning_up_candidate_code";
    private static final Map<String, Long> CACHE = new HashMap<>();
    // 5分钟内冲高3个点
    private static final int MINUS = 5;
    private static final double UP = 0.04;

    public static void main(String[] args) throws InterruptedException {
        // initLastDayCode();
        while ((true)) {
            new MorningUpStrategy().run();
            Thread.sleep(200);
        }
    }

    /*private static void initLastDayCode() {
        MONGO_UTIL.delete(BIG_DEAL_DB, CANDIDATE_CODE_TB, new Document());
        for (String code : allStockCodes()) {
            THREAD_UTIL.execute(() -> {
                List<StockLineDay> lineDays = stockLineDays(code, false);
                if (lineDays.size() < TRADE_DAYS_OF_YEAR ) {
                    return;
                }
                Collections.reverse(lineDays);
                StockLineDay lastDay = lineDays.get(0);
                Double chg = lastDay.getChg();
                if (chg >= 8) {
                    Document doc = new Document("code", code).append("chg", lastDay.getChg()).append("day", lastDay.getDay());
                    MONGO_UTIL.insertOne(BIG_DEAL_DB, CANDIDATE_CODE_TB, doc);
                }
            });
        }
    }*/

    /*@Override
    public List<String> getCodes() {
        List<String> codes = new ArrayList<>();
        MongoCursor<Document> cursor = MONGO_UTIL.find(BIG_DEAL_DB, CANDIDATE_CODE_TB, new Document());
        cursor.forEachRemaining(s -> {
            codes.add(s.getString("code"));
        });
        return codes;
    }*/

    @Override
    public void run() {
        for (String code : getCodes()) {
            THREAD_UTIL.execute(() -> {
                getBuyPoint(code);
            });
        }
        while (!THREAD_UTIL.empty()) {
            // wait finish
        }
    }

    @Override
    public void getBuyPoint(String code) {
        String url = EnvConfig.mkLineUrl(code);
        HashMap<String, String> headers = new HashMap<>();
        String rs = HttpUtil.sendGet(url, headers).substring(6);
        JSONObject data = JSONObject.parseObject(rs).getJSONObject("data");
        Double preClose = data.getDouble("preClose");
        List<String> kLine = data.getJSONArray("trends").toJavaList(String.class);
        Collections.reverse(kLine);
        double nowPrice = getMinKLinePrice(kLine.get(0));
        double minPrice = nowPrice;
        for (int i = 1; i <= MINUS && i < kLine.size(); i++) {
            minPrice = Math.min(minPrice, getMinKLinePrice(kLine.get(i)));
        }
        double v = Double.parseDouble(String.format("%.4f", (nowPrice - preClose) / preClose));
        double v2 = Double.parseDouble(String.format("%.4f", (minPrice - preClose) / preClose));
        if (v - v2 >= UP) {
            final Long lastTme = CACHE.getOrDefault(code, 0L);
            // 30分钟内不重复打印
            if (System.currentTimeMillis() - lastTme >= 1000 * 60 * 20) {
                CACHE.put(code, System.currentTimeMillis());
                log.info("code: " + code + " " + v + " " + nowPrice);
            }
        }
    }

    private double getMinKLinePrice(String line) {
        return Double.parseDouble(line.split(",")[2]);
    }

    /*@Override
    public void printResult() {
        BUY_POINTS.sort(Comparator.comparingDouble(o -> o.score));
        for (BuyPoint buyPoint : BUY_POINTS) {
            System.out.println(buyPoint.code + " " + buyPoint.score + " " + buyPoint.price);
        }
    }*/
}
