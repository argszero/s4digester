package org.s4digester.tourist.pe;

import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.map.DefaultedMap;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.SignalingEvent;
import org.s4digester.tourist.event.StayScenicDuringDaytimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 白天，[08:00,18:00]在景区停留超过3个小时的用户的PE，满足条件就发出event，可以对同一个用户重复发送。
 */
public class StayScenicDuringDaytimePE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, SingleImsiProcessor> processorMap = DefaultedMap.decorate(new HashMap<String, SingleImsiProcessor>(), InstantiateFactory.getInstance(SingleImsiProcessor.class, new Class[0], new Object[0]));
    private Stream<StayScenicDuringDaytimeEvent>[] streams;

    @Override
    protected void onCreate() {
        logger.info("create StayScenicDuringDaytimePE");
    }

    @Override
    protected void onRemove() {
        logger.info("remove StayScenicDuringDaytimePE");
    }

    public void setStreams(Stream<StayScenicDuringDaytimeEvent>... streams) {
        this.streams = streams;
    }

    @Override
    protected void onTime() { //每天18点定时执行，如果用户最后一次信令在景区，并且直到18点都没有信令，则认为用户一直在。
        for (Map.Entry<String, SingleImsiProcessor> entry : processorMap.entrySet()) {
            StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = entry.getValue().forceCheck(entry.getKey());
            if (stayScenicDuringDaytimeEvent != null) {
                emit(stayScenicDuringDaytimeEvent, streams);
            }
        }
    }

    public void onEvent(SignalingEvent event) {
        SingleImsiProcessor processor = processorMap.get(event.getImsi());
        StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = null;
        stayScenicDuringDaytimeEvent = processor.check(event);
        if (stayScenicDuringDaytimeEvent != null) {
            emit(stayScenicDuringDaytimeEvent, streams);
        }
    }

    /**
     * TODO: 需要考虑Event乱序到达的情况
     */
    private static class SingleImsiProcessor {

        private Status lastStatus = new Status();

        /**
         * 强制检查是否复合条件。如果当前还不符合3个小时，但还在景区，而且到18:00不离开就满3个小时，也算符合条件。
         *
         * @param imsi
         * @return
         */
        public StayScenicDuringDaytimeEvent forceCheck(String imsi) {
            synchronized (lastStatus) {
                if (lastStatus.stayTimeOfToday >= 3 * 60 * 60 * 1000) {
                    StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = new StayScenicDuringDaytimeEvent();
                    stayScenicDuringDaytimeEvent.setAge(getAge(lastStatus.eventTime));
                    stayScenicDuringDaytimeEvent.setImsi(imsi);
                    return stayScenicDuringDaytimeEvent;
                }
                if (lastStatus.isInside = true && (lastStatus.stayTimeOfToday + (18 * 60 * 60 * 1000 - getMillOfToday(lastStatus.eventTime))) >= 3 * 60 * 60 * 1000) {
                    StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = new StayScenicDuringDaytimeEvent();
                    stayScenicDuringDaytimeEvent.setAge(getAge(lastStatus.eventTime));
                    stayScenicDuringDaytimeEvent.setImsi(imsi);
                    return stayScenicDuringDaytimeEvent;
                }
            }
            return null;
        }

        public StayScenicDuringDaytimeEvent check(SignalingEvent event) {
            boolean isInsideNow = isInside(event);
            synchronized (lastStatus) {
                if (isNewCircle(lastStatus.eventTime, event.getTime())) { //如果是新的统计周期，则清空
                    //首先判断老的周期是不是复合条件
                    StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = forceCheck(event.getImsi());
                    lastStatus.eventTime = event.getTime();
                    lastStatus.isInside = isInsideNow;
                    lastStatus.stayTimeOfToday = 0;
                    return stayScenicDuringDaytimeEvent;
                } else {
                    if (!lastStatus.isInside && !isInsideNow) {//一直不在景区
                        lastStatus.eventTime = event.getTime();
                    } else if (lastStatus.isInside && isInsideNow) { //一直在景区
                        lastStatus.stayTimeOfToday += (event.getTime() - lastStatus.eventTime);
                        lastStatus.eventTime = event.getTime();
                    } else if (!lastStatus.isInside && isInsideNow) { //新进入景区
                        lastStatus.eventTime = event.getTime();
                        lastStatus.isInside = isInsideNow;
                    } else if (lastStatus.isInside && !isInsideNow) { //新离开景区
                        lastStatus.stayTimeOfToday += (event.getTime() - lastStatus.eventTime);
                        lastStatus.eventTime = event.getTime();
                        lastStatus.isInside = isInsideNow;
                    }
                    if (lastStatus.stayTimeOfToday > 3 * 60 * 60 * 1000) {
                        StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = new StayScenicDuringDaytimeEvent();
                        stayScenicDuringDaytimeEvent.setAge(getAge(lastStatus.eventTime));
                        stayScenicDuringDaytimeEvent.setImsi(event.getImsi());
                        return stayScenicDuringDaytimeEvent;
                    } else {
                        return null;
                    }
                }

            }
        }

        private long getAge(long time) {
            return time / (24 * 60 * 60 * 1000);
        }

        private long getMillOfToday(long time) {
            return time % (24 * 60 * 60 * 1000);
        }

        private boolean isNewCircle(long lastTime, long time) {
            if (getAge(lastTime) != getAge(time)) {//如果不是同一天
                return false;
            }
            if (getMillOfToday(lastTime) <= 18 * 60 * 60 * 1000 && getMillOfToday(time) > 18 * 60 * 60 * 1000) {//如果一个是18点前，一个是18点后
                return false;
            }
            return true;
        }

        private boolean isInside(SignalingEvent event) {
            //TODO: 需要根据知识库获取
            return true;
        }
    }

    private static class Status {
        private boolean isInside = false;
        private long eventTime = -1;
        private long stayTimeOfToday = 0;
    }
}
