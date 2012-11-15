package org.s4digester.tourist.test;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-15
 * Time: 上午10:31
 * To change this template use File | Settings | File Templates.
 */
public class T {
    public static void main(String[] args) {
        final ArrayBlockingQueue queue = new ArrayBlockingQueue(1000);
        for (int i = 0; i < 100; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 100; i++) {
                            queue.put("123");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }.start();
        }
    }
}
