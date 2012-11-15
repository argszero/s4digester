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
            boolean matchesBefore = status.isMatches();
            status.addEvent(event);
            boolean matchesNow = status.isMatches();//如果添加了Event后，是否符合条件发生变更，则发出事件
            if (matchesBefore ^ matchesNow) {
                send(status.getImsi(), getNextAge(status.getLatestEventTime(), end), matchesNow);
            }
        }
    }

    public void onEvent(TimeUpdateEvent event) {
        synchronized (status) {
            long latestAge = getNextAge(status.getLatestEventTime(), end);
            if ((!status.isMatches())  //如果用户当前不满足条件
                    && status.isInside()  //并且用户还未离开
                    && status.getStayTimeTo(event.getSignalingTime()) > stayTime) { //并且到当前的停留时间满足条件
                send(status.getImsi(), latestAge, true);
            }
            long newAge = getNextAge(event.getSignalingTime(), end);
            if (newAge > latestAge) { //如果统计周期变更
                status.reset(newAge);
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

    public void onEvent(AgeUpdateEvent event) {

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
        private long stayTimeOutWindow;
        private Window window;

        public boolean isMatches() {
            return false;
        }

        public boolean isInside() {
            return false;
        }

        public long getStayTimeTo(long signalingTime) {
            return 0;
        }

        public String getImsi() {
            return null;
        }

        public long getLatestEventTime() {
            return 0;

        }

        public void reset(long newAge) {

        }

        public void addEvent(SignalingEvent event) {

        }
    }

    private static class Window {

        public boolean isInside() {
            return false;

        }
    }

}
