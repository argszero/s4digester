package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.NextMillOfDayUpdateEvent;
import org.s4digester.tourist.event.SignalingEvent;
import org.s4digester.tourist.event.Stay4OneDayEvent;
import org.s4digester.tourist.event.StayScenicDuringDaytimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static org.s4digester.tourist.util.TimeUtil.*;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-13
 * Time: 下午6:06
 * To change this template use File | Settings | File Templates.
 */
public class Stay4OneDayPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, SingleImsiProcessor> processorMap = LazyMap.decorate(new HashMap<String, SingleImsiProcessor>(), InstantiateFactory.getInstance(SingleImsiProcessor.class, new Class[0], new Object[0]));
    private Stream<Stay4OneDayEvent>[] streams;
    //当前时间所属的统计周期结束时间所在的日期。
    //对于白天8~18的统计，到达这天的18点时要强制统计一下。 计算方法即当前时间之后最近的18点所在的日期
    //对于晚上18到第二天早上8点的统计，到达这天的8点后要强制统计一下。计算方法即当前时间之后最近的8点所在的日期
    private long endAge;
    //每天的固定时间点到了。在这个case中，每天的8点和18点都会接收到这个事件。
    private Stream<NextMillOfDayUpdateEvent>[] nextMillOfDayUpdateEventStreams;

    private final long start; //开始时间，比如白天统计开始时间为8点，即8*60*60*1000  ，晚上统计开始时间为18点，即18*60*60*1000
    private final long end;  //结束时间，比如白天统计结束时间为18点，即18*60*60*1000  ，晚上统计结束时间为8点，即8*60*60*1000
    private final long stayTime;//停留时间阀值 ，比如3个小时，即 3*60*60*1000

    public Stay4OneDayPE(App app, long start, long end, long stayTime) {
        super(app);
        this.start = start;
        this.end = end;
        this.stayTime = stayTime;
    }

    @Override
    protected void onCreate() {
        logger.info("create Stay4OneDayPE[{}:{}~{}:{}]", new Object[]{getHour(start), getMinute(start), getHour(end), getMinute(end)});
        logger.info(format("ZONE_OFFSET:%d", Calendar.getInstance().get(Calendar.ZONE_OFFSET)));
    }

    @Override
    protected void onRemove() {
        logger.info("remove Stay4OneDayPE[{}:{}~{}:{}]", new Object[]{getHour(start), getMinute(start), getHour(end), getMinute(end)});
    }

    public void onEvent(SignalingEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("receive Signaling:{}", new Gson().toJson(event));
        }
        long eventAge = getNextAge(event.getSignalingTime(), end);
        if (eventAge > endAge) { //一个统计周期结束，需要发出通知。对于白天的统计任务，18点之后发出通知，对于晚上的统计人，8点后发出通知
            if (logger.isTraceEnabled()) {
                logger.trace("new endAge:[{} -> {}]", endAge, eventAge);
            }
            NextMillOfDayUpdateEvent nextMillOfDayUpdateEvent = new NextMillOfDayUpdateEvent();
            nextMillOfDayUpdateEvent.setAge(eventAge);
            nextMillOfDayUpdateEvent.setMillOfDay(end);
            nextMillOfDayUpdateEvent.setEventTime(event.getSignalingTime());
            emit(nextMillOfDayUpdateEvent, nextMillOfDayUpdateEventStreams);
        }
        SingleImsiProcessor processor = processorMap.get(event.getImsi());
        processor.process(event, this, streams);
    }

    public void onEvent(NextMillOfDayUpdateEvent event) {
        if (event.getMillOfDay() == end) { //如果是当前的PE统计周期结束
            if (endAge != event.getAge()) { //如果当前的age还没有更新（如果已经更新了则忽略)
                endAge = event.getAge();
                //统计周期结束，强制检查本统计周期内没有离开景区的用户。比如，对于白天的统计，用户8点进入景区，一直到晚上18点都没有其他信令。我们这个时候也认为用户已经够3个小时了。
                //TODO: 其实不应该等到18点后再检查，用户如果9点进入景区，12点之前都没有收到其他信令，则应该认为用户已经在景区停留3个小时了。
                for (Map.Entry<String, SingleImsiProcessor> entry : processorMap.entrySet()) {
                    //我们认为
                    entry.getValue().forceCheck(event.getEventTime(), this, streams, entry.getKey(), entry.getValue().lastStatus.isInside);
                }
            }
        }
    }

    @Override
    public <T extends Event> void emit(T event, Stream<T>[] streamArray) {
        super.emit(event, streamArray);
    }

    public void setNextMillOfDayUpdateEventStreams(Stream<NextMillOfDayUpdateEvent>... nextMillOfDayUpdateEventStreams) {
        this.nextMillOfDayUpdateEventStreams = nextMillOfDayUpdateEventStreams;
    }

    public void setStreams(Stream<Stay4OneDayEvent>... streams) {
        this.streams = streams;
    }


    /**
     * 不考虑Event乱序的单用户处理器
     */
    private static class SingleImsiProcessor {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private Status lastStatus = new Status();

        public void process(SignalingEvent event, Stay4OneDayPE pe, Stream<Stay4OneDayEvent>[] streams) {
            boolean isInsideNow = isInside(event);
            synchronized (lastStatus) {
                long lastEndAge = getNextAge(lastStatus.getEventTime(), pe.end);
                long signalingAge = getNextAge(event.getSignalingTime(), pe.end);
                if (lastEndAge != signalingAge) { //如果是新的统计周期，则清空
                    if (logger.isTraceEnabled()) {
                        logger.trace("new circle:[{} -> {}]", getDate(lastEndAge), getDate(signalingAge));
                    }
                    //判断老的周期是不是符合条件，并将状态更新为新的周期
                    forceCheck(event.getSignalingTime(), pe, streams, event.getImsi(), isInsideNow);
                } else { //同一次统计周期  lastEndAge== signalingAge
                    long start = (pe.start < pe.end ? lastEndAge : lastEndAge - 1) * 24 * 60 * 60 * 1000 + pe.start; //统计起点，对于白天，则为当天，对于晚上，为结束的头一天
                    long end = lastEndAge * 24 * 60 * 60 * 1000 + pe.end; //统计终点
                    if (logger.isTraceEnabled()) {
                        logger.trace("imsi:{},lastStatus.isInside:{},lastTime:{},isInsideNow:{},nowTime:{}", new Object[]{event.getImsi(), lastStatus.isInside, lastStatus.getEventTime(), isInsideNow, event.getSignalingTime()});
                    }
                    if (!lastStatus.isInside && !isInsideNow) {//一直不在景区
                        lastStatus.setEventTime(event.getSignalingTime());
                    } else if (lastStatus.isInside && isInsideNow) { //一直在景区。
                        // 为什么不是：Math.min(end, event.getSignalingTime()) - Math.max(lastStatus.getEventTime(), start)？
                        // 假设signalingTime 为20点， lastTime为19点  min(18,20) - max(19,8) = 18-19 = -1
                        //这种情况下，应该为min(18,20) - min( max(19,8),18) = 0 才对
                        lastStatus.stayTimeOfToday += max(min(end, event.getSignalingTime()), start) - min(max(lastStatus.getEventTime(), start), end);
                        lastStatus.setEventTime(event.getSignalingTime());
                    } else if (!lastStatus.isInside && isInsideNow) { //新进入景区
                        lastStatus.setEventTime(event.getSignalingTime());
                        lastStatus.isInside = isInsideNow;
                    } else if (lastStatus.isInside && !isInsideNow) { //新离开景区
                        lastStatus.stayTimeOfToday += max(min(end, event.getSignalingTime()), start) - min(max(lastStatus.getEventTime(), start), end);
                        lastStatus.setEventTime(event.getSignalingTime());
                        lastStatus.isInside = isInsideNow;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("imsi[{}],stayTimeOfToday[{}]", event.getImsi(), lastStatus.stayTimeOfToday);
                    }
                    if (lastStatus.stayTimeOfToday > pe.stayTime) {
                        Stay4OneDayEvent stay4OneDayEvent = new Stay4OneDayEvent();
                        stay4OneDayEvent.setEndAge(lastEndAge);
                        stay4OneDayEvent.setImsi(event.getImsi());
                        pe.emit(stay4OneDayEvent, streams);
                    }
                }

            }
        }

        private boolean isInside(SignalingEvent event) {
            //TODO: 需要根据知识库获取
            return ("tourist".equals(event.getCell()));
        }

        public void forceCheck(long eventTime, Stay4OneDayPE pe, Stream<Stay4OneDayEvent>[] streams, String imsi, boolean insideNow) {
            synchronized (lastStatus) {
                StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = null;
                if (lastStatus.stayTimeOfToday <= pe.stayTime //未超过指定时间的（因为超过的已经发送过了）
                        && lastStatus.isInside  //最后一次出声时在景区（如果用户已经离开景区，也不需要计算了）
                        && (lastStatus.stayTimeOfToday + (pe.end - getMillOfToday(lastStatus.getEventTime()))) > pe.stayTime) { //TODO: 这里有问题，如果最后用户时间为23点，8点检查时怎么办？
                    Stay4OneDayEvent stay4OneDayEvent = new Stay4OneDayEvent();
                    stay4OneDayEvent.setEndAge(getNextAge(lastStatus.getEventTime(), pe.end));
                    stay4OneDayEvent.setImsi(imsi);
                    pe.emit(stay4OneDayEvent, streams);
                }
                lastStatus.setEventTime(eventTime);
                lastStatus.isInside = insideNow;
                lastStatus.stayTimeOfToday = 0;
            }


            Stay4OneDayEvent stay4OneDayEvent = null;

            if (stay4OneDayEvent != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("emit event: {}", new Gson().toJson(stay4OneDayEvent));
                }
                pe.emit(stay4OneDayEvent, streams);
            }
        }

        private static class Status {
            private boolean isInside = false;
            private long eventTime = -1;
            private long stayTimeOfToday = 0;

            public long getEventTime() {
                return eventTime;
            }

            public void setEventTime(long eventTime) {
                this.eventTime = eventTime;
            }
        }
    }
}
