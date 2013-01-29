package org.s4digester.arrival.pe;

import com.google.gson.Gson;
import static java.lang.String.format;
import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

import net.jcip.annotations.ThreadSafe;

import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.App;
import org.s4digester.arrival.event.*;
import org.s4digester.arrival.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查是否为机场工作人员的PE
 * 符合以下任一判定条件：
 * 1）统计期限（30天）内在机场区域内出现的累计时长>=50小时
 * OR
 * 2）统计期限（30天）内在机场区域内出现的累计天数>=10天
 * 
 */
@ThreadSafe
public class EmployeePE extends ProcessingElement {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private int statisticsDays; // 统计期限，最近天数
	private long workHours; // 统计天数内在机场时长，毫秒数
	private int workDays; // 统计天数内在机场天数
	private long[] hoursAtAirport; // 记录最近30天在机场的每日时长，毫秒数
	private Status status;

	/**
	 * constructor
	 * @param app
	 * @param statisticsDays 统计期限
	 * @param workHours 统计天数内在机场时长限制，小时数
	 * @param workDays 统计天数内在机场天数限制
	 */
	public EmployeePE(App app) {
		super(app);
		logger.debug("EmployeePE Constructor.");
	}

	@Override
	protected void onCreate() {
		logger.info("Create EmployeePE");
		logger.info(format("ZONE_OFFSET:%d", Calendar.getInstance().get(Calendar.ZONE_OFFSET)));
		status = new Status();
		this.hoursAtAirport =  new long[statisticsDays];
		for (long stayTimeinmillis : this.hoursAtAirport){
			stayTimeinmillis = -1L;
		}
	}

	@Override
	protected void onRemove() {
		logger.info("Remove EmployeePE");
	}

	public void onEvent(ArrivalSignalEvent event){
		// TODO
		
		logger.debug("Receive ArrivalSignalEvent with imsi: " + event.getImsi() +".");
	}
	
	/**
	 * 接收到TimeUpdateEvent时，其他用户按条件更新status
	 * @param event
	 */
	public void onEvent(TimeUpdateEvent event){
		// TODO
		logger.debug("Receive TimeUpdateEvent: " + new Gson().toJson(event) +".");
		String imsi = event.getImsi();
		long eventTime = event.getSignalingTime();
		
		// 所有不是本人的事件且接收到的事件时间晚于当前窗口中记录的最后一个事件的时间，则更新时间
		if((!imsi.equals(status.getImsi())) && (eventTime > status.getLastEventTimeInWin())){
			synchronized (status) {
				updateStayTime(status, eventTime);
				// TODO calculate time
				
			}
			
		} else {
			// 是本人或乱序事件，do nothing
		}
	}
	
	private boolean updateStayTime(Status status, long eventTime){
		// TODO
		status.setLastEventTimeInWin(eventTime);
		if (status.getLastEventTimeInWin() / (24*60*60*1000) != eventTime / (24*60*60*1000)){
			
		}
		return true;
	}
	
	
	public void setStatisticsDays(int statisticsDays) {
		this.statisticsDays = statisticsDays;
		logger.debug("EmployeePE, statisticsDays: " + statisticsDays + ".");
	}

	public void setWorkHours(long workHours) {
		this.workHours = workHours * 60 * 60 * 1000; // 小时数转换为毫秒数
		logger.debug("EmployeePE, workHours: " + workHours + ".");
	}

	public void setWorkDays(int workDays) {
		this.workDays = workDays;
		logger.debug("EmployeePE, workDays: " + workDays + ".");
	}


	/**
	 * 记录当前数据和状态
	 */
	private class Status {
		private Logger logger = LoggerFactory.getLogger(getClass());
		
		private String imsi = "";
		private Window window = new Window();
		private final long windowSize = 5 * 60 * 1000; // 窗口大小，限定为5分钟
		private boolean isAtAirport = false; // 截至到当前信令前客户是否在机场
		private long lastEventTimeInWin; // 窗口内最后一个事件的时间
		
		
		public void addEvent(ArrivalSignalEvent event) {
			if (imsi == null || imsi.equals("")){
				imsi=event.getImsi();
			}
			else if (!imsi.equals(event.getImsi())){
				logger.info("Imsi mismatch. Recorded: " + imsi + ", incoming: " + event.getImsi());
				return;
			} else {
				// do nothing
			}
			long eventTime = event.getSignalingTime();
			
			
		}
		
