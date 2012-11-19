package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 用户进入和离开景区的事件。
 * 当用户进入景区时，由TouristPE判断该用户是否为工作人员，是的话加入景区游客
 * 当用户离开景区时，TouristPE将其从游客列表中排除。
 */
public class EnterOrLeaveEvent extends Event {
    private String imsi;
    private boolean enter; //进入，反之为离开

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public boolean isEnter() {
        return enter;
    }

    public void setEnter(boolean enter) {
        this.enter = enter;
    }
}
