package org.s4digester.tourist.test;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-16
 * Time: 下午6:33
 * To change this template use File | Settings | File Templates.
 */
public class T {
    public static void main(String[] args) {
        final String[] a = new String[10];
        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    synchronized (a){
                    }
                }
            }.start();
        }
    }
}
