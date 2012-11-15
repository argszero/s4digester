package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InstantiateFactory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.s4digester.tourist.event.StayDaysEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 根据imsi分布
 * 输出同时满足：
 * 1. 白天...
 * 2. 晚上...
 * 3. 在网时长...
 */
@ThreadSafe
public class JoinAndPrintPE2 extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Join join = new Join();
    private String[] statisticsNames;//白天和晚上

    public JoinAndPrintPE2(App app, String... statisticsNames) {
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
            logger.trace("receive AgedImsiListEvent:{}", new Gson().toJson(event));
        }
        //对于同一个age，可能由多个PE依次发过来
        String imsi = event.getImsi();
        boolean matches = event.isMatches();
        synchronized (join) {
            boolean isUpdated = false;
            if (matches) {
                isUpdated = join.imsiSetMap.get(event.getStatisticsName()).add(imsi);
            } else {
                isUpdated = join.imsiSetMap.get(event.getStatisticsName()).remove(imsi);
            }

            if (isUpdated) {
                Collection<String> joins = join.imsiSetMap.get(statisticsNames[0]);
                for (int i = 1; i < statisticsNames.length; i++) {
                    joins =   CollectionUtils.intersection(joins, join.imsiSetMap.get(statisticsNames[i]));
                }
                StringBuffer sb = new StringBuffer();
                sb.append("Latest Tourist List:");
                sb.append("\n");
                for (String joinImsi : joins) {
                    if (isLangOnline(joinImsi)) {
                        sb.append(joinImsi+"\n");
                    }
                }
                logger.info(sb.toString());
            }
        }
    }

    private boolean isLangOnline(String imsi) {
        //TODO:需要重知识库获取，是否在网时长超过三个月
        return true;
    }

    private static class Join {
//        private Set<String> daytimeSet = new HashSet<String>();
//        private Set<String> nightSet = new HashSet<String>();
        private Map<String,Set<String>> imsiSetMap = LazyMap.decorate(new HashMap<String,Set<String>>(), InstantiateFactory.getInstance(HashSet.class,new Class[0], new Object[0]));
        private long age = -1;
    }
}
