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

package org.springframework.aop.framework.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;

/**
 * Interface allowing extension to the Spring AOP framework to allow
 * handling of new Advisors and Advice types.
 * Implementing objects can create AOP Alliance Interceptors from
 * custom advice types, enabling these advice types to be used
 * in the Spring AOP framework, which uses interception under the covers.
 * <br>There is no need for most Spring users to implement this interface;
 * do so only if you need to introduce more Advisor or Advice types to
 * Spring.
 * @author Rod Johnson
 * @version $Id: AdvisorAdapter.java,v 1.7 2004/03/19 16:54:41 johnsonr Exp $
 */
public interface AdvisorAdapter {
	
	/**
	 * Does this adapter understand this advice object? 
	 * Is it valid to invoke the wrap() method with the
	 * given advice as an argument?
	 * @param advice Advice such as a BeforeAdvice.
	 * @return whether this adapter understands the given advice object 
	 */
	boolean supportsAdvice(Advice advice);
	
	/**
	 * Return an AOP Alliance Interceptor exposing the behaviour of
	 * the given advice to an interception-based AOP framework.
	 * Don't worry about any Pointcut contained in the Advisor;
	 * the AOP framework will take care of checking the pointcut.
	 * @param advisor Advisor. the supportsAdvisor() method must have
	 * returned true on this object
	 * @return an AOP Alliance interceptor for this Advisor. There's
	 * no need to cache instances for efficiency, as the AOP framework
	 * caches advice chains.
	 */
	Interceptor getInterceptor(Advisor advisor);

}
