package org.s4digester.tourist.test;

import static org.s4digester.tourist.util.TimeUtil.getNextAge;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 12-11-16
 * Time: 下午6:33
 * To change this template use File | Settings | File Templates.
 */
public class T {
    public static void main(String[] args) {
        System.out.println(getNextAge(1325419200000L,18*60*60*1000));
    }
}
