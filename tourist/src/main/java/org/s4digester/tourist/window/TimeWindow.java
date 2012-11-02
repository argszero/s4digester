package org.s4digester.tourist.window;

import org.apache.s4.base.Event;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO:考虑使用现成的无锁队列，比如：https://code.google.com/p/disruptor/
 * 引入slot的概念，因为可以无锁的删除不需要的slot
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-2
 * Time: 上午11:08
 * To change this template use File | Settings | File Templates.
 */
public class TimeWindow<T> {
    /**
     * 窗口大小，单位为毫秒
     */
    private final long windowSize;
    /**
     * 插槽大小，单位为毫秒
     */
    private final long slotSize;

    /**
     * 当前最新的插槽的起始时间
     */
    private long currentSlotTime = -1;

    /**
     * 当前最新的插槽最新数据的时间
     */
    private long currentSlotDataTime = -1;

    private final Slot<T>[] slots;
    private int currentSlot = 0;

    public TimeWindow(long windowSize, long slotSize) {
        this.windowSize = windowSize;
        this.slotSize = slotSize;
        slots = new Slot[(int) (windowSize / slotSize + windowSize % slotSize == 0 ? 0 : 1)];
        slots[0] = new Slot<T>();
    }

    public void add(long time, T event) {
        Slot<T> slot = getSlot(time);
        if (slot != null) {
            slot.add(event);
        }
    }

    private Slot<T> getSlot(long time) {
        if (currentSlotTime == -1) {
            currentSlotTime = time;
            currentSlotDataTime = time;
            return new Slot<T>();
        } else if (time < currentSlotDataTime - windowSize) {
            //小于窗口时间，即窗口之前的数据，丢弃
            return null;
        } else if (time > (currentSlotTime + slotSize)) {
            //如果是之后的数据，则新建一个slot
            //对于新建slot的情况应该比较少，可以使用锁
            synchronized (this) {
                Slot<T> slot;
                while (time > (currentSlotTime + slotSize)) {
                    currentSlotTime += slotSize;
                    ++currentSlot;
                    currentSlot = currentSlot % slots.length;
                    slots[currentSlot] = new Slot<T>();
                }
                return slots[currentSlot];
            }
        } else {
            //大多事情况
            //如果是窗口内的某个slot，不一定是最新的slot
            //比如，对于【(9,12]，(13,16]，(17,20]】， 当前时间为10，则应该选slot[0], 如果为14，则应该选slot[1]
            return slots[((int) (currentSlot + (time - currentSlotTime) / slotSize))];
        }
    }

    private static class Slot<T> {

        public void add(T event) {
        }
    }
}
