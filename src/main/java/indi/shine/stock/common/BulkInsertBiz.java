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
    private final String[] upsertFields;
    private Integer batchSize;
    private final List<Document> ls = new ArrayList<>();

    public BulkInsertBiz(String dbName, String tbName, Integer batchSize, String... upsertFields) {
        this.dbName = dbName;
        this.tbName = tbName;
        this.batchSize = batchSize;
        this.upsertFields = upsertFields;
    }

    public synchronized void add(Document doc) {
        ls.add(doc);
        flush(false);
    }

    public void flush(boolean force) {
        if (force || ls.size() >= batchSize) {
            if (upsertFields == null || upsertFields.length == 0) {
                EnvConfig.MONGO_UTIL.insertMany(dbName, tbName, ls);
            } else {
                EnvConfig.MONGO_UTIL.upsertMany(dbName, tbName, ls, true, upsertFields);
            }
            ls.clear();
        }
    }
}
