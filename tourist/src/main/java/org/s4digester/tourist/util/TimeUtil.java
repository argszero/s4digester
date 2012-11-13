package org.s4digester.tourist.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-9
 * Time: 上午10:46
 * To change this template use File | Settings | File Templates.
 */
public class TimeUtil {
    public static long getAge(long time) {
        return time / (24 * 60 * 60 * 1000);
    }

    public static long getNextAge(long from, long standard) {
        long age = getAge(from);
        long millOfToday = getMillOfToday(from);
        return millOfToday <= standard ? age : age + 1;
    }

    public static long getMillOfToday(long time) {
        return time % (24 * 60 * 60 * 1000);
    }

    public static long getHour(long time) {
        return (time / (60 * 60 * 1000)) % 24;
    }

    public static long getMinute(long time) {
        return ((time % (60 * 60 * 1000)) / (60 * 1000)) % 60;
    }
    public static String getDate(long age){
        Calendar  calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0+age*24*60*60*1000);
        return new SimpleDateFormat("MM-dd").format(calendar.getTime());
    }
}
