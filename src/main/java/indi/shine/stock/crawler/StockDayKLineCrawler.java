package indi.shine.stock.crawler;

import ai.plantdata.script.util.other.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import indi.shine.stock.common.BulkInsertBiz;
import indi.shine.stock.env.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.Collections;
import java.util.List;

import static indi.shine.stock.common.biz.DataCenterBiz.allStockCodes;
import static indi.shine.stock.common.biz.TradeTimeBiz.isTradeDay;
import static indi.shine.stock.env.EnvConfig.*;

/**
 * @author xiezhenxiang 2023/3/21
 */
@Slf4j
public class StockDayKLineCrawler {


    public static void main(String[] args) throws InterruptedException {
        if (!isTradeDay()) {
            return;
        }
        log.info("开始爬取日K数据");
        HttpUtil.setRetryNum(100);
        BulkInsertBiz bulkInsertBiz = new BulkInsertBiz(BIG_DEAL_DB, STOCKS_DAY_KLINE_TB, 10,"_id");
        List<String> codes = allStockCodes();
        /*MongoCursor<Document> cursor = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB)
                .find().projection(Projections.include("_id")).cursor();
        cursor.forEachRemaining(s ->{
            codes.remove(s.getString("_id"));
        });*/
        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            Document doc = crawlDayKLine(code);
            bulkInsertBiz.add(doc);
            Thread.sleep(RANDOM.nextInt(301) + 500L);
            log.info("进度：{}/{}", i + 1, codes.size());
        }
        bulkInsertBiz.flush(true);
        log.info("日K数据爬取完成");
    }

    private static Document crawlDayKLine(String code) {
        String url = EnvConfig.kLineUrl(code);
        String rs = HttpUtil.sendGet(url);
        JSONObject rsObj = JSONObject.parseObject(rs);
        JSONObject data = rsObj.getJSONObject("data");
        Document doc = new Document();
        doc.append("_id", code);
        doc.append("klines", "[]");
        if (data != null && !data.isEmpty()) {
            List<String> kLines = data.getJSONArray("klines").toJavaList(String.class);
            Collections.reverse(kLines);
            doc.append("klines", kLines);
        }
        return doc;
    }
}
