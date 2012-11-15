package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.AgeChangeEvent;
import org.s4digester.tourist.event.StayDaysEvent;
import org.s4digester.tourist.event.StayHoursEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 白天10天内满足条件大于5天的用户,每次只输出新增的用户，可以重复发送
 * <p/>
 * 使用一个数组保存最近10天，每天复合条件的用户。
 * 每当接收到event时，如果不是新的一天，则不会有“原先符合条件的，现在不符合条件了”，如果是新的一天，则可能有“原先符合条件，现在不符合条件了”
 * 因为是分布式的PE，因此需要通过Event来保持各个PE的最新Event时间同步。
 * <p/>
 * TODO: 最近十天的规则
 * 什么是最近十天。假设当前为11号早上1点
 * 则白天为：1,2,3,4,5,6,7,8,9,10号的白天
 * 晚上为：1,2,3,4,5,6,7,8,9,10的晚上
 * 如果当前为11号早上9点
 * 则白天为：2,3,4,5,6,7,8,9,10,11号的白天
 * 晚上为：1,2,3,4,5,6,7,8,9,10的晚上
 * 如果当前为11号早上19点
 * 则白天为：2,3,4,5,6,7,8,9,10,11号的白天
 * 晚上为：2,3,4,5,6,7,8,9,10,11的晚上
 */
public class StayDaysPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    DaysCache[] daysCaches = new DaysCache[10];
    private Stream<StayDaysEvent>[] streams;
//    private Stream<AgeChangeEvent>[] ageUpdateStream;
    private final String name;

    public StayDaysPE(App app, String name) {
        super(app);
        this.name = name;
    }

    @Override
    protected void onCreate() {
    }

    @Override
    protected void onRemove() {
    }

    public void onEvent(AgeChangeEvent event) {
        if (name.equals(event.getStatisticsName())) {
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

    }

    public void onEvent(StayHoursEvent event) {
        if (name.equals(event.getStatisticsName())) {
            if (logger.isTraceEnabled()) {
                logger.trace("receive StayHoursEvent:{}", new Gson().toJson(event));
            }
            DaysCache daysCache = daysCaches[daysCaches.length - 1];//大多数情况下，即最后一天
            if (daysCache == null) { //第一次访问，做初始化
                synchronized (daysCaches) {
                    daysCache = daysCaches[daysCaches.length - 1];
                    if (daysCache == null) {
                        for (int i = 0; i < daysCaches.length; i++) {
                            daysCache = new DaysCache();
                            daysCache.setAge(event.getEndAge() + daysCaches.length - 1 - i);
                            daysCaches[i] = daysCache;
                        }
                    }
                }
            }
            if (event.getEndAge() == daysCache.getAge()) {
                if (daysCache.add(event.getImsi())) {//如果新增，则重新发出所有的复合条件的imsi
                    checkAndEmit(event.getImsi());
                    return;
                } else { //如果已经存在，则忽略}
                }
            } else if (event.getEndAge() > daysCache.getAge()) {//如果不是最新的一天的数据，最大可能是新的一天的数据
                synchronized (daysCaches) {
                    while (event.getEndAge() > daysCache.getAge()) {
                        Set<String> removedImsiSet = daysCaches[0].getImsiSet();
                        for (int i = 0; i < daysCaches.length - 1; i++) {
                            daysCaches[i] = daysCaches[i + 1];
                        }
                        daysCache = new DaysCache();
                        long newAge = daysCaches[daysCaches.length - 2].getAge() + 1;
                        daysCache.setAge(newAge);
                        daysCaches[daysCaches.length - 1] = daysCache;
//                        emitNewAge(newAge);
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
    }

//    private void emitNewAge(long newAge) {
//        AgeChangeEvent daytimeAgeUpdateEvent = new AgeChangeEvent();
//        daytimeAgeUpdateEvent.setAge(newAge);
//        daytimeAgeUpdateEvent.setStatisticsName(name);
//        emit(daytimeAgeUpdateEvent, ageUpdateStream);
//    }

    private void checkAndEmit(String imsi) {
        int matchDays = 0; //是否满足最近十天有多余5天符合条件
        synchronized (daysCaches) {
            for (DaysCache daysCache : daysCaches) {
                if (daysCache.getImsiSet().contains(imsi)) {
                    matchDays++;
                }
            }
            StayDaysEvent event = new StayDaysEvent();
            event.setImsi(imsi);
            event.setMatches(matchDays > 5);
            event.setStatisticsName(name);
//            event.setToAge(daysCaches[daysCaches.length - 1].age);
            emit(event, streams);
        }
    }

    public void setStreams(Stream<StayDaysEvent>... streams) {
        this.streams = streams;
    }

//    public void setAgeUpdateStreams(Stream<AgeChangeEvent>... ageUpdateStream) {
//        this.ageUpdateStream = ageUpdateStream;
//    }

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
