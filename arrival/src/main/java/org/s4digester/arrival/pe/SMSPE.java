package org.s4digester.arrival.pe;

import static java.lang.String.format;
import java.util.Calendar;

import org.apache.s4.core.ProcessingElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 接收CustomerPE发送的来港客户信息，实时发送短信
 * @author yangzq2
 *
 */
public class SMSPE extends ProcessingElement {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected void onCreate() {
		logger.info("Create SMSPE");
		logger.info(format("ZONE_OFFSET:%d", Calendar.getInstance().get(Calendar.ZONE_OFFSET)));
	}

	@Override
	protected void onRemove() {
		logger.info("Remove SMSPE");
	}

	/**
	 * 
	 */
	public void onEvent(){
		// TODO
		System.out.println("SMS sent to imsi: ");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
