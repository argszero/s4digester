package org.s4digester.arrival.event;

import org.apache.s4.base.Event;

/**
 * 来港信令事件
 */
public class ArrivalSignalEvent extends Event {
	private String imsi;
	private String eventType;
    private long signalingTime;
    private String lac;
    private String cell;
    
    public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
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
	public String getLac() {
		return lac;
	}
	public void setLac(String lac) {
		this.lac = lac;
	}
	public String getCell() {
		return cell;
	}
	public void setCell(String cell) {
		this.cell = cell;
	}
	
}
