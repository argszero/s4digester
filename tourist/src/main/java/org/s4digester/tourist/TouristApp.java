package org.s4digester.tourist;

import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.*;
import org.s4digester.tourist.pe.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * 用于检测景区游客的app
 * 选取最近10天内满足下面条件的用户
 * 1：每天8:00-18:00在景区停留时长超过3小时天数小于5天
 * 2：每天18:00到次日8:00在景区停留超过5小时小于5天
 * 3: 在网时长超过3个月
 */
public class TouristApp extends App {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void onStart() {
        logger.info("Start TouristApp");
    }

    @Override
    protected void onInit() {
        logger.info("Begin init TouristApp");
        //1. 由SignalingAdapter读取信令文件，生成Stream[Signaling]
        //2. PE[StayScenicDuringDaytimePE]，PE[StayScenicDuringNightPE]接收所有信令事件
        //   PE[StayScenicDuringDaytimePE]输出满足8:00-18:00在景区停留时长超过3小时的用户及日期
        //      即：Stream[Event[StayScenicDuringDaytimeEvent]]
        //   PE[StayScenicDuringNightPE]输出满足18:00到次日8:00在景区停留时长超过5小时的用户及日期
        //      即：Stream[Event[StayScenicDuringNightEvent]]
        //   PE根据用户imsi分发，保证同一个imsi会由同一个PE接收

        StayScenicDuringDaytimePE stayScenicDuringDaytimePE = new StayScenicDuringDaytimePE();
        StayScenicDuringNightPE stayScenicDuringNightPE = new StayScenicDuringNightPE();
        createInputStream("Signaling", new KeyFinder<SignalingEvent>() {
            @Override
            public List<String> get(SignalingEvent event) {
                return Arrays.asList(event.getImsi());
            }
        }, stayScenicDuringDaytimePE, stayScenicDuringNightPE);

        //3. PE[Daytime5In10PE]接收白天在景区停留超过3小时的用户事件
        //   输出所有10天内白天满足该条件的天数小于5天的用户
        Daytime5In10PE daytime5In10PE = new Daytime5In10PE();
        Stream<StayScenicDuringDaytimeEvent> stayScenicDuringDaytime = createInputStream("StayScenicDuringDaytime", new KeyFinder<StayScenicDuringDaytimeEvent>() {
            @Override
            public List<String> get(StayScenicDuringDaytimeEvent event) {
                return Arrays.asList(event.getImsi());
            }
        }, daytime5In10PE);
        stayScenicDuringDaytimePE.setStreams(stayScenicDuringDaytime);

        //4. PE[Daytime5In10PE]接收白天在景区停留超过3小时的用户事件
        //   输出所有10天内白天满足该条件的天数小于5天的用户
        Night5In10PE night5In10PE = new Night5In10PE();
        Stream<StayScenicDuringNightEvent> stayScenicDuringNight = createInputStream("StayScenicDuringNight", new KeyFinder<StayScenicDuringNightEvent>() {
            @Override
            public List<String> get(StayScenicDuringNightEvent event) {
                return Arrays.asList(event.getImsi());
            }
        }, night5In10PE);
        stayScenicDuringNightPE.setStream(stayScenicDuringNight);

        //5. PE[JoinAndPrintPE]同时接收stayScenicDuringDaytime和stayScenicDuringNight。
        //   取交集，并根据知识库的信息过滤掉在网时长不超过3个月的用户，然后输出
        JoinAndPrintPE joinAndPrintPE = new JoinAndPrintPE();
        createInputStream("Daytime5In10", new KeyFinder<Daytime5In10Event>() {
            @Override
            public List<String> get(Daytime5In10Event event) {
                return Arrays.asList(event.getImsi());
            }
        }, joinAndPrintPE);
        createInputStream("Night5In10", new KeyFinder<Night5In10Event>() {
            @Override
            public List<String> get(Night5In10Event event) {
                return Arrays.asList(event.getImsi());
            }
        }, joinAndPrintPE);
        logger.info("Finish init TouristApp");
    }

    @Override
    protected void onClose() {
        logger.info("Close TouristApp");
    }
}
