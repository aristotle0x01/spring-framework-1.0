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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterReturningAdvice;


/**
 * Interceptor to wrap a MethodAfterReturningAdvice. In future we may also offer a more efficient alternative
 * solution in cases where there is no interception advice and therefore no need to
 * create a MethodInvocation object.
 * <br>Used internally by the AOP framework: application developers should not need
 * to use this class directly.
 * @author Rod Johnson
 * @version $Id: AfterReturningAdviceInterceptor.java,v 1.3 2004/03/19 18:43:17 johnsonr Exp $
 */
final class AfterReturningAdviceInterceptor implements MethodInterceptor {
	
	private AfterReturningAdvice advice;
	
	public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
		this.advice = advice;
	}

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		Object retval = mi.proceed();
		advice.afterReturning(retval, mi.getMethod(), mi.getArguments(), mi.getThis() );
		return retval;
	}
	
}