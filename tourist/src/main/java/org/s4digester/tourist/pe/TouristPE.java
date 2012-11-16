package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.s4digester.tourist.event.EnterOrLeaveEvent;
import org.s4digester.tourist.event.StayDaysEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * 根据imsi分布
 * 输出同时满足：
 * 1. 白天...
 * 2. 晚上...
 * 3. 在网时长...
 */
@ThreadSafe
public class TouristPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Join join = new Join();
    private String[] statisticsNames;//白天和晚上

    public TouristPE(App app, String... statisticsNames) {
        super(app);
        this.statisticsNames = statisticsNames;
    }

    @Override
    protected void onCreate() {
    }

    @Override
    protected void onRemove() {
    }


    public void onEvent(StayDaysEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("receive StayDaysEvent:{}", new Gson().toJson(event));
        }
        //对于同一个age，可能由多个PE依次发过来
        String imsi = event.getImsi();
        boolean matches = event.isMatches();
        synchronized (join) {
            if (matches) {
                boolean isUpdated = join.workerImsiSetMap.get(event.getStatisticsName()).add(imsi);
                if (isUpdated) {
                    logger.debug("{}:new worker:{}", event.getStatisticsName(), imsi);
                }
            } else {
                boolean isUpdated = join.workerImsiSetMap.get(event.getStatisticsName()).remove(imsi);
                if (isUpdated) {
                    logger.debug("{}:new worker:{}", event.getStatisticsName(), imsi);
                }
            }

        }
    }

    public void onEvent(EnterOrLeaveEvent event) {
        synchronized (join) {
            String imsi = event.getImsi();
            boolean isUpdated = false;
            if (event.isEnter()) { //用户进入，如果是工作人员，则忽略，如果不是，则添加到游客列表
                if (!isWorker(event.getImsi())) {
                    isUpdated = join.tourists.add(event.getImsi());
                }
            } else {//用户离开，从游客列表中删除
                isUpdated = join.tourists.remove(event.getImsi());
            }
            if (isUpdated) {
                logger.info(format("Tourist:%s", StringUtils.join(join.tourists, ",")));
            }
        }
    }

    private boolean isWorker(String imsi) {
        for (Set<String> set : join.workerImsiSetMap.values()) {
            if (set.contains(imsi)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLangOnline(String imsi) {
        //TODO:需要重知识库获取，是否在网时长超过三个月
        return true;
    }

    private static class Join {
        private Set<String> tourists = new HashSet<String>();
        private Map<String, Set<String>> workerImsiSetMap = LazyMap.decorate(new HashMap<String, Set<String>>(), InstantiateFactory.getInstance(HashSet.class, new Class[0], new Object[0]));
    }
}
