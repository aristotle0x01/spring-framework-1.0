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

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import junit.framework.TestCase;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.framework.adapter.ThrowsAdviceInterceptorTests;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcutAdvisor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;

/**
 * Test cases for AOP FactoryBean, using XML bean factory.
 * Note that this FactoryBean will work in any bean factory
 * implementation.
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: ProxyFactoryBeanTests.java,v 1.26 2004/03/23 14:33:06 jhoeller Exp $
 */
public class ProxyFactoryBeanTests extends TestCase {
	
	private BeanFactory factory;

	protected void setUp() throws Exception {
		this.factory = new XmlBeanFactory(new ClassPathResource("proxyFactoryTests.xml", getClass()));
	}

	public void testIsDynamicProxy() {
		ITestBean test1 = (ITestBean) factory.getBean("test1");
		assertTrue("test1 is a dynamic proxy", Proxy.isProxyClass(test1.getClass()));
	}
	
	public void testGetObjectTypeWithDirectTarget() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("proxyFactoryTargetSourceTests.xml", getClass()));
		
		// We have a counting before advice here
		CountingBeforeAdvice cba = (CountingBeforeAdvice) bf.getBean("countingBeforeAdvice");
		assertEquals(0, cba.getCalls());
	
		ITestBean tb = (ITestBean) bf.getBean("directTarget");
		assertTrue(tb.getName().equals("Adam"));
		assertEquals(1, cba.getCalls());
		
		ProxyFactoryBean pfb = (ProxyFactoryBean) bf.getBean("&directTarget");
		assertEquals("Has correct object type", TestBean.class, pfb.getObjectType());
	}
	
	public void testGetObjectTypeWithTargetViaTargetSource() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("proxyFactoryTargetSourceTests.xml", getClass()));
		ITestBean tb = (ITestBean) bf.getBean("viaTargetSource");
		assertTrue(tb.getName().equals("Adam"));
		ProxyFactoryBean pfb = (ProxyFactoryBean) bf.getBean("&viaTargetSource");
		assertEquals("Has correct object type", TestBean.class, pfb.getObjectType());
	}
	
	public void testGetObjectTypeWithNoTargetOrTargetSource() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("proxyFactoryTargetSourceTests.xml", getClass()));

		ITestBean tb = (ITestBean) bf.getBean("noTarget");
		try {
			tb.getName();
			fail();
		}
		catch (UnsupportedOperationException ex) {
			assertEquals("getName", ex.getMessage());
		}
		FactoryBean pfb = (ProxyFactoryBean) bf.getBean("&noTarget");
		assertNull("Has null object type", pfb.getObjectType());
	}
	
	/**
	 * The instances are equal, but do not have object identity.
	 * Interceptors and interfaces and the target are the same.
	 */
	public void testSingletonInstancesAreEqual() {
		ITestBean test1 = (ITestBean) factory.getBean("test1");
		ITestBean test1_1 = (ITestBean) factory.getBean("test1");
		//assertTrue("Singleton instances ==", test1 == test1_1);
		assertEquals("Singleton instances ==", test1, test1_1);
		test1.setAge(25);
		assertEquals(test1.getAge(), test1_1.getAge());
		test1.setAge(250);
		assertEquals(test1.getAge(), test1_1.getAge());
		Advised pc1 = (Advised) test1;
		Advised pc2 = (Advised) test1_1;
		assertEquals(pc1.getAdvisors(), pc2.getAdvisors());
		int oldLength = pc1.getAdvisors().length;
		NopInterceptor di = new NopInterceptor();
		pc1.addInterceptor(1, di);
		assertEquals(pc1.getAdvisors(), pc2.getAdvisors());
		assertEquals("Now have one more advisor", oldLength + 1, pc2.getAdvisors().length);
		assertEquals(di.getCount(), 0);
		test1.setAge(5);
		assertEquals(test1_1.getAge(), test1.getAge());
		assertEquals(di.getCount(), 3);
	}
	
	
	public void testPrototypeInstancesAreNotEqual() {
		ITestBean test2 = (ITestBean) factory.getBean("prototype");
		ITestBean test2_1 = (ITestBean) factory.getBean("prototype");
		assertTrue("Prototype instances !=", test2 != test2_1);
		assertTrue("Prototype instances equal", test2.equals(test2_1));
	}
	
	
	/**
	 * Uses its own bean factory XML for clarity
	 * @param beanName name of the ProxyFactoryBean definition that should
	 * be a prototype
	 */
	private Object testPrototypeInstancesAreIndependent(String beanName) {
		// Initial count value set in bean factory XML 
		int INITIAL_COUNT = 10;
		
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("prototypeTests.xml", getClass()));
		
		// Check it works without AOP
		SideEffectBean raw = (SideEffectBean) bf.getBean("prototypeTarget");
		assertEquals(INITIAL_COUNT, raw.getCount() );
		raw.doWork();
		assertEquals(INITIAL_COUNT+1, raw.getCount() );
		raw = (SideEffectBean) bf.getBean("prototypeTarget");
		assertEquals(INITIAL_COUNT, raw.getCount() );
		
		// Now try with advised instances
		SideEffectBean prototype2FirstInstance = (SideEffectBean) bf.getBean(beanName);
		assertEquals(INITIAL_COUNT, prototype2FirstInstance.getCount() );
		prototype2FirstInstance.doWork();
		assertEquals(INITIAL_COUNT + 1, prototype2FirstInstance.getCount() );

		SideEffectBean prototype2SecondInstance = (SideEffectBean) bf.getBean(beanName);
		assertFalse("Prototypes are not ==", prototype2FirstInstance == prototype2SecondInstance);
		assertEquals(INITIAL_COUNT, prototype2SecondInstance.getCount() );
		assertEquals(INITIAL_COUNT + 1, prototype2FirstInstance.getCount() );
		
		return prototype2FirstInstance;
	}
	
	public void testCglibPrototypeInstancesAreIndependent() {
		Object prototype = testPrototypeInstancesAreIndependent("cglibPrototype");
		assertTrue("It's a cglib proxy", AopUtils.isCglibProxy(prototype));
		assertFalse("It's not a dynamic proxy", AopUtils.isJdkDynamicProxy(prototype));
	}
	
	public void testPrototypeInstancesAreIndependentWithTargetName() {
		Object prototype = testPrototypeInstancesAreIndependent("prototype");
		//assertTrue("It's a dynamic proxy", AopUtils.isJdkDynamicProxy(prototype));
	}
	
	/**
	 * Test invoker is automatically added to manipulate target
	 */
	public void testAutoInvoker() {
		String name = "Hieronymous";
		TestBean target = (TestBean) factory.getBean("test");
		target.setName(name);
		ITestBean autoInvoker = (ITestBean) factory.getBean("autoInvoker");
		assertTrue(autoInvoker.getName().equals(name));
	}

	public void testCanGetFactoryReferenceAndManipulate() {
		ProxyFactoryBean config = (ProxyFactoryBean) factory.getBean("&test1");
		assertEquals("Have one advisors", 1, config.getAdvisors().length);
		
		ITestBean tb = (ITestBean) factory.getBean("test1");
		// no exception 
		tb.hashCode();
		
		final Exception ex = new UnsupportedOperationException("invoke");
		// Add evil interceptor to head of list
		config.addInterceptor(0, new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				throw ex;
			}
		});
		assertEquals("Have correct advisor count", 2, config.getAdvisors().length);
		
		tb = (ITestBean) factory.getBean("test1"); 
		try {
			// Will fail now
			tb.hashCode();
			fail("Evil interceptor added programmatically should fail all method calls");
		} 
		catch (Exception thrown) {
			assertTrue(thrown == ex);
		}
	}
	
	/**
	 * Should see effect immediately on behaviour.
	 */
	public void testCanAddAndRemoveAspectInterfacesOnSingleton() {
		try {
			TimeStamped ts = (TimeStamped) factory.getBean("test1");
			fail("Shouldn't implement TimeStamped before manipulation");
		}
		catch (ClassCastException ex) {
		}
	
		ProxyFactoryBean config = (ProxyFactoryBean) factory.getBean("&test1");
		long time = 666L;
		TimestampIntroductionInterceptor ti = new TimestampIntroductionInterceptor();
		ti.setTime(time);
		
		// add to front of interceptor chain
		int oldCount = config.getAdvisors().length;
		config.addAdvisor(0, new DefaultIntroductionAdvisor(ti, TimeStamped.class));
		
		assertTrue(config.getAdvisors().length == oldCount + 1);
	
		TimeStamped ts = (TimeStamped) factory.getBean("test1");
		assertTrue(ts.getTimeStamp() == time);
	
		// Can remove
		config.removeInterceptor(ti);

		assertTrue(config.getAdvisors().length == oldCount);
	
		try {
			// Existing reference will fail
			ts.getTimeStamp();
			fail("Existing object won't implement this interface any more");
		}
		catch (RuntimeException ex) {
		}

	
		try {
			ts = (TimeStamped) factory.getBean("test1");
			fail("Should no longer implement TimeStamped");
		}
		catch (ClassCastException ex) {
		}
	
		// Now check non-effect of removing interceptor that isn't there
		config.removeInterceptor(new DebugInterceptor());
	
		assertTrue(config.getAdvisors().length == oldCount);
	
		ITestBean it = (ITestBean) ts;
		DebugInterceptor debugInterceptor = new DebugInterceptor();
		config.addInterceptor(0, debugInterceptor);
		it.getSpouse();
		assertEquals(1, debugInterceptor.getCount());
		config.removeInterceptor(debugInterceptor);
		it.getSpouse();
		// not invoked again
		assertTrue(debugInterceptor.getCount() == 1);
	}
	
	
	/**
	 * Try adding and removing interfaces and interceptors on prototype.
	 * Changes will only affect future references obtained from the factory.
	 * Each instance will be independent.
	 */
	public void testCanAddAndRemoveAspectInterfacesOnPrototype() {
		try {
			TimeStamped ts = (TimeStamped) factory.getBean("test2");
			fail("Shouldn't implement TimeStamped before manipulation");
		}
		catch (ClassCastException ex) {
		}
		
		ProxyFactoryBean config = (ProxyFactoryBean) factory.getBean("&test2");
		long time = 666L;
		TimestampIntroductionInterceptor ti = new TimestampIntroductionInterceptor();
		ti.setTime(time);
		// Add to head of interceptor chain
		int oldCount = config.getAdvisors().length;
		config.addAdvisor(0, new DefaultIntroductionAdvisor(ti, TimeStamped.class));
		assertTrue(config.getAdvisors().length == oldCount + 1);
		
		TimeStamped ts = (TimeStamped) factory.getBean("test2");
		assertEquals(time, ts.getTimeStamp());
		
		// Can remove
		config.removeInterceptor(ti);
		assertTrue(config.getAdvisors().length == oldCount);
		
		// Check no change on existing object reference
		assertTrue(ts.getTimeStamp() == time);
		
		try {
			ts = (TimeStamped) factory.getBean("test2");
			fail("Should no longer implement TimeStamped");
		}
		catch (ClassCastException ex) {
		}
		
		// Now check non-effect of removing interceptor that isn't there
		config.removeInterceptor(new DebugInterceptor());
		assertTrue(config.getAdvisors().length == oldCount);
		
		ITestBean it = (ITestBean) ts;
		DebugInterceptor debugInterceptor = new DebugInterceptor();
		config.addInterceptor(0, debugInterceptor);
		it.getSpouse();
		// Won't affect existing reference
		assertTrue(debugInterceptor.getCount() == 0);
		it = (ITestBean) factory.getBean("test2");
		it.getSpouse();
		assertEquals(1, debugInterceptor.getCount());
		config.removeInterceptor(debugInterceptor);
		it.getSpouse();
		
		// Still invoked wiht old reference
		assertEquals(2, debugInterceptor.getCount());
		
		// not invoked with new object
		it = (ITestBean) factory.getBean("test2");
		it.getSpouse();
		assertEquals(2, debugInterceptor.getCount());
		
		// Our own timestamped reference should still work
		assertEquals(time, ts.getTimeStamp());
	}
	
	
	
	/**
	 * Note that we can't add or remove interfaces without reconfiguring the
	 * singleton. 
	 * TODO address this?
	 *
	 */
	public void testCanAddAndRemoveAspectInterfacesOnSingletonByCasting() {
		ITestBean it = (ITestBean) factory.getBean("test1");
		Advised pc = (Advised) it;
		it.getAge();
		NopInterceptor di = new NopInterceptor();
		pc.addInterceptor(0, di);
		assertEquals(0, di.getCount());
		it.setAge(25);
		assertEquals(25, it.getAge());
		assertEquals(2, di.getCount());
	}
	
	
	public void testMethodPointcuts() {
		ITestBean tb = (ITestBean) factory.getBean("pointcuts");
		PointcutForVoid.reset();
		assertTrue("No methods intercepted", PointcutForVoid.methodNames.isEmpty());
		tb.getAge();
		assertTrue("Not void: shouldn't have intercepted", PointcutForVoid.methodNames.isEmpty());
		tb.setAge(1);
		tb.getAge();
		tb.setName("Tristan");
		tb.toString();
		assertEquals("Recorded wrong number of invocations", 2, PointcutForVoid.methodNames.size());
		assertTrue(PointcutForVoid.methodNames.get(0).equals("setAge"));
		assertTrue(PointcutForVoid.methodNames.get(1).equals("setName"));
	}
	
	public void testCanAddThrowsAdviceWithoutAdvisor() throws Throwable {
		BeanFactory f = new XmlBeanFactory(new ClassPathResource("throwsAdvice.xml", getClass()));
		ThrowsAdviceInterceptorTests.MyThrowsHandler th = (ThrowsAdviceInterceptorTests.MyThrowsHandler) f.getBean("throwsAdvice");
		CountingBeforeAdvice cba = (CountingBeforeAdvice) f.getBean("countingBeforeAdvice");
		assertEquals(0, cba.getCalls());
		assertEquals(0, th.getCalls());
		ThrowsAdviceInterceptorTests.IEcho echo = (ThrowsAdviceInterceptorTests.IEcho) f.getBean("throwsAdvised");
		int i = 12;
		echo.setA(i);
		assertEquals(i, echo.getA());
		assertEquals(2, cba.getCalls());
		assertEquals(0, th.getCalls());
		Exception expected = new Exception();
		try {
			echo.echoException(1, expected);
			fail();
		}
		catch (Exception ex) {
			assertEquals(expected, ex);
		}
		// No throws handler method: count should still be 0
		assertEquals(0, th.getCalls());
		
		// Handler knows how to handle this exception
		expected = new ServletException();
		try {
			echo.echoException(1, expected);
			fail();
		}
		catch (ServletException ex) {
			assertEquals(expected, ex);
		}
		// One match
		assertEquals(1, th.getCalls("servletException"));
	}
	
	
	// These two fail the whole bean factory
	// TODO put in sep file to check quality of error message
	/*
	public void testNoInterceptorNamesWithoutTarget() {
		try {
			ITestBean tb = (ITestBean) factory.getBean("noInterceptorNamesWithoutTarget");
			fail("Should require interceptor names");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}
	
	public void testNoInterceptorNamesWithTarget() {
		ITestBean tb = (ITestBean) factory.getBean("noInterceptorNamesWithoutTarget");
	}
	*.
	
	public void testEmptyInterceptorNames() {
		try {
			ITestBean tb = (ITestBean) factory.getBean("emptyInterceptorNames");
			fail("Interceptor names cannot be empty");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ok
		}
	}
	
	/**
	 * Globals must be followed by a target.
	 */
	public void testGlobalsWithoutTarget() {
		try {
			ITestBean tb = (ITestBean) factory.getBean("globalsWithoutTarget");
			fail("Should require target name");
		}
		catch (BeanCreationException ex) {
			assertTrue(ex.getCause() instanceof AopConfigException);
		}
	}
	
	/**
	 * Checks that globals get invoked,
	 * and that they can add aspect interfaces unavailable
	 * to other beans. These interfaces don't need
	 * to be included in proxiedInterface [].
	 */
	public void testGlobalsCanAddAspectInterfaces() {
		AddedGlobalInterface agi = (AddedGlobalInterface) factory.getBean("autoInvoker");
		assertTrue(agi.globalsAdded() == -1);
		
		ProxyFactoryBean pfb = (ProxyFactoryBean) factory.getBean("&validGlobals");
		// 2 globals + 2 explicit
		assertEquals("Have 2 globals and 2 explicit advisors", 3, pfb.getAdvisors().length);
		
		ApplicationListener l = (ApplicationListener) factory.getBean("validGlobals");
		agi = (AddedGlobalInterface) l;
		assertTrue(agi.globalsAdded() == -1);
		
		try {
			agi = (AddedGlobalInterface) factory.getBean("test1");
			fail("Aspect interface should't be implemeneted without globals");
		}
		catch (ClassCastException ex) {
		}
	}
	

	/**
	 * Fires only on void methods. Saves list of methods intercepted.
	 */
	public static class PointcutForVoid extends DynamicMethodMatcherPointcutAdvisor {
		
		public static List methodNames = new LinkedList();
		
		public static void reset() {
			methodNames.clear();
		}
		
		public PointcutForVoid() {
			super( new MethodInterceptor() {
				public Object invoke(MethodInvocation invocation) throws Throwable {
					methodNames.add(invocation.getMethod().getName());
					return invocation.proceed();
				}
			});
		}
		
		/** Should fire only if it returns null */
		public boolean matches(Method m, Class targetClass, Object[] args) {//, AttributeRegistry attributeRegistry) {
			//System.out.println(mi.getMethod().getReturnType());
			return m.getReturnType() == Void.TYPE;
		}
	}


	/**
	 * Aspect interface
	 */
	public interface AddedGlobalInterface {
		int globalsAdded();
	}


	/**
	 * Use as a global interceptor. Checks that 
	 * global interceptors can add aspect interfaces.
	 * NB: Add only via global interceptors in XML file.
	 */
	public static class GlobalAspectInterfaceInterceptor implements IntroductionInterceptor {

		public boolean implementsInterface(Class intf) {
			return intf.equals(AddedGlobalInterface.class);
		}

		public Object invoke(MethodInvocation mi) throws Throwable {
			if (mi.getMethod().getDeclaringClass().equals(AddedGlobalInterface.class)) {
				return new Integer(-1);
			}
			return mi.proceed();
		}
	}


	public static class GlobalIntroductionAdvice implements IntroductionAdvisor {
		
		private IntroductionInterceptor gi = new GlobalAspectInterfaceInterceptor();

		public ClassFilter getClassFilter() {
			return ClassFilter.TRUE;
		}

		public Advice getAdvice() {
			return this.gi;
		}

		public Class[] getInterfaces() {
			return new Class[] { AddedGlobalInterface.class };
		}

		public boolean isPerInstance() {
			return false;
		}
		
		public void validateInterfaces() {
		}
	}
	
}
