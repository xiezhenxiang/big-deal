package indi.shine.stock.crawler;

import ai.plantdata.script.util.other.CollectionUtil;
import ai.plantdata.script.util.other.http.HttpUtil;
import ai.plantdata.script.util.other.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import indi.shine.stock.common.BulkInsertBiz;
import indi.shine.stock.common.BulkPushBiz;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static indi.shine.stock.common.biz.DataCenterBiz.toLong;
import static indi.shine.stock.common.biz.TradeTimeBiz.isTradeDay;
import static indi.shine.stock.env.EnvConfig.*;

/**
 * https://data.eastmoney.com/zjlx/detail.html
 * @author xiezhenxiang 2022/7/20
 */
@Slf4j
public class StockAllCrawler/* extends Thread*/ {

    /** 沪市URL */
    private static final String H_URL = "https://push2.eastmoney.com/api/qt/clist/get?fid=f62&po=1&pz=100&pn=pageNo&np=1&fltt=2&invt=2&ut=8dec03ba335b81bf4ebdf7b29ec27d15&fs=m:1+t:2+f:!2,m:1+t:23+f:!2&fields=f2,f3,f5,f6,f12,f14,f15,f16,f17";
    /** 深市URL */
    private static final String S_URL = "https://push2.eastmoney.com/api/qt/clist/get?fid=f62&po=1&pz=50&pn=pageNo&np=1&fltt=2&invt=2&ut=8dec03ba335b81bf4ebdf7b29ec27d15&fs=m:0+t:6+f:!2,m:0+t:13+f:!2,m:0+t:80+f:!2&fields=f2,f3,f5,f6,f12,f14,f15,f16,f17";
    /** 创业板 */
    private static final String C_URL = "https://push2delay.eastmoney.com/api/qt/clist/get?fid=f62&po=1&pz=50&pn=pageNo&np=1&fltt=2&invt=2&ut=8dec03ba335b81bf4ebdf7b29ec27d15&fs=m%3A0%2Bt%3A80%2Bf%3A!2&fields=f12%2Cf14%2Cf2%2Cf3%2Cf62%2Cf184%2Cf66%2Cf69%2Cf72%2Cf75%2Cf78%2Cf81%2Cf84%2Cf87%2Cf204%2Cf205%2Cf124%2Cf1%2Cf13";

    public static void main(String[] args) throws InterruptedException {
        start();
    }

    private static BulkInsertBiz bulkInsertBiz;
    private static BulkPushBiz bulkPushBiz;
    private static HashSet<String> needUpdateCodes = new HashSet<>();

    public static void start() throws InterruptedException {
        if (!isTradeDay()) {
            return;
        }
        loadNeedUpdateCodes();
        bulkInsertBiz = new BulkInsertBiz(BIG_DEAL_DB, STOCKS_TB, 100,"_id");
        bulkPushBiz = new BulkPushBiz(BIG_DEAL_DB, STOCKS_DAY_KLINE_TB, 100, "klines");
        // crawl(H_URL, "沪");
        // crawl(S_URL, "深");
        crawl(C_URL, "创业板");
        bulkInsertBiz.flush(true);
        bulkPushBiz.flush(true);
    }

    private static void loadNeedUpdateCodes() {
        String today = TimeUtil.nowStr().substring(0, 10);
        MongoCollection<Document> collection = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB);
        Document projection = new Document("klines", new Document("$slice", 1));
        MongoCursor<Document> cursor = collection.find().projection(projection).cursor();
        cursor.forEachRemaining(s -> {
            List<String> klines = s.getList("klines", String.class);
            if (!CollectionUtil.isEmpty(klines) && !klines.get(0).startsWith(today)) {
                needUpdateCodes.add(s.getString("_id"));
            }
        });
    }

    private static void crawl(String aUrl, String aType) throws InterruptedException {
        log.info("开始爬取{}市当日数据", aType);
        String day = TimeUtil.nowStr().substring(0, 10);
        int count = 0;
        for (int i = 1; i < Integer.MAX_VALUE; i ++) {
            String url = aUrl.replace("pageNo", i + "");
            String rs = HttpUtil.sendGet(url);
            rs = rs.substring(rs.indexOf("{"), rs.lastIndexOf("}") + 1);
            JSONObject dataObj = JSONObject.parseObject(rs).getJSONObject("data");
            if (dataObj == null) {
                break;
            }
            List<JSONObject> ls = dataObj.getJSONArray("diff").toJavaList(JSONObject.class);
            for (JSONObject obj : ls) {
                String code = obj.getString("f12");
                String name = obj.getString("f14");
                if (name.contains("ST")) {
                    continue;
                }
                Document doc = new Document();
                doc.put("_id", code);
                doc.put("name", name);
                doc.put("type", aType);
                doc.put("createAt", TimeUtil.nowStr());
                bulkInsertBiz.add(doc);
                if (needUpdateCodes.contains(code) && obj.get("f3") instanceof Number && !obj.get("f5").toString().contains("-")) {
                    Double chg = 10.0;
                    // 涨跌幅
                    chg = obj.getDouble("f3");
                    // 成交量
                    Long vol = toLong(obj.get("f5").toString());
                    // 成交额
                    Long volCoin = toLong(obj.get("f6").toString());
                    Double price = ((BigDecimal) obj.get("f2")).doubleValue();
                    Double maxPrice = ((BigDecimal) obj.get("f15")).doubleValue();
                    Double minPrice = ((BigDecimal) obj.get("f16")).doubleValue();
                    Double openPrice = ((BigDecimal) obj.get("f17")).doubleValue();
                    String kLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", day, openPrice, price, maxPrice, minPrice, vol, volCoin, 0, chg);
                    Document push = new Document("_id", code).append("klines", kLine);
                    bulkPushBiz.add(push);
                }
                count ++;
            }
            Thread.sleep(RANDOM.nextInt(301) + 10000L);
            log.info("process: " + count);
        }
        log.info("{}市当日数据爬取完毕, 共{}支股票", aType, count);
    }
}
