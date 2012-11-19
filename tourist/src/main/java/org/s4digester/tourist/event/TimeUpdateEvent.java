package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-15
 * Time: 下午6:38
 * To change this template use File | Settings | File Templates.
 */
public class TimeUpdateEvent extends Event {
    private long signalingTime;

    public long getSignalingTime() {
        return signalingTime;
    }

    public void setSignalingTime(long signalingTime) {
        this.signalingTime = signalingTime;
    }
}
