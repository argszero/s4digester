package org.s4digester.tourist.test;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;

import static org.s4digester.tourist.test.TestUtils.newBuffer;
import static org.s4digester.tourist.test.TestUtils.trans;

/**
 * 测试工作人员的乱序到达
 * 8点收到进入景区的信令
 * 11:03收到离开景区的信令，判断用户在景区超过3个小时(3小时零3分钟)
 * 10:59收到乱序延迟到达的用户离开景区的信令，判断用户其实在景区不足3个小时(2小时59分钟)
 * 11:00收到用户进入景区的信令，判断用户离开景区后有进入景区，累计时间超过3个小时(3小时1分钟)
 */
public class TestWorkerOrder {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();
        IOUtils.write(trans("A,2012-01-01 08:00:00,tourist,tourist\r\n"), out);
        IOUtils.write(trans("A,2012-01-01 11:03:00,home,home\r\n"), out);
        //这时候应该误判为该用户在景区停留了3小时零3分钟
        //预期日志：StayDaysPE - daytime:receive StayHoursEvent:{"imsi":"A","endAge":15340,"matches":true
        IOUtils.write(trans("A,2012-01-01 10:59:00,home,home\r\n"), out);
        //这时候应该修改为该用户只在景区停留了2小时59分钟
        //预期日志：StayDaysPE - daytime:receive StayHoursEvent:{"imsi":"A","endAge":15340,"matches":false
        IOUtils.write(trans("A,2012-01-01 11:00:00,tourist,tourist\r\n"), out);
        //这时候，应该判断用户在景区停留了3小时1分钟
        //预期日志：StayDaysPE - daytime:receive StayHoursEvent:{"imsi":"A","endAge":15340,"matches":true

        out.close();
        socket.close();
    }
}
