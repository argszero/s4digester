package org.s4digester.tourist.test;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-9
 * Time: 下午3:24
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(trans("A,2012-01-01 08:00:00,tourist,tourist"));
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();
        IOUtils.write(trans("A,2012-01-01 08:00:00,tourist,tourist"), out);
        IOUtils.write(trans("A,2012-01-01 10:00:00,home,home"), out);
        out.close();
        socket.close();
    }

    private static String trans(String s) throws ParseException {
        String[] split = s.split(",");
        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(split[1]);
        split[1] = String.valueOf(date.getTime() + 8 * 60 * 60 * 1000);
        return StringUtils.join(split,",");
    }
}
