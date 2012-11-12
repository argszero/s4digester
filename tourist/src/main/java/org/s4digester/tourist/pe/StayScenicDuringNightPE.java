package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.map.DefaultedMap;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.NextMillOfDayUpdateEvent;
import org.s4digester.tourist.event.SignalingEvent;
import org.s4digester.tourist.event.StayScenicDuringNightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.s4digester.tourist.util.TimeUtil.getAge;
import static org.s4digester.tourist.util.TimeUtil.getMillOfToday;

/**
 * 晚上，[18:00-08:00]在景区停留超过5个小时的用户的PE，满足条件就发出event，可以对同一个用户重复发送。
 * 1. 如果用户1点进入景区， 10点离开。如何在8:00时判断用户符合条件？
 * 对所有用户的信令取最新的时间，如果最新时间为8:00时，触发检测
 */
public class StayScenicDuringNightPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, SingleImsiProcessor> processorMap = DefaultedMap.decorate(new HashMap<String, SingleImsiProcessor>(), InstantiateFactory.getInstance(SingleImsiProcessor.class, new Class[0], new Object[0]));
    private Stream<StayScenicDuringNightEvent>[] streams;
    //如果用户1点进入景区， 10点离开。如何在8:00时判断用户符合条件？
    // 因此，对于所有信令，一旦到达或超过8:00，就说统计周期变更，需要检测用户是不是符合条件了
    //这个变量保存最近的8点属于哪一天。比如，最新event17号1点，则值为17号。如果为17号10点，则为18号
    private long next8Age;
    private Stream<NextMillOfDayUpdateEvent>[] nextMillOfDayUpdateEventStreams;

    @Override
    protected void onCreate() {
        logger.info("create StayScenicDuringNightPE");
    }

    @Override
    protected void onRemove() {
        logger.info("remove StayScenicDuringNightPE");
    }

    public void setStreams(Stream<StayScenicDuringNightEvent>... streams) {
        this.streams = streams;
    }

    public void onEvent(SignalingEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("receive Signaling:[{}]", new Gson().toJson(event));
        }
        long eventAge8 = getAge8(event.getSignalingTime());
        if (eventAge8 > next8Age) {
            if (logger.isTraceEnabled()) {
                logger.trace("new age:[{} - {}]", next8Age, eventAge8);
            }
            NextMillOfDayUpdateEvent nextMillOfDayUpdateEvent = new NextMillOfDayUpdateEvent();
            nextMillOfDayUpdateEvent.setAge(eventAge8);
            nextMillOfDayUpdateEvent.setMillOfDay(8 * 60 * 60 * 1000);
            emit(nextMillOfDayUpdateEvent, nextMillOfDayUpdateEventStreams);
        }
        SingleImsiProcessor processor = processorMap.get(event.getImsi());
        StayScenicDuringNightEvent stayScenicDuringNightEvent = null;
        stayScenicDuringNightEvent = processor.check(event);
        if (stayScenicDuringNightEvent != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("emit event: {}", new Gson().toJson(stayScenicDuringNightEvent));
            }
            emit(stayScenicDuringNightEvent, streams);
        }
    }

    public static long getAge8(long eventTime) {
        long age = getAge(eventTime);
        long millOfToday = getMillOfToday(eventTime);
        return millOfToday <= 8 * 60 * 60 * 1000 ? age : age + 1;
    }

    public void onEvent(NextMillOfDayUpdateEvent event) {
        if (event.getMillOfDay() == 8 * 60 * 60 * 1000) {
            if (next8Age != event.getAge()) {
                next8Age = event.getAge();
                //每天8点定时执行，如果用户最后一次信令在景区，并且直到8点都没有信令，则认为用户一直在。
                for (Map.Entry<String, SingleImsiProcessor> entry : processorMap.entrySet()) {
                    StayScenicDuringNightEvent stayScenicDuringNightEvent = entry.getValue().forceCheckAndUpdateStatus(entry.getKey(), event.getAge() * 24 * 60 * 60 * 1000 + event.getMillOfDay(), entry.getValue().lastStatus.isInside);
                    if (stayScenicDuringNightEvent != null) {
                        emit(stayScenicDuringNightEvent, streams);
                    }
                }
            }
        }
    }

    public void setNextMillOfDayUpdateEventStreams(Stream<NextMillOfDayUpdateEvent>... nextMillOfDayUpdateEventStreams) {
        this.nextMillOfDayUpdateEventStreams = nextMillOfDayUpdateEventStreams;
    }

    /**
     * TODO: 需要考虑Event乱序到达的情况
     */
    public static class SingleImsiProcessor {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private Status lastStatus = new Status();

        public SingleImsiProcessor() {
        }

        /**
         * 强制检查是否复合条件。如果当前还不符合5个小时，但还在景区，而且到8:00不离开就满5个小时，也算符合条件。
         *
         * @param imsi
         * @return
         */
        public StayScenicDuringNightEvent forceCheckAndUpdateStatus(String imsi, long eventTime, boolean inside) {
            synchronized (lastStatus) {
                StayScenicDuringNightEvent stayScenicDuringNightEvent = null;
                if (lastStatus.stayTimeOfToday < 5 * 60 * 60 * 1000
                        && lastStatus.isInside
                        && (lastStatus.stayTimeOfToday + (8 * 60 * 60 * 1000 - getMillOfToday(lastStatus.eventTime))) >= 5 * 60 * 60 * 1000) {
                    stayScenicDuringNightEvent = new StayScenicDuringNightEvent();
                    stayScenicDuringNightEvent.setAge(getAge8(lastStatus.eventTime));
                    stayScenicDuringNightEvent.setImsi(imsi);
                }
                lastStatus.eventTime = eventTime;
                lastStatus.isInside = inside;
                lastStatus.stayTimeOfToday = 0;
                return stayScenicDuringNightEvent;
            }
        }

        public StayScenicDuringNightEvent check(SignalingEvent event) {
            boolean isInsideNow = isInside(event);
            synchronized (lastStatus) {
                if (isNewCircle(lastStatus.eventTime, event.getSignalingTime())) { //如果是新的统计周期，则清空
                    if (logger.isTraceEnabled()) {
                        logger.trace("new circle:[{} - {}]", getAge8(lastStatus.eventTime), getAge8(event.getSignalingTime()));
                    }
                    //首先判断老的周期是不是复合条件
                    StayScenicDuringNightEvent stayScenicDuringNightEvent = forceCheckAndUpdateStatus(event.getImsi(), event.getSignalingTime(), isInsideNow);
                    return stayScenicDuringNightEvent;
                } else {
                    if (!lastStatus.isInside && !isInsideNow) {//一直不在景区
                        lastStatus.eventTime = event.getSignalingTime();
                    } else if (lastStatus.isInside && isInsideNow) { //一直在景区
                        lastStatus.stayTimeOfToday += (event.getSignalingTime() - lastStatus.eventTime);
                        lastStatus.eventTime = event.getSignalingTime();
                    } else if (!lastStatus.isInside && isInsideNow) { //新进入景区
                        lastStatus.eventTime = event.getSignalingTime();
                        lastStatus.isInside = isInsideNow;
                    } else if (lastStatus.isInside && !isInsideNow) { //新离开景区
                        lastStatus.stayTimeOfToday += (event.getSignalingTime() - lastStatus.eventTime);
                        lastStatus.eventTime = event.getSignalingTime();
                        lastStatus.isInside = isInsideNow;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("imsi[{}] stayTimeOfToday[{}]", event.getImsi(), lastStatus.stayTimeOfToday);
                    }
                    if (lastStatus.stayTimeOfToday > 5 * 60 * 60 * 1000) {
                        StayScenicDuringNightEvent stayScenicDuringNightEvent = new StayScenicDuringNightEvent();
                        stayScenicDuringNightEvent.setAge(getAge(lastStatus.eventTime));
                        stayScenicDuringNightEvent.setImsi(event.getImsi());
                        return stayScenicDuringNightEvent;
                    } else {
                        return null;
                    }
                }

            }
        }


        private boolean isNewCircle(long lastTime, long time) {
            return getAge8(lastTime) != getAge8(time);
        }

        private boolean isInside(SignalingEvent event) {
            //TODO: 需要根据知识库获取
            return ("tourist".equals(event.getCell()));
        }
    }

    private static class Status {
        private boolean isInside = false;
        private long eventTime = -1;
        private long stayTimeOfToday = 0;
    }
}
