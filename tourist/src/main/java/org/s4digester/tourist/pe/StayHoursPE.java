package org.s4digester.tourist.pe;

import net.jcip.annotations.ThreadSafe;
import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.AgeUpdateEvent;
import org.s4digester.tourist.event.SignalingEvent;
import org.s4digester.tourist.event.StayHoursEvent;
import org.s4digester.tourist.event.TimeUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.String.format;
import static org.s4digester.tourist.util.TimeUtil.*;

/**
 * 每个Imsi一个实例
 */
@ThreadSafe
public class StayHoursPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Stream<StayHoursEvent>[] streams;
    private long endAge;
    //每天的固定时间点到了。在这个case中，每天的8点和18点都会接收到这个事件。
    private Stream<AgeUpdateEvent>[] ageUpdateEventStreams = new Stream[0];
    private Stream<TimeUpdateEvent>[] timeUpdateEventStreams = new Stream[0];

    private final long start; //开始时间，比如白天统计开始时间为8点，即8*60*60*1000  ，晚上统计开始时间为18点，即18*60*60*1000
    private final long end;  //结束时间，比如白天统计结束时间为18点，即18*60*60*1000  ，晚上统计结束时间为8点，即8*60*60*1000
    private final long stayTime;//停留时间阀值 ，比如3个小时，即 3*60*60*1000
    private String statisticsName;
    private Status status = new Status();


    public StayHoursPE(App app, long start, long end, long stayTime, String statisticsName) {
        super(app);
        this.start = start;
        this.end = end;
        this.stayTime = stayTime;
        this.statisticsName = statisticsName;
    }

    @Override
    protected void onCreate() {
        setName(format("StayHoursPE[%d:%d~%d:%d > %d:%d]", getHour(start), getMinute(start), getHour(end), getMinute(end), getHour(stayTime), getMinute(stayTime)));
        logger.info("create {}", statisticsName);
        logger.info(format("%s:ZONE_OFFSET:%d", statisticsName, Calendar.getInstance().get(Calendar.ZONE_OFFSET)));
    }

    @Override
    protected void onRemove() {
        logger.info("remove {}", statisticsName);
    }

    public void onEvent(SignalingEvent event) {
        //首先发送TimeUpdateEvent
        sendTimeUpdateEvent(event.getSignalingTime());
        synchronized (status) {
            boolean matchesBefore = isMatches(status.getStayTime());
            status.addEvent(event);
            boolean matchesNow = isMatches(status.getStayTime());//如果添加了Event后，是否符合条件发生变更，则发出事件
            if (matchesBefore ^ matchesNow) {
                send(status.getImsi(), getNextAge(status.getEventTImeInWindow(), end), matchesNow);
            }
        }
    }

    private boolean isMatches(long stayTime) {
        return stayTime > this.stayTime;
    }

    public void onEvent(TimeUpdateEvent event) {
        synchronized (status) {
            long latestAge = getNextAge(status.getEventTImeInWindow(), end);
            if ((!isMatches(status.getStayTime()))  //如果用户当前不满足条件
                    && status.isInsideInWindow()  //并且用户还未离开
                    && isMatches(status.getStayTime() + event.getSignalingTime() - status.getEventTImeInWindow())) { //并且到当前的停留时间满足条件
                send(status.getImsi(), latestAge, true);
            }
            long newAge = getNextAge(event.getSignalingTime(), end);
            if (newAge > latestAge) { //如果统计周期变更
                status.reset(event.getSignalingTime());
                sendAgeUpdateEvent(newAge, event.getSignalingTime());
            }
        }
    }

    private void sendAgeUpdateEvent(long age, long time) {
        AgeUpdateEvent event = new AgeUpdateEvent();
        event.setAge(age);
        event.setEventTime(time);
        event.setStatisticsName(statisticsName);
        emit(event, ageUpdateEventStreams);
    }

    private void send(String imsi, long age, boolean matches) {
        StayHoursEvent event = new StayHoursEvent();
        event.setEndAge(age);
        event.setMatches(matches);
        event.setStatisticsName(statisticsName);
        emit(event, streams);
    }

    private void sendTimeUpdateEvent(long signalingTime) {
        TimeUpdateEvent event = new TimeUpdateEvent();
        event.setSignalingTime(signalingTime);
        emit(event, timeUpdateEventStreams);
    }


    @Override
    public <T extends Event> void emit(T event, Stream<T>[] streamArray) {
        super.emit(event, streamArray);
    }

    public void setAgeUpdateEventStreams(Stream<AgeUpdateEvent>... ageUpdateEventStreams) {
        this.ageUpdateEventStreams = ageUpdateEventStreams;
    }

    public void setTimeUpdateEventStreams(Stream<TimeUpdateEvent>... timeUpdateEventStreams) {
        this.timeUpdateEventStreams = timeUpdateEventStreams;
    }

    public void setStreams(Stream<StayHoursEvent>... streams) {
        this.streams = streams;
    }

    private static class Status {
        String imsi = "";
        private long stayTimeOutWindow;
        private long stayTimeInWindow;
        private final long windowSize = 5 * 60 * 1000;
        private Window window = new Window();
        private boolean insideOutWindow;
        boolean insideInWindow = false;
        private long eventTimeOutWindow;
        private long eventTImeInWindow;

        public void addEvent(SignalingEvent event) {
            imsi = event.getImsi();
            long eventTime = event.getSignalingTime();
            if (eventTime >= eventTImeInWindow - windowSize) {
                Slot slot = window.add(event);
                remove(slot);
                SignalingEvent[] eventArray = (SignalingEvent[]) window.firstSlot.toArray();
                if (event.equals(eventArray.length - 1)) {
                    //如果event就是最新的
                    boolean isInsideNow = isInside(event);
                    stayTimeInWindow += calc(insideInWindow, eventTImeInWindow, event);
                    insideInWindow = isInsideNow;
                    eventTImeInWindow = event.getSignalingTime();
                } else {
                    //如果不是最新的，则从新计算所有event
                    stayTimeInWindow = calc(insideOutWindow, eventTimeOutWindow, window.secondSlot.toArray(), window.firstSlot.toArray());
                    insideInWindow = isInside(eventArray[eventArray.length - 1]);
                    eventTImeInWindow = eventArray[eventArray.length - 1].getSignalingTime();
                }
            }
        }

        private long calc(boolean lastInSide, long lastEventTime, SignalingEvent[]... signalingEvents) {
            long stayTime = 0;
            for (SignalingEvent[] events : signalingEvents) {
                for (SignalingEvent event : events) {
                    stayTime += calc(lastInSide, lastEventTime, event);
                    lastInSide = isInside(event);
                    lastEventTime = event.getSignalingTime();
                }
            }
            return stayTime;
        }

        private long calc(boolean lastInside, long lastEventTime, SignalingEvent event) {
            //只有上次在景区，停留时间才累加，否则（一直不在景区，新进入景区），停留时间都不变
            return lastInside ? (event.getSignalingTime() - lastEventTime) : 0;
        }

        private void remove(Slot slot) {
            if(slot!=null){
                SignalingEvent[] events = slot.toArray();
                long slotStayTime = calc(insideOutWindow, eventTimeOutWindow, events);
                stayTimeOutWindow += slotStayTime;
                stayTimeInWindow -= slotStayTime;
                SignalingEvent lastOutWindowEvent = events[events.length - 1];
                insideOutWindow = isInside(lastOutWindowEvent);
                eventTimeOutWindow = lastOutWindowEvent.getSignalingTime();
            }
        }

        private boolean isInside(SignalingEvent event) {
            //TODO: 需要根据知识库获取
            return ("tourist".equals(event.getCell()));
        }

        public boolean isInsideInWindow() {
            return insideInWindow;
        }


        public long getEventTImeInWindow() {
            return eventTImeInWindow;
        }

        public long getStayTime() {
            return stayTimeOutWindow + stayTimeInWindow;
        }

        public String getImsi() {
            return imsi;
        }

        public void reset(long eventTime) {
            //imsi = "";  //imsi不会变，不需要重新初始化
            stayTimeOutWindow = 0; //新周期，窗口外的停留时间为0
            stayTimeInWindow = 0;  //新周期，窗口内的停留时间为0
            SignalingEvent lastEvent = window.firstSlot.last();
            insideOutWindow = isInside(lastEvent);
            insideInWindow = insideOutWindow;
            eventTimeOutWindow = lastEvent.getSignalingTime();
            eventTImeInWindow = eventTimeOutWindow;
            window = new Window();
        }
    }

    /**
     * 每个窗口有三个Slot。
     * firstSlot: 保存最新的event，有可能5分钟内的全在这里，也可能只有最近1分钟的
     * secondSlot: 保存上次的firstSlot,最近十分钟和最近5分钟以外的数据都在这里,可能更新
     */
    private static class Window {
        private final long windowSize = 5 * 60 * 1000;
        private Slot firstSlot;
        private Slot secondSlot;

        Slot add(SignalingEvent event) {
            init(event);
            Slot removed = null;
            if (firstSlot.endTime <= event.getSignalingTime()) {
                //切换
                synchronized (this) {
                    removed = secondSlot;
                    secondSlot = firstSlot;
                    firstSlot = new Slot(event.getSignalingTime(), event.getSignalingTime() + windowSize);
                }
            }
            firstSlot.add(event);
            return removed;
        }

        private void init(SignalingEvent event) {
            if (firstSlot == null) {
                synchronized (this) {
                    if (firstSlot == null) {
                        firstSlot = new Slot(event.getSignalingTime(), event.getSignalingTime() + windowSize);
                        secondSlot = new Slot(event.getSignalingTime() - windowSize, event.getSignalingTime());
                    }
                }
            }
        }
    }

    /**
     * 保存[startTime,endTime)的数据
     */
    private static class Slot {
        private final long startTime;
        private final long endTime;

        private ConcurrentSkipListSet<SignalingEvent> currentEvents = new ConcurrentSkipListSet<SignalingEvent>(new Comparator<SignalingEvent>() {
            @Override
            public int compare(SignalingEvent o1, SignalingEvent o2) {
                return Long.compare(o1.getSignalingTime(), o2.getSignalingTime()); //从小到大排序
            }
        });

        private Slot(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public void add(SignalingEvent event) {
            currentEvents.add(event);
        }

        public SignalingEvent[] toArray() {
            return (SignalingEvent[]) currentEvents.toArray();
        }

        public SignalingEvent last() {
            return currentEvents.last(); //返回时间最大的那个
        }
    }


}
