package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-8
 * Time: 下午2:45
 * To change this template use File | Settings | File Templates.
 */
public class Daytime5In10AgeChangeEvent extends Event {
    private long age;

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }
}
