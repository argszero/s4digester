package org.s4digester.arrival.util;

/**
 * 事件类型常量定义
 * @author yangzq2
 *
 */
public class EventTypeConst {
	public static final String EVENT_CALL = "01"; // 语音主叫
	public static final String EVENT_CALLED = "02"; // 语音被叫
	public static final String EVENT_SMS_RECEIVE = "03"; // 短信接收
	public static final String EVENT_SMS_SEND = "04"; // 短信发送
	public static final String EVENT_TURN_ON = "05"; // 开机
	public static final String EVENT_TURN_OFF = "06"; // 关机
	public static final String EVENT_OTHERS = "99"; // 其他事件
}
