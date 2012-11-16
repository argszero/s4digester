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

import static org.s4digester.tourist.test.TestUtils.trans;

/**
 * 测试刚好符合条件的景区工作人员：连续五天，每天早上8点到景区，11:00:01离开。下午18:00到景区，23:00:01离开。
 * 预期结果：
 * 日志：
 * StayDaysPE - daytime:receive Signaling:{"imsi":"A","endAge":15341,"matches":true //白天符合条件5次
 * StayDaysPE - night:receive Signaling:{"imsi":"A","endAge":15341,"matches":true //晚上符合条件5次
 * TouristPE - new worker:A //判断出A是景区工作人员
 */
public class TestSampleWorker {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();

//        IOUtils.write(trans("A,2012-01-01 08:00:00,tourist,tourist\r\n"), out);
        IOUtils.write(trans("A,2012-01-01 11:00:01,home,home\r\n"), out);
//        IOUtils.write(trans("A,2012-01-01 18:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-01 23:00:01,home,home\r\n"), out);
//
//        IOUtils.write(trans("A,2012-01-02 08:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-02 11:00:01,home,home\r\n"), out);
//        IOUtils.write(trans("A,2012-01-02 18:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-02 23:00:01,home,home\r\n"), out);
//
//        IOUtils.write(trans("A,2012-01-03 08:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-03 11:00:01,home,home\r\n"), out);
//        IOUtils.write(trans("A,2012-01-03 18:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-03 23:00:01,home,home\r\n"), out);
//
//        IOUtils.write(trans("A,2012-01-04 08:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-04 11:00:01,home,home\r\n"), out);
//        IOUtils.write(trans("A,2012-01-04 18:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-04 23:00:01,home,home\r\n"), out);
//
//        IOUtils.write(trans("A,2012-01-05 08:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-05 11:00:01,home,home\r\n"), out);
//        IOUtils.write(trans("A,2012-01-05 18:00:00,tourist,tourist\r\n"), out);
//        IOUtils.write(trans("A,2012-01-05 23:00:01,home,home\r\n"), out);



        out.close();
        socket.close();
    }


}
