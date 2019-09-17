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

package org.springframework.aop.support;

import org.aopalliance.aop.Advice;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;

/**
 * Convenient superclass for Advisors that are also dynamic pointcuts.
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcherPointcutAdvisor extends DynamicMethodMatcher
    implements PointcutAdvisor, Pointcut, Ordered {

	private int order = Integer.MAX_VALUE;

	private Advice advice;
	
	protected DynamicMethodMatcherPointcutAdvisor() {
	}

	protected DynamicMethodMatcherPointcutAdvisor(Advice advice) {
		this.advice = advice;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}
	
	public Advice getAdvice() {
		return advice;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public final Pointcut getPointcut() {
		return this;
	}

	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
