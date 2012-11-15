package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 用户停留的天数，是否符合条件,比如10天内至少5天
 */
public class StayDaysEvent extends Event {
    private String imsi;

    private boolean matches;

    private String statisticsName;

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

    public String getStatisticsName() {
        return statisticsName;
    }

    public void setStatisticsName(String statisticsName) {
        this.statisticsName = statisticsName;
    }
}
