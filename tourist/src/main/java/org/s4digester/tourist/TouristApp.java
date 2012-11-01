package org.s4digester.tourist;

import org.apache.s4.core.App;

/**
 * 用于检测景区游客的app
 * 选取最近10天内满足下面条件的用户
 * 1：每天8:00-18:00在景区停留时长超过3小时天数小于5天
 * 2：每天18:00到次日8:00在景区停留超过5小时小于5天
 * 3: 在网时长超过3个月
 */
public class TouristApp  extends App {
    @Override
    protected void onStart() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onInit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onClose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
