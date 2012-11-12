package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.Night5In10Event;
import org.s4digester.tourist.event.NightAgeUpdateEvent;
import org.s4digester.tourist.event.StayScenicDuringNightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 晚上10天内满足条件大于5天的用户
 */
public class Night5In10PE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    DaysCache[] daysCaches = new DaysCache[10];
    private Stream<Night5In10Event>[] streams;
    private Stream<NightAgeUpdateEvent>[] ageUpdateStream;

    @Override
    protected void onCreate() {
    }

    @Override
    protected void onRemove() {
    }

    public void onEvent(NightAgeUpdateEvent event) {
        long age = event.getAge();
        synchronized (daysCaches) {
            DaysCache daysCache = daysCaches[daysCaches.length - 1];
            while (event.getAge() > daysCache.getAge()) {
                Set<String> removedImsiSet = daysCaches[0].getImsiSet();
                for (int i = 0; i < daysCaches.length - 1; i++) {
                    daysCaches[i] = daysCaches[i + 1];
                }
                daysCache = new DaysCache();
                long newAge = daysCaches[daysCaches.length - 2].getAge() + 1;
                daysCache.setAge(newAge);
                daysCaches[daysCaches.length - 1] = daysCache;
                for (String imsi : removedImsiSet) { //将去掉的那一天里包含的imsi，重新计算
                    checkAndEmit(imsi);
                }
            }
        }
    }

    public void onEvent(StayScenicDuringNightEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("receive StayScenicDuringNightEvent:{}", new Gson().toJson(event));
        }
        DaysCache daysCache = daysCaches[daysCaches.length - 1];//大多数情况下，即最后一天
        if (daysCache == null) { //第一次访问，做初始化
            synchronized (daysCaches) {
                daysCache = daysCaches[daysCaches.length - 1];
                if (daysCache == null) {
                    for (int i = 0; i < daysCaches.length; i++) {
                        daysCache = new DaysCache();
                        daysCache.setAge(event.getAge() + daysCaches.length - 1 - i);
                        daysCaches[i] = daysCache;
                    }
                }
            }
        }
        if (event.getAge() == daysCache.getAge()) {
            if (daysCache.add(event.getImsi())) {//如果新增，则重新发出所有的复合条件的imsi
                checkAndEmit(event.getImsi());
                return;
            } else { //如果已经存在，则忽略}
            }
        } else if (event.getAge() > daysCache.getAge()) {//如果不是最新的一天的数据，最大可能是新的一天的数据
            synchronized (daysCaches) {
                while (event.getAge() > daysCache.getAge()) {
                    Set<String> removedImsiSet = daysCaches[0].getImsiSet();
                    for (int i = 0; i < daysCaches.length - 1; i++) {
                        daysCaches[i] = daysCaches[i + 1];
                    }
                    daysCache = new DaysCache();
                    long newAge = daysCaches[daysCaches.length - 2].getAge() + 1;
                    daysCache.setAge(newAge);
                    daysCaches[daysCaches.length - 1] = daysCache;
                    emitNewAge(newAge);
                    for (String imsi : removedImsiSet) { //将去掉的那一天里包含的imsi，重新计算
                        checkAndEmit(imsi);
                    }
                }
                if (daysCache.add(event.getImsi())) {//如果新增，则重新发出所有的复合条件的imsi
                    checkAndEmit(event.getImsi());
                    return;
                } else { //如果已经存在，则忽略
                }
            }
        } else { //前面某一天的数据，这种情况几乎不会出现，暂时忽略

        }
    }

    private void emitNewAge(long newAge) {
        NightAgeUpdateEvent nightAgeUpdateEvent = new NightAgeUpdateEvent();
        nightAgeUpdateEvent.setAge(newAge);
        emit(nightAgeUpdateEvent, ageUpdateStream);
    }

    private void checkAndEmit(String imsi) {
        int matchDays = 0; //是否满足最近十天有多余5天符合条件
        synchronized (daysCaches) {
            for (DaysCache daysCache : daysCaches) {
                if (daysCache.getImsiSet().contains(imsi)) {
                    matchDays++;
                }
            }
            Night5In10Event event = new Night5In10Event();
            event.setImsi(imsi);
            event.setToAge(daysCaches[daysCaches.length - 1].age);
            event.setMatches(matchDays > 5);
            emit(event, streams);
        }
    }

    public void setStreams(Stream<Night5In10Event>... streams) {
        this.streams = streams;
    }

    public void setAgeUpdateStreams(Stream<NightAgeUpdateEvent>... ageUpdateStream) {
        this.ageUpdateStream = ageUpdateStream;
    }

    private static class DaysCache {
        private Set<String> imsiSet = new ConcurrentSkipListSet<String>();
        private long age;

        public long getAge() {
            return age;
        }

        public boolean add(String imsi) {
            return imsiSet.add(imsi);
        }

        public void setAge(long age) {
            this.age = age;
        }

        public Set<String> getImsiSet() {
            return imsiSet;
        }
    }
}
