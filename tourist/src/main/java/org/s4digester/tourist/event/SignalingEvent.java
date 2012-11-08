package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 信令事件
 */
public class SignalingEvent extends Event {
    private String imsi;
    private long time;
    private String loc;
    private String cell;


    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }
}
