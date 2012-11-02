package org.s4digester.tourist;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.s4digester.tourist.pe.StayPE;

import java.util.Arrays;
import java.util.List;

/**
 * 用于检测景区游客的app
 * 选取最近10天内满足下面条件的用户
 * 1：每天8:00-18:00在景区停留时长超过3小时天数小于5天
 * 2：每天18:00到次日8:00在景区停留超过5小时小于5天
 * 3: 在网时长超过3个月
 */
public class TouristApp extends App {
    @Override
    protected void onStart() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onInit() {
        StayPE dayStayPE = new StayPE(this, 8, 18, 3);
        StayPE nightStayPE = new StayPE(this, 18, 8, 3);
        //创建一个输入流，侦听所有信令Signaling
        createInputStream("Signaling", new KeyFinder<Event>() {
            @Override
            public List<String> get(Event event) {
                return Arrays.asList(new String[]{event.get("imsi"), event.get("time"), event.get("lac"), event.get("cell")});
            }
        }, dayStayPE,nightStayPE);

    }

    @Override
    protected void onClose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
