package org.s4digester.tourist.pe;

import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.StayScenicDuringNightEvent;

/**
 * 晚上，[18:00-08:00]在景区停留超过5个小时的用户的PE
 */
public class StayScenicDuringNightPE extends ProcessingElement {
    @Override
    protected void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRemove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStream(Stream<StayScenicDuringNightEvent> stayScenicDuringNight) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
