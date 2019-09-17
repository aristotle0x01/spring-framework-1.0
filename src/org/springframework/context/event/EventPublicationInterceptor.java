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

package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ApplicationObjectSupport;

/**
 * Interceptor that knows how to publish {@link org.springframework.context.ApplicationEvent}s to all
 * <code>ApplicationListener</code>s registered with <code>ApplicationContext</code> 
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptor.java,v 1.3 2004/03/18 02:46:10 trisberg Exp $
 */
public class EventPublicationInterceptor extends ApplicationObjectSupport implements MethodInterceptor {

	private Class applicationEventClass;

	/**
	 * Set the application event class to publish.
	 * <p>The event class must have a constructor with a single Object argument
	 * for the event source. The interceptor will pass in the invoked object.
	 */
	public void setApplicationEventClass(Class applicationEventClass) {
		if (applicationEventClass == null || !ApplicationEvent.class.isAssignableFrom(applicationEventClass)) {
			throw new IllegalArgumentException("applicationEventClass needs to implement ApplicationEvent");
		}
		this.applicationEventClass = applicationEventClass;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		Constructor constructor = this.applicationEventClass.getConstructor(new Class[] {Object.class});
		ApplicationEvent applicationEvent = (ApplicationEvent) constructor.newInstance(new Object[] {invocation.getThis()});
		getApplicationContext().publishEvent(applicationEvent);
		return retVal;
	}

}
