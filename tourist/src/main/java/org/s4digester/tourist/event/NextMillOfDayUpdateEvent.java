package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-9
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class NextMillOfDayUpdateEvent  extends Event {
    private long millOfDay;
    private long age;

    public long getMillOfDay() {
        return millOfDay;
    }

    public void setMillOfDay(long millOfDay) {
        this.millOfDay = millOfDay;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }
}
