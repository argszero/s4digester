package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.AgeUpdateEvent;
import org.s4digester.tourist.event.StayDaysEvent;
import org.s4digester.tourist.event.StayHoursEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
    boolean[] recentDays = new boolean[10]; //最近10天是否满足条件，默认全为false
    long latestAge = -1;
    private Stream<StayDaysEvent>[] streams;
    private final String statisticsName;
    private String imsi;

    public StayDaysPE(App app, String name) {
        super(app);
        this.statisticsName = name;
    }

    @Override
    protected void onCreate() {
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
            synchronized (recentDays) {
                long age = event.getAge();
                if (latestAge == -1) {
                    latestAge = age;
                } else if (age > latestAge) {
                    //左移
                    long matchesDaysBefore = getMatchesDays(recentDays);
                    while (age > latestAge) {
                        for (int i = 1; i < recentDays.length; i++) {
                            recentDays[i - 1] = recentDays[i];
                        }
                        latestAge++;
                    }
                    //判断：如果更新之前用户是符合条件，更新之后不符合条件，则发出不符合条件的事件
                    if (isDaysMatches(matchesDaysBefore) && isDaysMatches(getMatchesDays(recentDays))) {
                        send(false);
                    }
                }
            }
        }
    }

    private void send(boolean matches) {
        StayDaysEvent event = new StayDaysEvent();
        event.setStatisticsName(statisticsName);
        event.setImsi(imsi);
        event.setMatches(matches);
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
                logger.trace("{}:receive Signaling:{}", statisticsName, new Gson().toJson(event));
            }
            synchronized (recentDays) {
                imsi = event.getImsi();
                boolean matchesBefore = isDaysMatches(getMatchesDays(recentDays));
                long age = event.getEndAge();
                long index = age + recentDays.length - 1 - latestAge;
                if (index >= 0 && index < recentDays.length) {
                    recentDays[(int) index] = event.isMatches();
                } else {
                    //忽略 ，比如当前最新时间为10号，至少要三个小时后才收到11号符合条件，此时AgeUpdateEvent应该早就到了
                }
                boolean matchesNow = isDaysMatches(getMatchesDays(recentDays));
                logger.debug(String.format("%s %s: days:%d",statisticsName,imsi,getMatchesDays(recentDays)));
                logger.debug(String.format("%d",latestAge));
                logger.debug(String.format("%d",Arrays.toString(recentDays)));
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
