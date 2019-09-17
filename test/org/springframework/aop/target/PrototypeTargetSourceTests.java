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

package org.springframework.aop.target;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rod Johnson
 * @version $Id: PrototypeTargetSourceTests.java,v 1.4 2004/03/18 03:01:18 trisberg Exp $
 */
public class PrototypeTargetSourceTests extends TestCase {
	
	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;
	
	private BeanFactory beanFactory;
	
	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("prototypeTests.xml", getClass()));
	}

	/**
	 * Test that multiple invocations of the prototype bean will result
	 * in no change to visible state, as a new instance is used.
	 * With the singleton, there will be change.
	 */
	public void testPrototypeAndSingletonBehaveDifferently() {
		SideEffectBean singleton = (SideEffectBean) beanFactory.getBean("singleton");
		assertEquals(INITIAL_COUNT, singleton.getCount() );
		singleton.doWork();
		assertEquals(INITIAL_COUNT + 1, singleton.getCount() );
		
		SideEffectBean prototype = (SideEffectBean) beanFactory.getBean("prototype");
		assertEquals(INITIAL_COUNT, prototype.getCount() );
		prototype.doWork();
		assertEquals(INITIAL_COUNT, prototype.getCount() );
	}


}
