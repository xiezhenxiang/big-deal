package indi.shine.stock;

import ai.plantdata.script.util.other.CollectionUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import indi.shine.stock.bean.po.DayKline;
import indi.shine.stock.common.biz.DataCenterBiz;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static indi.shine.stock.common.biz.DataCenterBiz.parseDayKline;
import static indi.shine.stock.common.biz.DataCenterBiz.printCodeByPrice;
import static indi.shine.stock.env.EnvConfig.*;

/**
 * @author xiezhenxiang 2022/6/24
 */
public class Main {

    @Getter
    public static class Rr {
        public Long totalVolCoin = 0L;
        public String code;
    }

    public static void main(String[] args) {
        printCodeByPrice(16.61);
    }
}
