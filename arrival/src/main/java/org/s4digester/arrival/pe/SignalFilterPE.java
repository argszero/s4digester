package org.s4digester.arrival.pe;

import com.google.gson.Gson;
import static java.lang.String.format;
import java.util.Calendar;
import java.util.Date;

import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;

import org.s4digester.arrival.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 筛选待处理客户对象PE
 * 只转发三月平均ARPU>50的客户信令
 */
public class SignalFilterPE extends ProcessingElement {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private Stream<ArrivalSignalEvent>[] streams;
	private Stream<TimeUpdateEvent>[] timeUpdateEventStreams;
	
	public SignalFilterPE(App app){
		super(app);
		timeUpdateEventStreams = new Stream[0];
		logger.debug("SignalFilterPE Constructor.");
	}
	
	@Override
	protected void onCreate() {
		logger.info("Create SignalFilterPE");
		logger.info(format("ZONE_OFFSET:%d", Calendar.getInstance().get(Calendar.ZONE_OFFSET)));
	}

	@Override
	protected void onRemove() {
		logger.info("Remove SignalFilterPE");
	}

	public void onEvent(ArrivalSignalEvent event){
		logger.debug("Receive ArrivalSignalEvent: " + new Gson().toJson(event));
		// 发送时间更新事件
		TimeUpdateEvent timeUpdateEvent =  new TimeUpdateEvent();
		timeUpdateEvent.setImsi(event.getImsi());
		timeUpdateEvent.setSignalingTime(event.getSignalingTime());
		emit(timeUpdateEvent, timeUpdateEventStreams);
		logger.debug("Dispatch TimeUpdateEvent with imsi: " + event.getImsi() +", and time: " + event.getTime() + " as " + new Date(event.getTime()) + ".");
		
		// 检查用户的ARPU
		String imsi = event.getImsi();
		if(checkARPU(imsi) ^ false){
			logger.debug("Dispatch ArrivalSignalEvent with imsi: " + imsi +".");
			emit(event, streams);
		} else {
			logger.error("Drop ArrivalSignalEvent with imsi: " + imsi +": check ARPU fails.");
		}
	}
	
	@Override
    public <T extends Event> void emit(T event, Stream<T>[] streamArray) {
        super.emit(event, streamArray);
    }
	/**
	 * 三月平均ARPU>50返回true，否则返回false，过滤有价值客户
	 * 暂时按照校验客户imsi是否以50开头返回
	 * @param imsi
	 * @return boolean
	 */
	private boolean checkARPU(String imsi){
		//TODO: 从知识库获取，性能优化
		return (imsi.startsWith("50"));
	}

	public void setStreams(Stream<ArrivalSignalEvent>... streams) {
		this.streams = streams;
	}

	public void setTimeUpdateEventStreams(Stream<TimeUpdateEvent>... timeUpdateEventStreams) {
		this.timeUpdateEventStreams = timeUpdateEventStreams;
	}

}
