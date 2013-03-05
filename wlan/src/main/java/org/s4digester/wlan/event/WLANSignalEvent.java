package org.s4digester.wlan.event;

import org.apache.s4.base.Event;

/**
 * WLAN信令事件
 * @author yangzq2
 *
 */
public class WLANSignalEvent extends Event {
	private String imsi; // 用户的IMSI号码
	private String eventType; // 事件类型
    private long signalingTime; // 事件发生的时间戳
    private String cause; // 事件原因值编码
    private String lac; // 位置区编号
    private String cell; // 小区编号
    private String calling; // 发送方号码
    private String called; // 接收方号码
    private String apn; // 访问点APN
    private String sgsnIp; // SGSN的IP地址，相当于信令消息中的源IP地址
    private String res2; // 扩展保留
    
	public String getImsi() {
		return imsi;
	}
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public long getSignalingTime() {
		return signalingTime;
	}
	public void setSignalingTime(long signalingTime) {
		this.signalingTime = signalingTime;
	}
	public String getCause() {
		return cause;
	}
	public void setCause(String cause) {
		this.cause = cause;
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
	public String getCalling() {
		return calling;
	}
	public void setCalling(String calling) {
		this.calling = calling;
	}
	public String getCalled() {
		return called;
	}
	public void setCalled(String called) {
		this.called = called;
	}
	public String getApn() {
		return apn;
	}
	public void setApn(String apn) {
		this.apn = apn;
	}
	public String getSgsnIp() {
		return sgsnIp;
	}
	public void setSgsnIp(String sgsnIp) {
		this.sgsnIp = sgsnIp;
	}
	public String getRes2() {
		return res2;
	}
	public void setRes2(String res2) {
		this.res2 = res2;
	}
    
}
