package org.s4digester.tourist.util;

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

    public static long getMillOfToday(long time) {
        return time % (24 * 60 * 60 * 1000);
    }

}
