package indi.shine.stock.common.biz;

import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static indi.shine.stock.env.EnvConfig.BIG_DEAL_DB;
import static indi.shine.stock.env.EnvConfig.MONGO_UTIL;

/**
 * @author xiezhenxiang 2023/10/9
 */
public class HolidayBiz {

    private static final String HOLIDAY_TB = "holiday";

    public static void main(String[] args) {
        /*addHoliday("2023-01-01");
        addHoliday("2023-01-02");
        addHoliday("2023-01-03");
        addHoliday("2023-01-31");
        addHoliday("2023-02-01");
        addHoliday("2023-02-02");
        addHoliday("2023-02-03");
        addHoliday("2023-02-04");
        addHoliday("2023-02-05");
        addHoliday("2023-02-06");
        addHoliday("2023-04-03");
        addHoliday("2023-04-04");*/
        addHoliday("2023-04-05");

        addHoliday("2023-04-29");
        addHoliday("2023-04-30");
        addHoliday("2023-05-01");
        addHoliday("2023-05-02");
        addHoliday("2023-05-03");

        addHoliday("2023-06-22");
        addHoliday("2023-06-23");
        addHoliday("2023-06-24");

        addHoliday("2023-09-29");
        addHoliday("2023-09-30");
        addHoliday("2023-10-01");
        addHoliday("2023-10-02");
        addHoliday("2023-10-03");
        addHoliday("2023-10-04");
        addHoliday("2023-10-05");
        addHoliday("2023-10-06");
    }

    public static boolean isHoliday(String day) {
        Document doc = new Document("day", day);
        MongoCursor<Document> cursor = MONGO_UTIL.find(BIG_DEAL_DB, HOLIDAY_TB, doc);
        return cursor.hasNext();
    }

    private static void addHoliday(String day) {
        Document doc = new Document("day", day);
        MONGO_UTIL.upsertOne(BIG_DEAL_DB, HOLIDAY_TB, doc, doc);
    }
}
