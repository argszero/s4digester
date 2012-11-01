package org.s4digester.tourist.pe;

import org.apache.s4.core.ProcessingElement;

/**
 * 合并前两个PE的结果，并从知识库中检测是否符合在网时长规则
 */
public class ConjunctionAndOnlineTimePE extends ProcessingElement {
    private long onlineTime;//在网时长

    @Override
    protected void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRemove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
