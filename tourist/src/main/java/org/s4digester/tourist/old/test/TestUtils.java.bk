package org.s4digester.tourist.test;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-12
 * Time: 下午1:13
 * To change this template use File | Settings | File Templates.
 */
public class TestUtils {
    public static String trans(String s) throws ParseException {
        String[] split = s.split(",");
        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(split[1]);
        split[1] = String.valueOf(date.getTime() + 8 * 60 * 60 * 1000);
        return StringUtils.join(split, ",");
    }

    public static String trans(int i, String s) throws ParseException {
        String[] split = s.split(",");
        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(split[1]);
        split[1] = String.valueOf(date.getTime() + 8 * 60 * 60 * 1000 + i);
        return StringUtils.join(split, ",");
    }
}
