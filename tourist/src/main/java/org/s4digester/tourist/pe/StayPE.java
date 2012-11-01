package org.s4digester.tourist.pe;

import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;

import static java.lang.String.format;

/**
 * 停留时间的PE，比如
 * 1.从8:00到18点，停留时间大于3个小时的用户和当前日期
 * 2.从18:00到次日8:00,停留时间大于5个小时的用户和次日日期
 * <p/>
 * 此PE可以根据用户hash，分布到多个机器上运行
 */
public class StayPE extends ProcessingElement {
    private final long from;
    private final long to;
    private final long stay;

    public StayPE(App app, long from, long to, long stay) {
        super(app);
        this.from = from;
        this.to = to;
        this.stay = stay;
    }

    public void onEvent(Event event) {
        System.out.println(format("StayPE(%d-%d:%d)Event:%s", event.get("imsi")));
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
