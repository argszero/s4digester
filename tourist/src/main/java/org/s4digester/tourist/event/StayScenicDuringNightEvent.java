package org.s4digester.tourist.event;

import org.apache.s4.base.Event;

/**
 * 晚上，[18:00-08:00]在景区停留超过5个小时的用户
 */
public class StayScenicDuringNightEvent extends Event {
    private String imsi;
    private long age; //哪一天，使用当前日期和1970-01-01直接相差的天数表示。当统计17号晚上到18号早晨的数据时，这个值为17号。

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }
}
