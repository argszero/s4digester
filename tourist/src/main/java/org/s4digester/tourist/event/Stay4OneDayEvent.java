package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 在某一天停留时间符合指定长度的用户
 */
public class Stay4OneDayEvent extends Event {
    private String imsi;
    private long endAge;
    private long start;
    private long end;
    private long stayTime;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public long getEndAge() {
        return endAge;
    }

    public void setEndAge(long endAge) {
        this.endAge = endAge;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getStayTime() {
        return stayTime;
    }

    public void setStayTime(long stayTime) {
        this.stayTime = stayTime;
    }
}
