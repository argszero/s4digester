package org.s4digester.tourist.pe;

import org.apache.s4.core.ProcessingElement;

/**
 * 输出对于某个条件，如果满足指定的天数的用户，比如：
 * 1. 10天内，（每天8:00-18:00在景区停留时长超过3小时）的天数小于5天
 * 2. 10天内，（每天18:00到次日8:00在景区停留超过5小时）的天数小于5天
 *
 * 此PE只在一个机器上运行
 */
public class ConditionDaysPE extends ProcessingElement {
    private long durationDays; //在多少天以内
    private long threshold; //比较阀值

    @Override
    protected void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRemove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