		private long reCalculateTime(){ // 重新计算乱序的时间
			//TODO
			
			return 0L;
		}
		private long calculateTime(boolean isAtAirport, long[] hoursAtAirport, ArrivalSignalEvent[] eventArray, ArrivalSignalEvent[] historyArray, Slot slot){
			//TODO
			
			return 0L;
		}
		
		/**
		 * 触发事件event时客户是否在机场区域
		 * 暂时按校验事件对象中Lac字段是否为airport返回
		 * @param event
		 * @return boolean
		 */
		private boolean checkAirportPosition(ArrivalSignalEvent event){
			//TODO: 需要根据知识库获取
			return ("airport".equals(event.getLac()));
		}

		public String getImsi() {
			return imsi;
		}

		public long getLastEventTimeInWin() {
			return lastEventTimeInWin;
		}

		public void setLastEventTimeInWin(long lastEventTimeInWin) {
			this.lastEventTimeInWin = lastEventTimeInWin;
		}

	}
	
	/**
	 * 窗口类，支持5分钟内的乱序
	 * 保存两个Slot数据结构（以5分钟为一个时间段整体来处理以提高窗口删除效率）：
	 * 1）currentSlot 保存当前时间段内处理中的event数据，保存的事件时间跨度可能小于5分钟，开始到最终事件跨度满5分钟则把数据切换给cacheSlot；
	 * 2）cacheSlot 作为上5分钟接收到的event历史数据缓存，乱序处理可能会用到；
	 */
	private static class Window {
		private final long windowSize = 5 * 60 * 1000; // 窗口大小，限定为5分钟
        private Slot currentSlot;
        private Slot cacheSlot;
        
        /**
         * 向窗口中添加事件，切换Slot后返回被替换掉的历史Slot数据，未切换则返回null
         * @param event
         * @return removed slot
         */
        Slot add(ArrivalSignalEvent event){
        	initWindow(event);
        	Slot removed = null;
        	if (currentSlot.endTime <= event.getSignalingTime()){
        		// 接收到的事件时间晚于当前currentSlot的结束时间则开始新的时间段记录，把当前currentSlot中的历史数据移入cacheSlot
        		synchronized (this) {
					removed = cacheSlot;
					cacheSlot = currentSlot;
					currentSlot = new Slot(event.getSignalingTime(), event.getSignalingTime() + windowSize);
				}
        	}
        	currentSlot.add(event);
        	return removed;
        }
        
        /**
         * 初始化窗口，成员变量初始化
         * @param event
         */
        private void initWindow(ArrivalSignalEvent event){
        	if (currentSlot == null){
        		synchronized (this) {
        			currentSlot = new Slot(event.getSignalingTime(), event.getSignalingTime() + windowSize);
        			cacheSlot =  new Slot(event.getSignalingTime() - windowSize, event.getSignalingTime());
				}
        	}
        }
	}
	
	/**
     * 窗口基类，保存[startTime,endTime)的事件数据
     */
    private static class Slot {
        private final long startTime; // 记录事件开始时间
        private final long endTime; // 记录事件结束时间

        private ConcurrentSkipListSet<ArrivalSignalEvent> currentEvents = new ConcurrentSkipListSet<ArrivalSignalEvent>(new Comparator<ArrivalSignalEvent>() {
            @Override
            public int compare(ArrivalSignalEvent e1, ArrivalSignalEvent e2) {
                return Long.compare(e1.getSignalingTime(), e2.getSignalingTime()); // 按时间从小到大排序
            }
        });

        private Slot(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public void add(ArrivalSignalEvent event) {
            currentEvents.add(event);
        }

        public ArrivalSignalEvent[] toArray() {
            return currentEvents.toArray(new ArrivalSignalEvent[0]);
        }

        public ArrivalSignalEvent last() {
            return currentEvents.last(); // 返回最后时间的事件
        }
    }
}
