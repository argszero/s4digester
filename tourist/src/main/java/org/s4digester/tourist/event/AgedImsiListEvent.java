package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 指定用户(imsi),是否符合条件(matches)
 */
public class AgedImsiListEvent extends Event {
    private String imsi;

    private boolean matches;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public boolean isMatches() {
        return matches;
    }

    public void setMatches(boolean matches) {
        this.matches = matches;
    }
}
