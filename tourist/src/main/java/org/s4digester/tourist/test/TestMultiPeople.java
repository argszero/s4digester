package org.s4digester.tourist.test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;

import static org.s4digester.tourist.test.TestUtils.trans;

/**
 * 测试1000个用户，每个用户每天100条信令，其中当用户为A100,A300,A500时，符合景区条件，其他不符合景区条件
 */
public class TestMultiPeople {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();
        for (int days = 1; days < 8; days++) {
//            noTourist(1, days, out);
//            tourist(2, days, out);
//            noTourist(3, days, out);
            tourist(4, days, out);
//            for (int i = 0; i <= 10; i++) {
//                if (i == 5 || i == 7 || i == 4) {
//                    tourist(i, days, out);
//                } else {
//                    noTourist(i, days, out);
//                }
//            }
        }
        out.close();
        socket.close();
    }

    private static void noTourist(int i, int days, OutputStream out) throws ParseException, IOException {
        IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 08:00:00,tourist,tourist\r\n"), out);
        IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 10:00:01,home,home\r\n"), out);
        IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 18:00:00,tourist,tourist\r\n"), out);
        IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 22:00:01,home,home\r\n"), out);
    }

    private static void tourist(int i, int days, OutputStream out) throws ParseException, IOException {
        if (days == 1 || days == 2 || days == 4 || days == 5 || days == 6 || days == 7) {
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 08:00:00,tourist,tourist\r\n"), out);
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 11:00:01,home,home\r\n"), out);
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 18:00:00,tourist,tourist\r\n"), out);
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 23:00:01,home,home\r\n"), out);
        } else {
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 08:00:00,tourist,tourist\r\n"), out);
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 10:00:01,home,home\r\n"), out);
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 18:00:00,tourist,tourist\r\n"), out);
            IOUtils.write(trans(i, "A" + i + ",2012-01-0" + days + " 22:00:01,home,home\r\n"), out);
        }
    }
}
