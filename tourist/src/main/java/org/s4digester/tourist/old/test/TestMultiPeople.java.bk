package org.s4digester.tourist.test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;

import static org.s4digester.tourist.test.TestUtils.trans;

/**
 * 测试1000个用户，每个用户每天100条信令，其中当用户为A100,A300,A500时，符合景区条件，其他不符合景区条件
 * 预期结果：JoinAndPrintPE - Latest Tourist List:
 A2
 A4
 A321
 A72
 */
public class TestMultiPeople {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();
        long dayCount = 8;
        long userCount = 400;
        for (int days = 1; days < dayCount; days++) {
            boolean touristDay = days == 1 || days == 2 || days == 4 || days == 5 || days == 6 || days == 7;

            for (int i = 0; i <= userCount; i++) { //所有用户都是8点进入景区
                write(trans(i, "A" + i + ",2012-01-0" + days + " 08:00:00,tourist,tourist\r\n"), out);
            }

            for (int i = 0; i <= userCount; i++) { //非景区游客10点就离开了
                boolean isTourist = isTourist(i);
                if (isTourist && touristDay) {
                } else {
                    write(trans(i, "A" + i + ",2012-01-0" + days + " 10:00:01,home,home\r\n"), out);
                }
            }
            for (int i = 0; i <= userCount; i++) { //景区游客11点开始离开
                boolean isTourist = isTourist(i);
                if (isTourist && touristDay) {
                    write(trans(i, "A" + i + ",2012-01-0" + days + " 11:00:01,home,home\r\n"), out);
                } else {
                }
            }
            for (int i = 0; i <= userCount; i++) {  //所有用户都是18点进入景区
                write(trans(i, "A" + i + ",2012-01-0" + days + " 18:00:00,tourist,tourist\r\n"), out);
            }
            for (int i = 0; i <= userCount; i++) { //非景区游客22点离开
                boolean isTourist = isTourist(i);
                if (isTourist && touristDay) {
                } else {
                    write(trans(i, "A" + i + ",2012-01-0" + days + " 22:00:01,home,home\r\n"), out);
                }
            }
            for (int i = 0; i <= userCount; i++) { //景区游客23点离开
                boolean isTourist = isTourist(i);
                if (isTourist && touristDay) {
                    write(trans(i, "A" + i + ",2012-01-0" + days + " 23:00:01,home,home\r\n"), out);
                } else {
                }
            }
        }
        out.close();
        socket.close();
    }

    private static boolean isTourist(int i) {
        return i == 2 || i == 4 || i == 72 || i == 321;
    }

    private static void write(String data, OutputStream out) throws IOException {
        System.out.println(data);
        IOUtils.write(data, out);
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
