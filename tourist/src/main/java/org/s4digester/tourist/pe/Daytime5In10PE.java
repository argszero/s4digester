package org.s4digester.tourist.pe;

import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.Daytime5In10Event;
import org.s4digester.tourist.event.StayScenicDuringDaytimeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 白天10天内满足条件大于5天的用户
 */
public class Daytime5In10PE extends ProcessingElement {
    DaysCache[] daysCaches = new DaysCache[10];
    private Stream<Daytime5In10Event>[] streams;

    @Override
    protected void onCreate() {
    }

    @Override
    protected void onRemove() {
    }

    public void onEvent(StayScenicDuringDaytimeEvent event) {
        DaysCache daysCache = daysCaches[daysCaches.length - 1];//大多数情况下，即最后一天
        if (event.getAge() == daysCache.getAge()) {
            if (daysCache.add(event.getImsi())) {//如果新增，则重新发出所有的复合条件的imsi
                emitAll();
                return;
            } else { //如果已经存在，则忽略}
            }
        } else if (event.getAge() > daysCache.getAge()) {//如果不是最新的一天的数据，最大可能是新的一天的数据
            synchronized (daysCaches) {
                while (event.getAge() > daysCache.getAge()) {
                    for (int i = 0; i < daysCaches.length - 1; i++) {
                        daysCaches[i] = daysCaches[i + 1];
                    }
                    daysCache = new DaysCache();
                    daysCache.setAge(daysCaches[daysCaches.length - 2].getAge() + 1);
                    daysCaches[daysCaches.length - 1] = daysCache;
                }
                if (daysCache.add(event.getImsi())) {//如果新增，则重新发出所有的复合条件的imsi
                    emitAll();
                    return;
                } else { //如果已经存在，则忽略}
                }
            }
        } else { //前面某一天的数据，这种情况几乎不会出现，暂时忽略

        }
    }

    private void emitAll() {
        List<String> imsiList = new ArrayList<String>();
        for (DaysCache daysCache : daysCaches) {
            for (String imsi : daysCache.getImsiSet()) {
                imsiList.add(imsi);
            }
        }
        Daytime5In10Event event = new Daytime5In10Event();
        event.setImsiList(imsiList);
        event.setLastAge(daysCaches[daysCaches.length-1].getAge());
        emit(event, streams);
    }

    public void setStreams(Stream<Daytime5In10Event>... streams) {
        this.streams = streams;
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
