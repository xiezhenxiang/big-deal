package indi.shine.stock.common;

import com.google.common.collect.Lists;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static indi.shine.stock.env.EnvConfig.MONGO_UTIL;

/**
 * @author xiezhenxiang 2025/7/16
 */
public class BulkPushBiz {

    private final String dbName;
    private final String tbName;
    private final String arrField;
    private final Integer batchSize;
    private final List<Document> ls = new ArrayList<>();

    public BulkPushBiz(String dbName, String tbName, Integer batchSize, String arrField) {
        this.dbName = dbName;
        this.tbName = tbName;
        this.batchSize = batchSize;
        this.arrField = arrField;
    }

    public synchronized void add(Document doc) {
        ls.add(doc);
        flush(false);
    }

    public void flush(boolean force) {
        if (ls.size() == 0) {
            return;
        }
        if (force || ls.size() >= batchSize) {
            List<WriteModel<Document>> updates = new ArrayList<>();
            for (Document doc : ls) {
                String id = doc.getString("_id");
                Object element = doc.get(arrField);
                // 更新操作：在 arr 数组的索引 0 位置插入元素
                Document filter = new Document("_id", id);
                Document update = new Document("$push", new Document(
                        arrField, new Document("$each", Lists.newArrayList(element)).append("$position", 0)
                ));
                // 创建 UpdateOneModel（upsert 设为 false，若文档不存在则不插入）
                updates.add(new UpdateOneModel<>(filter, update, new UpdateOptions().upsert(false)));
            }
            MONGO_UTIL.getClient().getDatabase(dbName).getCollection(tbName).bulkWrite(updates);
            ls.clear();
        }
    }


}
