package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import net.jcip.annotations.ThreadSafe;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.AgeUpdateEvent;
import org.s4digester.tourist.event.StayDaysEvent;
import org.s4digester.tourist.event.StayHoursEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.lang.String.format;

/**
 * 判断用户停留天数是否满足条件的PE。
 * 使用一个boolean[]来保存最近10天用户是否满足条件。
 * 接收两种Event：
 * 1. AgeUpdateEvent
 * bitmap左移
 * 2. StayHoursEvent
 * 判断
 */
@ThreadSafe
public class StayDaysPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    boolean[] recentDays;//最近10天是否满足条件，默认全为false
    long latestAge;
    private Stream<StayDaysEvent>[] streams;
    private final String statisticsName;
    private String imsi;

    public StayDaysPE(App app, String name) {
        super(app);
        this.statisticsName = name;
        latestAge = -1;
    }

    @Override
    protected void onCreate() {
        //如果要recentDays在多个PE实例中不共享，需要将他的赋值放到onCreate方法，而不是构造函数
        recentDays = new boolean[10];
    }

    @Override
    protected void onRemove() {
    }

    /**
     * 接收到统计周期变更事件后，更新最近10天的状态。
     *
     * @param event
     */
    public void onEvent(AgeUpdateEvent event) {
        if (statisticsName.equals(event.getStatisticsName())) {
            if (logger.isTraceEnabled()) {
                logger.trace("receive AgeUpdateEvent:{}", new Gson().toJson(event));
            }
            long age = event.getAge();
            updateAge(age);
        }
    }

    private void updateAge(long age) {
        synchronized (this) {
            if (latestAge == -1) {
                latestAge = age;
            } else if (age > latestAge) {
                boolean matchesBefore = isDaysMatches(getMatchesDays(recentDays));
                while (age > latestAge) {
                    for (int i = 1; i < recentDays.length; i++) {
                        recentDays[i - 1] = recentDays[i];
                    }
                    recentDays[recentDays.length - 1] = false;
                    latestAge++;
                }
                boolean matchesNow = isDaysMatches(getMatchesDays(recentDays));
                if (matchesBefore ^ matchesNow) { //当状态变更时，发送信息
                    send(matchesNow);
                }
            }
        }
    }

    private void send(boolean matches) {
        StayDaysEvent event = new StayDaysEvent();
        event.setStatisticsName(statisticsName);
        event.setImsi(imsi);
        event.setMatches(matches);
        if (!matches) {
            if (logger.isTraceEnabled()) {
                logger.trace(format("%s mismatched:%s,latestAge:%s", imsi, Arrays.toString(recentDays), latestAge));
            }
        }
        emit(event, streams);
    }

    private boolean isDaysMatches(long days) {
        return days >= 5;
    }

    private long getMatchesDays(boolean[] recentDays) {
        long matchesDays = 0;
        for (boolean matches : recentDays) {
            matchesDays += matches ? 1 : 0;
        }
        return matchesDays;
    }

    public void onEvent(StayHoursEvent event) {
        if (statisticsName.equals(event.getStatisticsName())) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:receive StayHoursEvent:{}", statisticsName, new Gson().toJson(event));
            }
            synchronized (this) {
                imsi = event.getImsi();
                boolean matchesBefore = isDaysMatches(getMatchesDays(recentDays));
                long age = event.getEndAge();
                updateAge(age);
                long index = age + recentDays.length - 1 - latestAge;
                if (index >= 0 && index < recentDays.length) {
                    recentDays[(int) index] = event.isMatches();
                } else {
                    //忽略 ，比如当前最新时间为10号，至少要三个小时后才收到11号符合条件，此时AgeUpdateEvent应该早就到了
                }
                boolean matchesNow = isDaysMatches(getMatchesDays(recentDays));
                if (matchesBefore ^ matchesNow) { //当状态变更时，发送信息
                    send(matchesNow);
                }
            }


        }
    }

    public void setStreams(Stream<StayDaysEvent>... streams) {
        this.streams = streams;
    }
}
