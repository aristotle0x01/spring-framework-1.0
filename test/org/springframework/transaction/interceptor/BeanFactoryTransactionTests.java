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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.CountingTxManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Test cases for AOP transaction management.
 * @author Rod Johnson
 * @since 23-Apr-2003
 * @version $Id: BeanFactoryTransactionTests.java,v 1.20 2004/03/18 03:01:17 trisberg Exp $
 */
public class BeanFactoryTransactionTests extends TestCase {

	private BeanFactory factory;

	public void setUp() {
		this.factory = new XmlBeanFactory(new ClassPathResource("transactionalBeanFactory.xml", getClass()));
		ITestBean testBean = (ITestBean) factory.getBean("target");
		testBean.setAge(666);
	}

	public void testGetsAreNotTransactionalWithProxyFactory1() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory1");
		assertTrue("testBean is a dynamic proxy", Proxy.isProxyClass(testBean.getClass()));
		executeGetsAreNotTransactional(testBean);
	}

	public void testGetsAreNotTransactionalWithProxyFactory2() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory2");
		assertTrue("testBean is a dynamic proxy", Proxy.isProxyClass(testBean.getClass()));
		executeGetsAreNotTransactional(testBean);
	}

	public void testGetsAreNotTransactionalWithProxyFactory3() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory3");
		assertTrue("testBean is a full proxy", testBean instanceof DerivedTestBean);
		InvocationCounterPointcut txnCounter = (InvocationCounterPointcut) factory.getBean("txnInvocationCounterPointcut");
		InvocationCounterInterceptor preCounter = (InvocationCounterInterceptor) factory.getBean("preInvocationCounterInterceptor");
		InvocationCounterInterceptor postCounter = (InvocationCounterInterceptor) factory.getBean("postInvocationCounterInterceptor");
		txnCounter.counter = 0;
		preCounter.counter = 0;
		postCounter.counter = 0;
		executeGetsAreNotTransactional(testBean);
		// Can't assert it's equal to 4 as the pointcut may be optimized and only invoked once
		assertTrue(0 < txnCounter.counter && txnCounter.counter <= 4);
		assertEquals(4, preCounter.counter);
		assertEquals(4, postCounter.counter);
	}

	public void executeGetsAreNotTransactional(ITestBean testBean) throws NoSuchMethodException {
		// Install facade
		MockControl ptmControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptmControl.getMock();
		// Expect no methods
		ptmControl.replay();
		PlatformTransactionManagerFacade.delegate = ptm;

		assertTrue("Age should not be " + testBean.getAge(), testBean.getAge() == 666);
		// Check no calls
		ptmControl.verify();

		// Install facade expecting a call
		final TransactionStatus ts = new DefaultTransactionStatus(null, true, false, false, false, null);
		ptm = new PlatformTransactionManager() {
			private boolean invoked;
			public TransactionStatus getTransaction(TransactionDefinition definition)
					throws TransactionException {
				if (invoked) {
					throw new IllegalStateException("getTransaction should not get invoked more than once");
				}
				invoked = true;
				return ts;
			}
			public void commit(TransactionStatus status) throws TransactionException {
				assertTrue(status == ts);
			}
			public void rollback(TransactionStatus status) throws TransactionException {
				throw new IllegalStateException("rollback should not get invoked");
			}
		};
		PlatformTransactionManagerFacade.delegate = ptm;

		// TODO same as old age to avoid ordering effect for now
		int age = 666;
		testBean.setAge(age);
		assertTrue(testBean.getAge() == age);
		ptmControl.verify();
	}
	
	/**
	 * Check that we fail gracefully if the user doesn't
	 * set any transaction attributes.
	 */
	public void testNoTransactionAttributeSource() {
		try {
			XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("noTransactionAttributeSource.xml", getClass()));
			ITestBean testBean = (ITestBean) bf.getBean("noTransactionAttributeSource");
			fail("Should require TransactionAttributeSource to be set");
		}
		catch (FatalBeanException ex) {
			// Ok
		}
	}
	
	/**
	 * Test that we can set the target to a dynamic TargetSource
	 * @throws NoSuchMethodException
	 */
	public void testDynamicTargetSource() throws NoSuchMethodException {
		// Install facade
		CountingTxManager txMan = new CountingTxManager();
		PlatformTransactionManagerFacade.delegate = txMan;
		
		TestBean tb = (TestBean) factory.getBean("hotSwapped");
		assertEquals(666, tb.getAge());
		int newAge = 557;
		tb.setAge(newAge);
		assertEquals(newAge, tb.getAge());
		
		TestBean target2 = new TestBean();
		target2.setAge(65);
		HotSwappableTargetSource ts = (HotSwappableTargetSource) factory.getBean("swapper");
		ts.swap(target2);
		assertEquals(target2.getAge(), tb.getAge());
		tb.setAge(newAge);
		assertEquals(newAge, target2.getAge());
		
		assertEquals(0, txMan.inflight);
		assertEquals(2, txMan.commits);
		assertEquals(0, txMan.rollbacks);
	}


	public static class InvocationCounterPointcut extends StaticMethodMatcherPointcut {

		int counter = 0;

		public boolean matches(Method method, Class clazz) {
			counter++;
			return true;
		}
	}


	public static class InvocationCounterInterceptor implements MethodInterceptor {

		int counter = 0;

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			counter++;
			return methodInvocation.proceed();
		}
	}

}
