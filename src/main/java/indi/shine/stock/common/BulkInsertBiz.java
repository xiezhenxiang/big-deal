package indi.shine.stock.common;

import indi.shine.stock.env.EnvConfig;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiezhenxiang 2023/3/23
 */
public class BulkInsertBiz {

    private final String dbName;
    private final String tbName;
    private final List<Document> ls = new ArrayList<>();

    public BulkInsertBiz(String dbName, String tbName) {
        this.dbName = dbName;
        this.tbName = tbName;
    }

    public void add(Document doc) {
        ls.add(doc);
        flush(false);
    }

    public void flush(boolean force) {
        if (force || ls.size() >= 1000) {
            EnvConfig.MONGO_UTIL.insertMany(dbName, tbName, ls);
            ls.clear();
        }
    }
}
