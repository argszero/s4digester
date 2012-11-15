package org.s4digester.tourist.window;

import org.s4digester.tourist.event.SignalingEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 时间窗口。
 * 1. 每次添加会返回所有过期和再用的数据，可以根据这个来重新计算
 * 2. 目前这个窗口没有
 */
public class TimeWindow {
    private final long windowSize;

    public TimeWindow(long windowSize) {
        this.windowSize = windowSize;
    }

    private ConcurrentSkipListSet events = new ConcurrentSkipListSet<SignalingEvent>(new Comparator<SignalingEvent>() {
        @Override
        public int compare(SignalingEvent o1, SignalingEvent o2) {
            return Long.compare(o1.getSignalingTime(), o2.getSignalingTime()); //从小到大排序
        }
    });

    public AddResult add(SignalingEvent event) {
        SignalingEvent[] eventArray;
        synchronized (events) {
//这里不需要锁，因为：1. event本身是线程安全的。
// 2. 如果event里添加和返回数组之前，另外一个线程向event里插入一条数据，则计算最新的也没什么问题
// 3。如果event里添加和返回数组之前，另外一个线程删除了一条数据


            events.add(event);
            eventArray = (SignalingEvent[]) events.toArray();
            final long beginTime = event.getSignalingTime() - windowSize;
            int i = Arrays.binarySearch(eventArray, null, new Comparator<SignalingEvent>() {
                @Override
                public int compare(SignalingEvent o1, SignalingEvent o2) {
                    return Long.compare(o1.getSignalingTime(), beginTime);
                }
            });
            i = i > 0 ? i : (-i - 1);
            SignalingEvent[] expired = Arrays.copyOfRange(eventArray, 0, i);
            for (SignalingEvent e : expired) {
                events.remove(e);
            }
            SignalingEvent[] inWindowed = Arrays.copyOfRange(eventArray, i, eventArray.length);
            return new AddResult(expired, inWindowed);
        }
    }

    private static class AddResult {
        SignalingEvent[] expired;
        SignalingEvent[] inWindowed;

        private AddResult(SignalingEvent[] expired, SignalingEvent[] inWindowed) {
            this.expired = expired;
            this.inWindowed = inWindowed;
        }
    }

}
