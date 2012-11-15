package org.s4digester.tourist;

import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;
import org.s4digester.tourist.event.*;
import org.s4digester.tourist.pe.JoinAndPrintPE;
import org.s4digester.tourist.pe.StayDaysPE;
import org.s4digester.tourist.pe.StayHoursPE;
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
 * <p/>
 * //注意：流的名称不能大于20个字符，否则status命令会报错
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
        String daytime = "daytime";
        String night = "night";
        //JoinAndPrintPE <-- StayDaysEvent <-- StayDaysPE <-- StayHoursEvent <--  StayHoursPE  <--SignalingEvent <-- Adaptor
        //JoinAdPrintPE2是做最后的汇总，应该只有一个
        //StayDaysPE有两种，StayHoursPE也有两种，分别处理白天和晚上的统计
        JoinAndPrintPE joinAndPrintPE2 = new JoinAndPrintPE(this, daytime, night);
        joinAndPrintPE2.setSingleton(true);
        Stream<StayDaysEvent> stayDaysEventStream = createInputStream("StayDaysEvents", new KeyFinder<StayDaysEvent>() {
            @Override
            public List<String> get(StayDaysEvent event) {
                return Arrays.asList(event.getImsi());
            }
        }, joinAndPrintPE2);

        StayDaysPE daytimeStayDaysPE = new StayDaysPE(this, daytime);
        daytimeStayDaysPE.setStreams(stayDaysEventStream);
        StayDaysPE nightStayDaysPE = new StayDaysPE(this, night);
        nightStayDaysPE.setStreams(stayDaysEventStream);

        Stream<StayHoursEvent> stayHoursEventStream = createInputStream("StayHoursEvents", new KeyFinder<StayHoursEvent>() {
            @Override
            public List<String> get(StayHoursEvent event) {
                return Arrays.asList(event.getImsi());
            }
        }, daytimeStayDaysPE, nightStayDaysPE);

        StayHoursPE daytimeStayHoursPE = new StayHoursPE(this, 8 * 60 * 60 * 1000, 18 * 60 * 60 * 1000, 3 * 60 * 60 * 1000, daytime);
        daytimeStayHoursPE.setStreams(stayHoursEventStream);
        StayHoursPE nightStayHoursPE = new StayHoursPE(this, 18 * 60 * 60 * 1000, 8 * 60 * 60 * 1000, 5 * 60 * 60 * 1000, night);
        nightStayHoursPE.setStreams(stayHoursEventStream);
        createInputStream("Signaling", new KeyFinder<SignalingEvent>() {
            @Override
            public List<String> get(SignalingEvent event) {
                return Arrays.asList(event.getImsi());
            }
        }, daytimeStayHoursPE, nightStayHoursPE);

        //StayHoursPE产生AgeChangeEvents，给StayDaysPE
        Stream<AgeUpdateEvent> ageChangeEventStream = createInputStream("AgeUpdateEvents", daytimeStayDaysPE, nightStayDaysPE);
        daytimeStayHoursPE.setAgeUpdateEventStreams(ageChangeEventStream);
        nightStayHoursPE.setAgeUpdateEventStreams(ageChangeEventStream);

        //StayHoursPE产生TimeUpdateEvents，给所有StayHoursPE
        Stream<TimeUpdateEvent> timeUpdateEventStream = createInputStream("TimeUpdateEvents", daytimeStayHoursPE, nightStayHoursPE);
        daytimeStayHoursPE.setTimeUpdateEventStreams(timeUpdateEventStream);
        nightStayHoursPE.setTimeUpdateEventStreams(timeUpdateEventStream);

        logger.info("Finish init TouristApp1");
    }

    @Override
    protected void onClose() {
        logger.info("Close TouristApp");
    }
}
