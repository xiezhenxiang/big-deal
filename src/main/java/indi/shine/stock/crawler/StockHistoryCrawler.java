package indi.shine.stock.crawler;

import ai.plantdata.script.util.other.HttpUtil;
import ai.plantdata.script.util.other.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCursor;
import indi.shine.stock.bean.po.StockLineDay;
import indi.shine.stock.common.BulkInsertBiz;
import indi.shine.stock.common.biz.TradeStatusBiz;
import indi.shine.stock.env.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static indi.shine.stock.env.EnvConfig.MONGO_UTIL;

/**
 * @author xiezhenxiang 2023/3/21
 */
@Slf4j
public class StockHistoryCrawler {

    private static final boolean NOT_FIRST = true;

    public static void main(String[] args) {
        if (!TradeStatusBiz.isTradeDay()) {
            log.info("not trade day");
            return;
        }
        log.info("开始爬取历史数据");
        List<String> codes = allStockCodes();
        BulkInsertBiz bulkInsertBiz = new BulkInsertBiz(EnvConfig.BIG_DEAL_DB, EnvConfig.STOCKS_HISTORY_TB);
        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            List<StockLineDay> lineDays = stockLineDays(code, true);
            for (StockLineDay lineDay : lineDays) {
                Document doc = new Document("code", code)
                        .append("day", lineDay.getDay())
                        .append("price", lineDay.getPrice())
                        .append("chg", lineDay.getChg())
                        .append("openPrice", lineDay.getOpenPrice())
                        .append("minPrice", lineDay.getMinPrice())
                        .append("maxPrice", lineDay.getMaxPrice())
                        .append("vol", lineDay.getVol())
                        .append("volCoin", lineDay.getVolCoin())
                        .append("createAt", TimeUtil.nowStr());
                bulkInsertBiz.add(doc);
            }
            log.info("进度：{}/{}", i + 1, codes.size());
        }
        bulkInsertBiz.flush(true);
        log.info("历史数据爬取完成");
    }

    public static List<String> allStockCodes() {
        List<String> ls = new ArrayList<>();
        MongoCursor<Document> cursor = MONGO_UTIL.find(EnvConfig.BIG_DEAL_DB, EnvConfig.STOCKS_TB, new Document());
        cursor.forEachRemaining(s -> {
            if (!s.getString("name").contains("ST")) {
                ls.add(s.getString("_id"));
            }
        });
        return ls;
    }

    private static Long toLong(String str) {
        int lastIndex = str.contains(".") ? str.indexOf(".") : str.length();
        return Long.parseLong(str.substring(0, lastIndex));
    }

    public static List<StockLineDay> stockLineDays(String code, boolean recently) {
        List<StockLineDay> lineDays = new ArrayList<>();
        String url = EnvConfig.kLineUrl(code);
        String rs = HttpUtil.sendGet(url);
        JSONObject rsObj = JSONObject.parseObject(rs);
        JSONObject data = rsObj.getJSONObject("data");
        if (data != null && !data.isEmpty()) {
            List<String> kLines = data.getJSONArray("klines").toJavaList(String.class);
            if (recently && kLines.size() > 30) {
                kLines = kLines.subList(kLines.size() - 30, kLines.size());
            }
            for (String kLine : kLines) {
                StockLineDay lineDay = parseLineDay(kLine);
                lineDays.add(lineDay);
            }
        }
        Collections.reverse(lineDays);
        return lineDays;
    }

    private static StockLineDay parseLineDay(String kline) {
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
        StockLineDay lineDay = new StockLineDay();
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
}
