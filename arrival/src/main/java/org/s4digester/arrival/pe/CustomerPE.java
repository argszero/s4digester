package org.s4digester.arrival.pe;

import org.apache.s4.core.ProcessingElement;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查是否为来港客户的PE
 * @author yangzq2
 *
 */
@ThreadSafe
public class CustomerPE extends ProcessingElement {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected void onCreate() {
		
	}

	@Override
	protected void onRemove() {
		
	}

}
