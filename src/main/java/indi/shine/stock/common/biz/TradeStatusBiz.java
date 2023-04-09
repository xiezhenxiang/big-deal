package indi.shine.stock.common.biz;

import ai.plantdata.script.util.other.TimeUtil;

import java.util.Calendar;

import static indi.shine.stock.common.biz.HolidayBiz.isHoliday;

/**
 * @author xiezhenxiang 2022/7/21
 */
public class TradeStatusBiz {

    /**
     * 今天是否交易日
     */
    public static boolean isTradeDay() {
        String today = TimeUtil.nowStr().substring(0, 10);
        if (isHoliday(today)) {
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
        System.out.println(isTradeTime());
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
