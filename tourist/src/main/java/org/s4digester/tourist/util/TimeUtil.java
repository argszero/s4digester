package org.s4digester.tourist.util;

import org.s4digester.tourist.event.SignalingEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Math.max;
import static java.lang.Math.min;

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

    public static long calc(long start, long end, boolean lastInside, long lastEventTime, long now) {
        //只有上次在景区，停留时间才累加，否则（一直不在景区，新进入景区），停留时间都不变
        if (lastInside) {
            long lastEndAge = getNextAge(lastEventTime, end);
            long startTime = (start < end ? lastEndAge : lastEndAge - 1) * 24 * 60 * 60 * 1000 + start; //统计起点，对于白天，则为当天，对于晚上，为结束的头一天
            long endTime = lastEndAge * 24 * 60 * 60 * 1000 + end; //统计终点
            return max(min(endTime, now), startTime) - min(max(lastEventTime, startTime), endTime);
        } else return 0;
    }
}
