package org.s4digester.tourist.pe;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.DefaultedMap;
import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.s4digester.tourist.window.TimeWindow;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * 停留时间的PE，比如
 * 1.从8:00到18点，停留时间大于3个小时的用户和当前日期
 * 2.从18:00到次日8:00,停留时间大于5个小时的用户和次日日期
 * 此PE可以根据用户hash，分布到多个机器上运行
 * 每个PE处理一批用户，该PE需要保存该用户每天的停留时间
 * <p/>
 * 由于信令到达时间可能乱序，因此需要有一个窗口做信令排序，窗口时间为10分钟。
 * 窗口里保存10分钟内用户的所有信令，每隔6秒换一个插槽。
 * 当
 */
public class StayPE extends ProcessingElement {
    private final long from;
    private final long to;
    private final long stay;
    private final Map<String, TimeWindow> userTimeWindow;

    public StayPE(App app, long from, long to, long stay) {
        super(app);
        this.from = from;
        this.to = to;
        this.stay = stay;
        userTimeWindow = DefaultedMap.decorate(new HashMap<String, TimeWindow>(), new Factory() {
            @Override
            public Object create() {
                return new TimeWindow<Event>(10 * 60, 6);
            }
        });
    }


    public void onEvent(Event event) {
        String imsi =  event.get("imsi");
        TimeWindow timeWindow =  userTimeWindow.get(imsi);
        long time = event.get("time", long.class);
        // 将当前信令加入10分钟窗口
        // 1. 当前信令是最新的信令
        //    这时，只需要正常处理
        // 2. 当前信令是10分钟之前的信令
        //    这时，忽略
        // 3. 当前信令是10分钟内的，但不是最新的信令
        //    这时，最近10分钟计算出来的停留信息可能有误，需要计算出差值，然后更新停留时间窗口。
        timeWindow.add(time,event);



        //对于非18-18的数据，不做处理
        if (from > to) {

        }
        System.out.println(format("StayPE(%d-%d:%d)Event:%s", from, to, stay, event.get("imsi")));
    }

    @Override
    protected void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRemove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
