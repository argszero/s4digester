package org.s4digester.tourist.pe;

import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.StayScenicDuringDaytimeEvent;

/**
 * 白天，[08:00,18:00]在景区停留超过3个小时的用户的PE
 */
public class StayScenicDuringDaytimePE extends ProcessingElement {
    @Override
    protected void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRemove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStream(Stream<StayScenicDuringDaytimeEvent> stream) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
