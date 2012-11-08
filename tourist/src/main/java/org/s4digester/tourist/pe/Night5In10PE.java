package org.s4digester.tourist.pe;

import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.Daytime5In10Event;
import org.s4digester.tourist.event.Night5In10Event;

/**
 *  晚上10天内满足条件大于5天的用户
 */
public class Night5In10PE extends ProcessingElement {
    @Override
    protected void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRemove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStreams(Stream<Night5In10Event> streams) {
    }
}
