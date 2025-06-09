package indi.shine.stock.common.biz;

import ai.plantdata.script.util.database.MongoUtil;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import indi.shine.stock.env.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static indi.shine.stock.crawler.StockHistoryCrawler.allStockCodes;
import static indi.shine.stock.env.EnvConfig.*;

/**
 * @author xiezhenxiang 2025/3/30
 */
@Slf4j
public class ChildrenBiz {

    public static void main(String[] args) {
        List<String> codes = allStockCodes();
        log.info("codes size: " + codes.size());
        String endTime = "2025-03-20";
        int arr[] = {1,0,1,0,1,0,1,1,0,0,1,0,1,0,0,0,0,0,0};
        String start = "华";
        for (String code : codes) {
            int index = -1 ;
            Bson q = Filters.and(Filters.eq("code", code), Filters.lte("day", endTime));
            MongoCursor<Document> cursor = MONGO_UTIL.find(BIG_DEAL_DB, STOCKS_HISTORY_TB, q, Sorts.descending("day"));
            while (cursor.hasNext()) {
                /*if (++ index == arr.length) {
                    MongoCursor<Document> cursor2 = MONGO_UTIL.find(EnvConfig.BIG_DEAL_DB, EnvConfig.STOCKS_TB, new Document("_id", code));
                    String name = cursor2.next().getString("name");
                    log.info("get code: " + name);
                    break;
                }*/
                Document doc = cursor.next();
                Double price = doc.getDouble("price");

                if (Math.abs(price - 126.88) <= 0.05) {
                    MongoCursor<Document> cursor2 = MONGO_UTIL.find(EnvConfig.BIG_DEAL_DB, EnvConfig.STOCKS_TB, new Document("_id", code));
                    String name = cursor2.next().getString("name");
                    log.info("get code: " + name);
                    break;
                }
                /*
                double v = doc.getDouble("price") - doc.getDouble("openPrice");
                BigDecimal bd = new BigDecimal(v);
                bd = bd.setScale(3, RoundingMode.HALF_UP);
                v = bd.doubleValue();
                if (v == 0) {
                    continue;
                }
                int f = v > 0 ? 1 : 0;
                if (f != arr[index]) {
                    break;
                }*/
            }
            // log.info("进度：{}/{}", ++cnt, codes.size());
        }
    }

    private static void findCode() {

    }
}
