package indi.shine.stock.common.biz;

import ai.plantdata.script.util.other.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author xiezhenxiang 2022/7/21
 */
public class TradeTimeBiz {

    private static final List<String> HOLIDAYS = new ArrayList<>();
    static {
        HOLIDAYS.add("2025-10-01");
        HOLIDAYS.add("2025-10-02");
        HOLIDAYS.add("2025-10-03");
        HOLIDAYS.add("2025-10-04");
        HOLIDAYS.add("2025-10-05");
        HOLIDAYS.add("2025-10-06");
        HOLIDAYS.add("2025-10-07");
        HOLIDAYS.add("2025-10-08");
    }

    /**
     * 今天是否交易日
     */
    public static boolean isTradeDay() {
        String today = TimeUtil.nowStr().substring(0, 10);
        if (HOLIDAYS.contains(today)) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        boolean isFirstSunday = (calendar.getFirstDayOfWeek() == Calendar.SUNDAY);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (isFirstSunday) {
            weekDay--;
            if (weekDay == 0) {
                weekDay = 7;
            }
        }
        return weekDay != 6 && weekDay != 7;
    }

    public static void main(String[] args) {
        System.out.println(isTradeDay());
    }

    /**
     * 现在是否交易时间
     */
    public static boolean isTradeTime() {
        if (!isTradeDay()) {
            return false;
        }
        String str = TimeUtil.nowStr();
        String time = str.substring(11, 16);
        return (time.compareTo("09:30") >= 0 && time.compareTo("11:30") <= 0)
                || (time.compareTo("13:00") >= 0 && time.compareTo("15:00") <= 0);
    }
}
