package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-7
 * Time: 下午7:01
 * To change this template use File | Settings | File Templates.
 */
public class AgedImsiListEvent extends Event {
    private List<String> imsiList;
    private long lastAge;

    public List<String> getImsiList() {
        return imsiList;
    }

    public void setImsiList(List<String> imsiList) {
        this.imsiList = imsiList;
    }


    public void setLastAge(long lastAge) {
        this.lastAge = lastAge;
    }

    public long getLastAge() {
        return lastAge;
    }
}
