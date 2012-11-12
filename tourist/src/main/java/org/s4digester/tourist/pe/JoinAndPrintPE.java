package org.s4digester.tourist.pe;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.s4.core.ProcessingElement;
import org.s4digester.tourist.event.AgedImsiListEvent;
import org.s4digester.tourist.event.Daytime5In10Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 根据imsi分布
 * 输出同时满足：
 * 1. 白天...
 * 2. 晚上...
 * 3. 在网时长...
 */
public class JoinAndPrintPE extends ProcessingElement {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Join join = new Join();

    @Override
    protected void onCreate() {
    }

    @Override
    protected void onRemove() {
    }

    public void onEvent(AgedImsiListEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("receive AgedImsiListEvent:{}", new Gson().toJson(event));
        }
        //对于同一个age，可能由多个PE依次发过来
        String imsi = event.getImsi();
        boolean matches = event.isMatches();
        synchronized (join) {
            boolean isUpdated = false;
            if (event instanceof Daytime5In10Event) {
                if (matches) {
                    isUpdated = join.daytimeSet.add(imsi);
                } else {
                    isUpdated = join.daytimeSet.remove(imsi);
                }
            } else {
                if (matches) {
                    isUpdated = join.nightSet.add(imsi);
                } else {
                    isUpdated = join.nightSet.remove(imsi);
                }
            }
            if (isUpdated) {
                Collection<String> joins = CollectionUtils.intersection(join.daytimeSet, join.nightSet);
                StringBuffer sb = new StringBuffer();
                sb.append("Latest Tourist List:");
                sb.append("\n");
                for (String joinImsi : joins) {
                    if (isLangOnline(joinImsi)) {
                        sb.append("joinImsi\n");
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
        private Set<String> daytimeSet = new HashSet<String>();
        private Set<String> nightSet = new HashSet<String>();
        private long age = -1;
    }
}
