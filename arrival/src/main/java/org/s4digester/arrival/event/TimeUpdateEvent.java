package org.s4digester.arrival.event;

import org.apache.s4.base.Event;
/**
 * 时间变化事件，每次收到信令后向所有PE发送更新时间的事件。
 * 触发对员工PE中保存的机场员工在机场时长记录数组中当前在机场的员工自增时长。
 * @author yangzq2
 *
 */
public class TimeUpdateEvent extends Event {
	private String imsi;
	private long signalingTime;
	private final String target = "all";

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public long getSignalingTime() {
		return signalingTime;
	}

	public void setSignalingTime(long signalingTime) {
		this.signalingTime = signalingTime;
	}

	public String getTarget() {
		return target;
	}
	
}
