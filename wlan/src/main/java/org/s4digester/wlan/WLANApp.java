package org.s4digester.wlan;

import org.apache.s4.core.App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WLAN热点提醒App
 * 选取满足以下条件的客户:
 * 1）最近15分钟，链接WAP后未断开
 * 2）最近15分钟内，WAP请求超过20次
 * 3）手机终端有WIFI功能（知识库获取）
 * 
 * @author yangzq2
 *
 */
public class WLANApp extends App {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected void onClose() {
		logger.info("Close WLANApp");
	}

	@Override
	protected void onInit() {
		logger.info("Init WLANApp begins");
		
		logger.info("Init WLANApp complete");
	}

	@Override
	protected void onStart() {
		logger.info("Start WLANApp");
	}
}
