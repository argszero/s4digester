package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 在某一天停留时间符合条件，比如一天内至少停留3个小时
 */
public class StayHoursEvent extends Event {
    private String imsi;
    private long endAge;
    private boolean matches;
    private String statisticsName;

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

    public String getStatisticsName() {
        return statisticsName;
    }

    public void setStatisticsName(String statisticsName) {
        this.statisticsName = statisticsName;
    }

    public boolean isMatches() {
        return matches;
    }

    public void setMatches(boolean matches) {
        this.matches = matches;
    }
}
