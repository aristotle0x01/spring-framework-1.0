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

package org.springframework.aop;

/**
 * 
 * @author Dmitriy Kopylenko
 * @version $Id: SimpleBeforeAdviceImpl.java,v 1.2 2004/03/18 03:01:17 trisberg Exp $
 */
public class SimpleBeforeAdviceImpl implements SimpleBeforeAdvice {
	
	private int invocationCounter;

	/**
	 * @see org.springframework.aop.SimpleBeforeAdvice#before()
	 */
	public void before() throws Throwable {
		System.out.println("before() method is called on " + getClass().getName());
		++invocationCounter;
	}

	public int getInvocationCounter() {
		return invocationCounter;
	}

}
