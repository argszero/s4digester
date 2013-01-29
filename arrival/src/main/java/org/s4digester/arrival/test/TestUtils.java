package org.s4digester.arrival.test;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(split[1]);
        split[1] = String.valueOf(date.getTime() + 8 * 60 * 60 * 1000);
        return StringUtils.join(split, ",");
    }

    public static String trans(int i, String s) throws ParseException {
        String[] split = s.split(",");
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(split[1]);
        split[1] = String.valueOf(date.getTime() + 8 * 60 * 60 * 1000 + i);
        return StringUtils.join(split, ",");
    }

    public static Buffer newBuffer() {
        return new Buffer();
    }

    public static class Buffer {
        List<String[]> buffer = new ArrayList<String[]>();

        public Buffer add(String name, String start, String end) {
            return add(name, start, end, true, true);
        }

        public Buffer add(String name, String start, String end, boolean printStart, boolean printEnd) {
            buffer.add(new String[]{name, start, "tourist", String.valueOf(printStart)});
            buffer.add(new String[]{name, end, "home", String.valueOf(printEnd)});
            return this;
        }

        public void write(OutputStream output, int day) throws ParseException, IOException {
            Collections.sort(buffer, new Comparator<String[]>() {
                @Override
                public int compare(String[] o1, String[] o2) {
                    long o1Time = Long.parseLong(o1[1].replace(":", ""));
                    long o2Time = Long.parseLong(o2[1].replace(":", ""));
                    return Long.compare(o1Time, o2Time);
                }
            });
            for (String[] a : buffer) {
                if (Boolean.valueOf(a[3])) {
                    String message = a[0] + ",2012-01-0" + day + " " + a[1] + "," + a[2] + "," + a[2] + "\r\n";
                    System.out.print(message);
                    IOUtils.write(trans(message), output);
                }
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(trans("Worker1,2012-01-01 08:00:00,tourist,tourist"));
        System.out.println(trans("Worker1,2012-01-01 11:59:59,tourist,tourist"));
        System.out.println(trans("Worker1,2012-01-01 12:00:00,tourist,tourist"));


        System.out.println( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(1325419200000L)));
        System.out.println( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(1325376000000L)));
        System.out.println((1325419200000L-1325376000000L)/(24*60*60*1000));
        System.out.println((1325419200000L-1325376000000L)/(60*60*1000));
    }

}
