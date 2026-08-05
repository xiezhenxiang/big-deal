package indi.shine.stock.common;

import ai.plantdata.script.util.other.http.HttpProxyPool;
import ai.plantdata.script.util.other.http.HttpUtil;
import com.google.common.collect.Lists;

/**
 * @author xiezhenxiang 2026/1/8
 */
public class InitBiz {

    public static void initHttpProxy() {
        HttpProxyPool.setUseInterval(11000);
        HttpUtil.setProxyIpPool(Lists.newArrayList("47.240.65.214:46288", "117.72.179.51:46288"));
        HttpUtil.setProxyAuth("squidroot", "squidroot@2025");
    }
}
