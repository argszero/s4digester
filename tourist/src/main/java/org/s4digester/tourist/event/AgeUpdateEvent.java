package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 统计周期变更事件，白天8~18点的统计，18点后统计周期变更为第二天。晚上18:00到第二天早上8点的统计，8点以后统计周期变更为第二天
 */
public class AgeUpdateEvent extends Event {
    private long age;
    private String statisticsName;
    private long eventTime;

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public String getStatisticsName() {
        return statisticsName;
    }

    public void setStatisticsName(String statisticsName) {
        this.statisticsName = statisticsName;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
