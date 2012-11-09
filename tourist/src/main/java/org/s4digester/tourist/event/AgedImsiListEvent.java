package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 从哪一天之前10天内大于5点的 指定用户(imsi),是否符合条件(matches)
 */
public class AgedImsiListEvent extends Event {
    private String imsi;

    private boolean matches;

    private long toAge;  //好像没啥用

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

    public long getToAge() {
        return toAge;
    }

    public void setToAge(long toAge) {
        this.toAge = toAge;
    }
}
