package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import net.jcip.annotations.ThreadSafe;
import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static org.s4digester.tourist.util.TimeUtil.getNextAge;

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
    private Stream<EnterOrLeaveEvent>[] enterOrLeaveEventStreams;


    public StayHoursPE(App app, long start, long end, long stayTime, String statisticsName) {
        super(app);
        this.start = start;
        this.end = end;
        this.stayTime = stayTime;
        this.statisticsName = statisticsName;
    }

    @Override
    protected void onCreate() {
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
        checkAndSendAgeUpdateEvent(event);
        synchronized (status) {
            boolean matchesBefore = isMatches(status.getStayTime());
            boolean inSideBefore = status.insideInWindow;
            status.addEvent(start, end, event);
            boolean matchesNow = isMatches(status.getStayTime());//如果添加了Event后，是否符合条件发生变更，则发出事件
            if(event.getImsi().equals("Worker4")&&statisticsName.equals("night")){
                StringBuffer message = new StringBuffer("aaaa").append(new Gson().toJson(event));
                message.append("stayTime:").append(status.getStayTime());
                message.append("matchesBefore:").append(matchesBefore);
                message.append("matchesNow:").append(matchesNow);
                logger.trace(message.toString());
            }
            if (matchesBefore ^ matchesNow) {
                send(status.getImsi(), getNextAge(status.getEventTImeInWindow(), end), matchesNow);
            }
            boolean inSideNow = status.insideInWindow;
            if (inSideBefore ^ inSideNow) {
                sendEnterOrLeaveEvent(status.getImsi(), inSideNow);
            }
        }
    }

    private void checkAndSendAgeUpdateEvent(SignalingEvent event) {
        synchronized (status) {
            long latestAge = getNextAge(status.getEventTImeInWindow(), end);
            long newAge = getNextAge(event.getSignalingTime(), end);
            if (newAge > latestAge) { //如果统计周期变更
                status.reset(event.getSignalingTime());
                sendAgeUpdateEvent(newAge, event.getSignalingTime());
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
        event.setImsi(imsi);
        event.setStatisticsName(statisticsName);
        emit(event, streams);
    }

    private void sendTimeUpdateEvent(long signalingTime) {
        TimeUpdateEvent event = new TimeUpdateEvent();
        event.setSignalingTime(signalingTime);
        emit(event, timeUpdateEventStreams);
    }

    private void sendEnterOrLeaveEvent(String imsi, boolean enter) {
        EnterOrLeaveEvent event = new EnterOrLeaveEvent();
        event.setEnter(enter);
        event.setImsi(imsi);
        emit(event, enterOrLeaveEventStreams);
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

    public void setEnterOrLeaveEventStreams(Stream<EnterOrLeaveEvent>... enterOrLeaveEventStreams) {
        this.enterOrLeaveEventStreams = enterOrLeaveEventStreams;
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

        public void addEvent(long start, long end, SignalingEvent event) {
            imsi = event.getImsi();
            long eventTime = event.getSignalingTime();

            if (eventTime >= eventTImeInWindow - windowSize) {
                Slot slot = window.add(event);
                remove(start, end, slot);
                SignalingEvent[] eventArray = window.firstSlot.toArray();
                if (event.equals(eventArray[eventArray.length - 1])) {
                    //如果event就是最新的
                    boolean isInsideNow = isInside(event);
                    stayTimeInWindow += calc(start, end, insideInWindow, eventTImeInWindow, event);
                    insideInWindow = isInsideNow;
                    eventTImeInWindow = event.getSignalingTime();
                } else {
                    //如果不是最新的，则从新计算所有event
                    stayTimeInWindow = calc(start, end, insideOutWindow, eventTimeOutWindow, window.secondSlot.toArray(), window.firstSlot.toArray());
                    insideInWindow = isInside(eventArray[eventArray.length - 1]);
                    eventTImeInWindow = eventArray[eventArray.length - 1].getSignalingTime();
                }
            }
        }


        private long calc(long start, long end, boolean lastInSide, long lastEventTime, SignalingEvent[]... signalingEvents) {
            long stayTime = 0;
            for (SignalingEvent[] events : signalingEvents) {
                for (SignalingEvent event : events) {
                    stayTime += calc(start, end, lastInSide, lastEventTime, event);
                    lastInSide = isInside(event);
                    lastEventTime = event.getSignalingTime();
                }
            }
            return stayTime;
        }

        private long calc(long start, long end, boolean lastInside, long lastEventTime, SignalingEvent event) {
            //只有上次在景区，停留时间才累加，否则（一直不在景区，新进入景区），停留时间都不变
            if (lastInside) {
                long lastEndAge = getNextAge(lastEventTime, end);
                long startTime = (start < end ? lastEndAge : lastEndAge - 1) * 24 * 60 * 60 * 1000 + start; //统计起点，对于白天，则为当天，对于晚上，为结束的头一天
                long endTime = lastEndAge * 24 * 60 * 60 * 1000 + end; //统计终点
                return max(min(endTime, event.getSignalingTime()), startTime) - min(max(lastEventTime, startTime), endTime);
            } else return 0;
        }

        private void remove(long start, long end, Slot slot) {
            if (slot != null) {
                SignalingEvent[] events = slot.toArray();
                if (events.length > 0) {
                    long slotStayTime = calc(start, end, insideOutWindow, eventTimeOutWindow, events);
                    stayTimeOutWindow += slotStayTime;
                    stayTimeInWindow -= slotStayTime;
                    SignalingEvent lastOutWindowEvent = events[events.length - 1];
                    insideOutWindow = isInside(lastOutWindowEvent);
                    eventTimeOutWindow = lastOutWindowEvent.getSignalingTime();
                }
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
            insideOutWindow = insideInWindow; //新周期，窗口外的状态变成原先窗口内的状态(窗口里的数据移到了窗口外)
            insideInWindow = insideOutWindow;//窗口内没有数据，所以和窗口外窗口状态一致
            eventTimeOutWindow = eventTime;
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
            return currentEvents.toArray(new SignalingEvent[0]);
        }

        public SignalingEvent last() {
            return currentEvents.last(); //返回时间最大的那个
        }
    }


}
