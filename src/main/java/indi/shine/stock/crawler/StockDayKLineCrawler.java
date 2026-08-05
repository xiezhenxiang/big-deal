package indi.shine.stock.crawler;

import ai.plantdata.script.util.other.http.HttpProxyPool;
import ai.plantdata.script.util.other.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import indi.shine.stock.common.BulkInsertBiz;
import indi.shine.stock.env.EnvConfig;
import indi.shine.stock.util.RgbUtil;
import indi.shine.stock.util.RobotUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static indi.shine.stock.common.biz.DataCenterBiz.allStockCodes;
import static indi.shine.stock.common.biz.TradeTimeBiz.isTradeDay;
import static indi.shine.stock.env.EnvConfig.*;
import static indi.shine.stock.util.RobotUtil.ROBOT;

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
        List<String> proxyIps = Lists.newArrayList(
                "123.138.24.114:9443", "36.140.66.185:7890",
                "123.138.24.113:9443", "123.138.24.113:8800",
                "123.138.24.114:9480", "114.94.148.37:18080",
                "39.99.144.119:8118", "175.6.75.144:10064",
                "210.12.191.238:8088", "27.128.158.105:18079",
                "36.151.194.43:8087", "114.111.19.228:3389",
                "103.254.68.83:15010", "123.138.24.114:9480",
                "119.188.131.55:17981", "47.99.53.14:25001",
                "59.36.210.211:13552", "27.185.218.213:17981",
                "36.151.194.43:8087", "114.94.148.37:18080",
                "111.79.111.126:3128", "123.138.24.112:9443","114.246.101.17:12302", "36.140.66.185:7890");
        HttpProxyPool.setUseInterval(10000);
        HttpProxyPool.setProxyPool(proxyIps, false);
        HttpUtil.setRetryNum(10);
        BulkInsertBiz bulkInsertBiz = new BulkInsertBiz(BIG_DEAL_DB, STOCKS_DAY_KLINE_TB, 10,"_id");
        List<String> codes = allStockCodes();
        /*MongoCursor<Document> cursor = MONGO_UTIL.getClient().getDatabase(BIG_DEAL_DB).getCollection(STOCKS_DAY_KLINE_TB)
                .find().projection(Projections.include("_id")).cursor();
        cursor.forEachRemaining(s ->{
            codes.remove(；.getString("_id"));
        });*/
        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            Document doc = crawlDayKLine(code);
            //bulkInsertBiz.add(doc);
            Thread.sleep(RANDOM.nextInt(301) + 8000L);
            log.info("进度：{}/{}", i + 1, codes.size());
        }
        bulkInsertBiz.flush(true);
        log.info("日K数据爬取完成");
    }

    private static Document crawlDayKLine(String code) {
        String url = EnvConfig.kLineUrl(code);
        Map<String, String> head = new HashMap<>();
        head.put("Connection", "close");
        // String rs = HttpUtil.sendGet(url);
        String rs = rsFromGoogle(url);
        rs = rs.substring(rs.indexOf("{"), rs.lastIndexOf("}") + 1);
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

    private static String rsFromGoogle(String url) {
        // 确定谷歌浏览器是打开的
        while (!RgbUtil.match(2613, 114, 237, 30, 69)) {

        }
        // 把url写入系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(url), null);
        // Ctrl + L 选中浏览器地址栏
        ROBOT.keyPress(KeyEvent.VK_CONTROL);
        ROBOT.keyPress(KeyEvent.VK_L);
        ROBOT.keyRelease(KeyEvent.VK_L);
        ROBOT.keyRelease(KeyEvent.VK_CONTROL);
        ROBOT.delay(200);
        // Ctrl + V 粘贴
        ROBOT.keyPress(KeyEvent.VK_CONTROL);
        ROBOT.keyPress(KeyEvent.VK_V);
        ROBOT.keyRelease(KeyEvent.VK_V);
        ROBOT.keyRelease(KeyEvent.VK_CONTROL);
        ROBOT.delay(200);
        // Enter回车访问
        ROBOT.keyPress(KeyEvent.VK_ENTER);
        ROBOT.keyRelease(KeyEvent.VK_ENTER);
        while (!RgbUtil.match(189, 115, 71, 71, 71)) {

        }
        ROBOT.delay(200);
        if (!RgbUtil.match(246, 261, 240, 240, 240)) {
            return rsFromGoogle(url);
        }
        // Ctrl + A 选中结果
        ROBOT.keyPress(KeyEvent.VK_CONTROL);
        ROBOT.keyPress(KeyEvent.VK_A);
        ROBOT.keyRelease(KeyEvent.VK_A);
        ROBOT.keyRelease(KeyEvent.VK_CONTROL);
        ROBOT.delay(200);
        // Ctrl + C 复制
        ROBOT.keyPress(KeyEvent.VK_CONTROL);
        ROBOT.keyPress(KeyEvent.VK_C);
        ROBOT.keyRelease(KeyEvent.VK_C);
        ROBOT.keyRelease(KeyEvent.VK_CONTROL);
        ROBOT.delay(200);
        Transferable transferable = clipboard.getContents(null);
        String str = "";
        try {
            str = (String) transferable.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            str = rsFromGoogle(url);
        }
        return str;
    }
}
