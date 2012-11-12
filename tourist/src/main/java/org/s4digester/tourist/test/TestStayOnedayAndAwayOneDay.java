package org.s4digester.tourist.test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;

import static org.s4digester.tourist.test.TestUtils.trans;

/**
 * 测试用户一天在景区，第二天不在景区
 *
 * 预期结果：只有一条Daytime5In10PE - receive StayScenicDuringDaytimeEvent
 */
public class TestStayOneDayAndAwayOneDay {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();

        IOUtils.write(trans("A,2012-01-01 08:00:00,tourist,tourist\r\n"), out);
        IOUtils.write(trans("A,2012-01-01 11:00:01,home,home\r\n"), out);

        IOUtils.write(trans("A,2012-01-02 17:00:00,tourist,tourist\r\n"), out);
        IOUtils.write(trans("A,2012-01-02 18:00:00,home,home\r\n"), out);

        out.close();
        socket.close();
    }
}
