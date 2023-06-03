package indi.shine.stock.crawler;

import ai.plantdata.script.util.other.HttpUtil;
import ai.plantdata.script.util.other.TimeUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.List;

import static indi.shine.stock.env.EnvConfig.*;
import static indi.shine.stock.common.biz.TradeStatusBiz.isTradeTime;

/**
 * https://data.eastmoney.com/zjlx/detail.html
 * @author xiezhenxiang 2022/7/20
 */
@Slf4j
public class StockAllCrawler/* extends Thread*/ {

    /** 沪市URL */
    private static final String H_URL = "https://push2.eastmoney.com/api/qt/clist/get?fid=f62&po=1&pz=100&pn=pageNo&np=1&fltt=2&invt=2&fs=m%3A1%2Bt%3A2%2Bf%3A!2%2Cm%3A1%2Bt%3A23%2Bf%3A!2&fields=f12%2Cf14%2Cf2%2Cf3%2Cf62%2Cf184%2Cf66%2Cf69%2Cf72%2Cf75%2Cf78%2Cf81%2Cf84%2Cf87%2Cf204%2Cf205%2Cf124%2Cf1%2Cf13";
    /** 深市URL */
    private static final String S_URL = "https://push2.eastmoney.com/api/qt/clist/get?fid=f62&po=1&pz=100&pn=pageNo&np=1&fltt=2&invt=2&fs=m%3A0%2Bt%3A6%2Bf%3A!2%2Cm%3A0%2Bt%3A13%2Bf%3A!2%2Cm%3A0%2Bt%3A80%2Bf%3A!2&fields=f12%2Cf14%2Cf2%2Cf3%2Cf62%2Cf184%2Cf66%2Cf69%2Cf72%2Cf75%2Cf78%2Cf81%2Cf84%2Cf87%2Cf204%2Cf205%2Cf124%2Cf1%2Cf13";

    public static void main(String[] args) {
        if (!isTradeTime()) {
            start();
        }
    }

    public static void start() {
        MONGO_UTIL.delete(BIG_DEAL_DB, STOCKS_TB, new Document());
        crawl(H_URL, "沪");
        crawl(S_URL, "深");
    }

    private static void crawl(String aUrl, String aType) {
        log.info("开始爬取{}市当日数据", aType);
        int count = 0;
        for (int i = 1; i < Integer.MAX_VALUE; i ++) {
            String url = aUrl.replace("pageNo", i + "");
            String rs = HttpUtil.sendGet(url);
            JSONObject dataObj = JSONObject.parseObject(rs).getJSONObject("data");
            if (dataObj == null) {
                break;
            }
            List<JSONObject> ls = dataObj.getJSONArray("diff").toJavaList(JSONObject.class);
            for (JSONObject obj : ls) {
                String code = obj.getString("f12");
                String name = obj.getString("f14");
                count ++;
                insert(code, name, aType);
            }
        }
        log.info("{}市当日数据爬取完毕, 共{}支股票", aType, count);
    }

    private static void insert(String code, String name, String type) {
        if (name.contains("ST")) {
            return;
        }
        Document doc = new Document();
        doc.put("_id", code);
        doc.put("name", name);
        doc.put("type", type);
        doc.put("createAt", TimeUtil.nowStr());
        MONGO_UTIL.upsertOne(BIG_DEAL_DB, STOCKS_TB, new Document("_id", code), doc);
    }
}
