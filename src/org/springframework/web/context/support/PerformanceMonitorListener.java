/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ResponseTimeMonitorImpl;

/**
 * Listener that logs the response times of web requests.
 * To be registered in a WebApplicationContext.
 * @author Rod Johnson
 * @since January 21, 2001
 * @see RequestHandledEvent
 */
public class PerformanceMonitorListener implements ApplicationListener {

	protected final Log logger = LogFactory.getLog(getClass());

	protected ResponseTimeMonitorImpl responseTimeMonitor;

	public PerformanceMonitorListener() {
		this.responseTimeMonitor = new ResponseTimeMonitorImpl();
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof RequestHandledEvent) {
			RequestHandledEvent rhe = (RequestHandledEvent) event;
			// Could use one monitor per URL
			this.responseTimeMonitor.recordResponseTime(rhe.getTimeMillis());
			if (logger.isInfoEnabled()) {
				// Stringifying objects is expensive. Don't do it unless it will show.
				logger.info("PerformanceMonitorListener: last=" + rhe.getTimeMillis() + "ms; " +
										this.responseTimeMonitor + "; client was " + rhe.getIpAddress());
			}
		}
	}

}
