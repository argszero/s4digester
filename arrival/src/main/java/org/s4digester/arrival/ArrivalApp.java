package org.s4digester.arrival;

import java.util.List;
import java.util.Arrays;

import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;

import org.s4digester.arrival.pe.*;
import org.s4digester.arrival.event.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 筛选机场来港用户App
 * 选取满足以下条件的客户:
 * 1）选择在机场基站区域覆盖的客户
 * 2）月内在机场区域内出现的累计时长<50小时
 * 3）月内在机场区域内出现的累计天数<10天
 * 4）手机用户在机场区域内开机并离开机场区域，且停留时间<120分钟
 * 5）三月平均ARPU>50
 * 
 * 注：实现时条件部分中的“月内”按照最近30天计；
 * 实现思路：
 * 1. 按照条件2）、3）反向筛选和记录机场工作人员
 * 2. 选取出在机场基站区域内有开机行为的非机场工作人员
 * 3. 筛选其中三月平均ARPU>50的客户
 * 4. 为筛选出的客户创建从开机时间起最长时间120分钟的窗口，开机120分钟内离开机场基站区域，可判断为来港客户
 * 
 */
public class ArrivalApp extends App {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	protected void onClose() {
		logger.info("Close ArrivalApp");
	}

	@Override
	protected void onInit() {
		logger.info("Init ArrivalApp begins");
		
		EmployeePE employeePE = new EmployeePE(this, 30, 50, 10);
		Stream<ArrivalSignalEvent> employeeStream = createInputStream("ArrivalSignalEvents", new KeyFinder<ArrivalSignalEvent>(){
			@Override
			public List<String> get(ArrivalSignalEvent e) {
				return Arrays.asList(e.getImsi());
			}
		}, employeePE);
		// create a SignalFilterPE prototype
		SignalFilterPE filterPE = new SignalFilterPE(this);
		filterPE.setStreams(employeeStream);
		// Create a stream that listens to the "ArrivalSignals" stream and passes events to the filterPE instance.
		createInputStream("ArrivalSignals", new KeyFinder<ArrivalSignalEvent>(){
			@Override
			public List<String> get(ArrivalSignalEvent e) {
				return Arrays.asList(e.getImsi());
			}
		}, filterPE);
		
		//SignalFilterPE产生TimeUpdateEvents，给所有EmployeePE
		Stream<TimeUpdateEvent> timeUpdateEventStream = createInputStream("TimeUpdateEvents", employeePE);
		filterPE.setTimeUpdateEventStreams(timeUpdateEventStream);
		
		logger.info("Init ArrivalApp complete");
	}
	
	@Override
	protected void onStart() {
		logger.info("Startup ArrivalApp");
	}
}
