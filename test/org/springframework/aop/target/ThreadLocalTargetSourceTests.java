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
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rod Johnson
 * @version $Id: ThreadLocalTargetSourceTests.java,v 1.5 2004/03/18 03:01:18 trisberg Exp $
 */
public class ThreadLocalTargetSourceTests extends TestCase {

	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;

	private XmlBeanFactory beanFactory;
	
	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("threadLocalTests.xml", getClass()));
	}
	
	/**
	 * We must simulate container shutdown, which should clear threads.
	 */
	protected void tearDown() {
		this.beanFactory.destroySingletons();
	}
	
	/**
	 * Check we can use two different ThreadLocalTargetSources
	 * managing objects of different types without them interfering
	 * with one another.
	 */
	public void testUseDifferentManagedInstancesInSameThread() {
			SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT, apartment.getCount() );
		apartment.doWork();
		assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
	
		ITestBean test = (ITestBean) beanFactory.getBean("threadLocal2");
		assertEquals("Rod", test.getName());
		assertEquals("Kerry", test.getSpouse().getName());
	}

	public void testReuseInSameThread() {
		SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT, apartment.getCount() );
		apartment.doWork();
		assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
		
		apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
	}
	
	/**
	 * Relies on introduction
	 *
	 */
	public void testCanGetStatsViaMixin() {
		ThreadLocalTargetSourceStats stats = (ThreadLocalTargetSourceStats) beanFactory.getBean("apartment");
		// +1 because creating target for stats call counts
		assertEquals(1, stats.getInvocations());
		SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		apartment.doWork();
		// +1 again
		assertEquals(3, stats.getInvocations());
		// + 1 for states call!
		assertEquals(3, stats.getHits());
		apartment.doWork();
		assertEquals(6, stats.getInvocations());
		assertEquals(6, stats.getHits());
		// Only one thread so only one object can have been bound
		assertEquals(1, stats.getObjects());
	}
	

	public void testNewThreadHasOwnInstance() throws InterruptedException {
		SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT, apartment.getCount() );
		apartment.doWork();
		apartment.doWork();
		apartment.doWork();
		assertEquals(INITIAL_COUNT + 3, apartment.getCount() );
	
		class Runner implements Runnable {
			public SideEffectBean mine;
			public void run() {
				this.mine = (SideEffectBean) beanFactory.getBean("apartment");
				assertEquals(INITIAL_COUNT, mine.getCount() );
				mine.doWork();
				assertEquals(INITIAL_COUNT + 1, mine.getCount() );
			}
		}
		Runner r = new Runner();
		Thread t = new Thread(r);
		t.start();
		t.join();
		
		assertNotNull(r);
		
		// Check it didn't affect the other thread's copy
		assertEquals(INITIAL_COUNT + 3, apartment.getCount() );
		
		// When we use other thread's copy in this thread 
		// it should behave like ours
		assertEquals(INITIAL_COUNT + 3, r.mine.getCount() );
		
		// Bound to two threads
		assertEquals(2, ((ThreadLocalTargetSourceStats) apartment).getObjects());
	}

}
