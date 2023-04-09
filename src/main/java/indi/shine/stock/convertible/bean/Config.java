package indi.shine.stock.convertible.bean;

import ai.plantdata.script.util.database.DriverUtil;

/**
 * @author xiezhenxiang 2022/6/27
 */
public class Config {

    public static String MYSQL_IP = "www.shine555.top";

    public static Integer MYSQL_PORT = 3306;
    public static String MYSQL_USERNAME = "root";
    public static String MYSQL_PASSWORD = "xzxvip@Y21";

    public static DriverUtil DRIVER_UTIL = DriverUtil.getMysqlInstance(Config.MYSQL_IP, Config.MYSQL_PORT, "shares_trade",  Config.MYSQL_USERNAME, Config.MYSQL_PASSWORD);
}
