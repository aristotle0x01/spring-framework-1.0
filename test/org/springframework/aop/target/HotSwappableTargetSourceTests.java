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

import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rod Johnson
 * @version $Id: HotSwappableTargetSourceTests.java,v 1.4 2004/03/18 03:01:18 trisberg Exp $
 */
public class HotSwappableTargetSourceTests extends TestCase {

	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;

	private XmlBeanFactory beanFactory;
	
	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("hotSwapTests.xml", getClass()));
	}
	
	/**
	 * We must simulate container shutdown, which should clear threads.
	 */
	protected void tearDown() {
		// Will call pool.close()
		this.beanFactory.destroySingletons();
	}

	/**
	 * Check it works like a normal invoker
	 *
	 */
	public void testBasicFunctionality() {
		SideEffectBean target1 = (SideEffectBean) beanFactory.getBean("target1");
		SideEffectBean proxied = (SideEffectBean) beanFactory.getBean("swappable");
		assertEquals(INITIAL_COUNT, proxied.getCount() );
		proxied.doWork();
		assertEquals(INITIAL_COUNT + 1, proxied.getCount() );
		
		proxied = (SideEffectBean) beanFactory.getBean("swappable");
		proxied.doWork();
		assertEquals(INITIAL_COUNT + 2, proxied.getCount() );
	}
	
	public void testValidSwaps() {
		SideEffectBean target1 = (SideEffectBean) beanFactory.getBean("target1");
		SideEffectBean target2 = (SideEffectBean) beanFactory.getBean("target2");
		
		SideEffectBean proxied = (SideEffectBean) beanFactory.getBean("swappable");
	//	assertEquals(target1, ((Advised) proxied).getTarget());
		assertEquals(target1.getCount(), proxied.getCount() );
		proxied.doWork();
		assertEquals(INITIAL_COUNT + 1, proxied.getCount() );
	
		HotSwappableTargetSource swapper = (HotSwappableTargetSource) beanFactory.getBean("swapper");
		Object old = swapper.swap(target2);
		assertEquals("Correct old target was returned", target1, old);
		
		// TODO should be able to make this assertion: need to fix target handling
		// in AdvisedSupport
		//assertEquals(target2, ((Advised) proxied).getTarget());
		
		assertEquals(20, proxied.getCount());
		proxied.doWork();
		assertEquals(21, target2.getCount());
		
		// Swap it back
		swapper.swap(target1);
		assertEquals(target1.getCount(), proxied.getCount());
	}
	
	
	/**
	 * 
	 * @param invalid
	 * @return the message
	 */
	private AopConfigException testRejectsSwapToInvalidValue(Object invalid) {
		HotSwappableTargetSource swapper = (HotSwappableTargetSource) beanFactory.getBean("swapper");
		AopConfigException aopex = null;
		try {
			swapper.swap(invalid);
			fail("Shouldn't be able to swap to invalid value [" + invalid + "]");
		}
		catch (AopConfigException ex) {
			// Ok
			aopex = ex;
		}
		
		// It shouldn't be corrupted, it should still work
		testBasicFunctionality();
		return aopex;
	}
	
	public void testRejectsSwapToNull() {
		AopConfigException ex = testRejectsSwapToInvalidValue(null);
		assertTrue(ex.getMessage().indexOf("null") != -1);
	}
	
	// TODO test reject swap to wrong interface or class?
	// how to decide what's valid?
	
	
}
