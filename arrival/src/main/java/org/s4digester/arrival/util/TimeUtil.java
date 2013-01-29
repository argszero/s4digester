package org.s4digester.arrival.util;

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
	
	/**
	 * 获取time对应的天数
	 * @param time
	 * @return long
	 */
    public static long getDay(long time) {
        return time / (24 * 60 * 60 * 1000);
    }

    /**
     * 返回两个毫秒数之间的天数间隔
     * @param from 前
     * @param to 后
     * @return
     */
    public static long getNextAge(long from, long to) {
        long age = getDay(from);
        long newAge = getDay(to);
        return (long)(newAge - age);
    }
    
    /**
     * 获取time对应当天的毫秒数
     * @param time
     * @return
     */
    public static long getMillisOfDay(long time) {
        return time % (24 * 60 * 60 * 1000);
    }

    /**
     * 获取time对应当天的小时数
     * @param time
     * @return
     */
    public static long getHour(long time) {
        return (time / (60 * 60 * 1000)) % 24;
    }

    /**
     * 获取time对应当天的分钟数
     * @param time
     * @return
     */
    public static long getMin(long time) {
        return ((time % (60 * 60 * 1000)) / (60 * 1000)) % 60;
    }
    
    /**
     * 获取age对应的MM-dd格式日期
     * @param age 天数
     * @return
     */
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
