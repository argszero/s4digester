package org.s4digester.arrival.test;

import java.util.Date;
import java.util.Calendar;

public class Test1 {
	
	long[] testArr = new long[10];

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		String test="";
		System.out.println(test == "");
		System.out.println(test == null);
		System.out.println(test.equals(""));
		
		String test1="abc";
		System.out.println(test1 == "");
		System.out.println(test1.equals(""));
		*/
//		long now=System.currentTimeMillis();
//		System.out.println("毫秒数：" + now);
//		System.out.println("日期类型：" + new Date(now));
		
//		for (long testlong:new Status().testArr1){
//			System.out.println(testlong);
//		}
		/**
        long t1 = 1218211200000L - 8*60*60*1000;
        long t2 = 1218211200000L+ 0*60*60*1000;
        System.out.println("milisecond：" + t1);
        System.out.println("milisecond：" + t2);
        System.out.println("milisecond：" + (t1 / (24 * 60 * 60 * 1000)) * 24 * 60 * 60 * 1000);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(t1);
        System.out.println("Calendar：" + cal1.get(Calendar.DAY_OF_MONTH));
        System.out.println("日期类型：" + new Date(t1));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(t2);
        System.out.println("Calendar：" + cal.get(Calendar.DAY_OF_MONTH));
        System.out.println("日期类型：" + new Date(t2));
        System.out.println("日期类型：" + new Date((t1 / (24 * 60 * 60 * 1000)) * 24 * 60 * 60 * 1000));
        long daysBetweenFirst = (t2 + 8 * 60 * 60 * 1000) / (24 * 60 * 60 * 1000) - (t1 + 8 * 60 * 60 * 1000) / (24 * 60 * 60 * 1000);
        System.out.println("t1：" + (long)( (t1 + 8 * 60 * 60 * 1000) / (24 * 60 * 60 * 1000)));
        System.out.println("t2：" + (long)( (t2 + 8 * 60 * 60 * 1000) / (24 * 60 * 60 * 1000)));
        System.out.println("daysBetweenFirst：" + daysBetweenFirst);
        */
//        System.out.println("t1：" + (long)(14099 * (24 * 60 * 60 )));
//        System.out.println("t2：" + (long)(14100 * (24 * 60 * 60 )));
		System.out.println("true ^ true：" + (true ^ true));
		System.out.println("true ^ false：" + (true ^ false));
	}

	public void test(){
		
	}
	private static class Status {
		long[] testArr1 = new long[10];
	}
}

