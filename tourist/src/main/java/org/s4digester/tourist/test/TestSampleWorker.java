package org.s4digester.tourist.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;

import static org.s4digester.tourist.test.TestUtils.newBuffer;

/**
 * 测试景区工作人员：Worker1（白班）： 连续5天，每天08上班，12点下班； 工作4个小时，大于3个小时。（要是咱们也工作4小时多好！）
 * 测试景区工作人员：Worker2（夜班）： 连续5天，每天18上班，23点01下班； 工作5个小时零一分钟，大于5个小时（夜班时间长？）
 * 测试景区工作人员：Worker3（夜班）： 连续5天，每天19上班，第二天01点01下班； 工作5个小时零一分钟，大于5个小时 （测试工作时间跨天的）
 * 测试景区工作人员：Worker4（早班）： 连续5天，每天早上2点上班，7点01点下班； 工作5个小时零一分钟，大于5个小时
 * Tourist1: 白天晨练，连续5天，每天08开始锻炼，9点结束； 晨练1个小时，小于3个小时。
 * Tourist2: 看夜景，连续5天，每天18开始锻炼，19点结束； 看1个小时，小于5个小时。
 * Tourist3: 看夜景，连续5天，每天23开始锻炼，第二天1点结束； 看2个小时，小于5个小时。
 * Tourist4: 看日出，连续5天，每天05开始锻炼，第二天07点结束； 看2个小时，小于5个小时。
 * <p/>
 * 预期结果：
 * 日志：
 * StayDaysPE - daytime:receive Signaling:{"imsi":"A","endAge":15341,"matches":true //白天符合条件5次
 * StayDaysPE - night:receive Signaling:{"imsi":"A","endAge":15341,"matches":true //晚上符合条件5次
 * TouristPE - new worker:Worker1，Worker2，Worker3，Worker4 //判断出Worker1，Worker2，Worker3，Worker4是景区工作人员
 */
public class TestSampleWorker {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = new Socket("10.1.253.24", 15000);
        OutputStream out = socket.getOutputStream();
        int endDays = 7;
        int lastDays = endDays + 1;
        for (int day = 1; day <= endDays; day++) {
            newBuffer()
                    .add("Worker1", "08:00:00", "12:00:00", day > 0 && day < endDays, day > 0 && day < endDays)
                    .add("Worker2", "18:00:00", "23:01:00", day > 0 && day < endDays, day > 0 && day < endDays)
                    .add("Worker3", "19:00:00", "01:01:00", day > 0 && day < endDays, day > 1 && day < lastDays)
//                    .add("Worker4", "02:00:00", "07:01:00", day > 0 && day < endDays, day > 0 && day < endDays)
//                    .add("Tourist1", "08:00:00", "09:00:00", day > 0 && day < endDays, day > 0 && day < endDays)
//                    .add("Tourist2", "18:00:00", "19:00:00", day > 0 && day < endDays, day > 0 && day < endDays)
//                    .add("Tourist3", "23:00:00", "01:00:00", day > 0 && day < endDays, day > 1 && day < lastDays)
//                    .add("Tourist4", "05:00:00", "07:00:00", day > 0 && day < endDays, day > 0 && day < endDays)
                    .write(out, day);
        }
        out.close();
        socket.close();
    }

}
