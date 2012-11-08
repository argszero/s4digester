package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-8
 * Time: 下午6:26
 * To change this template use File | Settings | File Templates.
 */
public class DaytimeAgeUpdateEvent extends Event {
    private long age;

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }
}
