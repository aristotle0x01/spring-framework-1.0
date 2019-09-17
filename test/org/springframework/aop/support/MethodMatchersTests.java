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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.aop.MethodMatcher;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * $Id: MethodMatchersTests.java,v 1.3 2004/03/18 03:01:17 trisberg Exp $
 */
public class MethodMatchersTests extends TestCase {

	final Method EXCEPTION_GETMESSAGE;

	final Method ITESTBEAN_SETAGE;
	
	final Method ITESTBEAN_GETAGE;
	
	final Method IOTHER_ABSQUATULATE;

	/**
	 * Constructor for DefaultMethodMatcherTests.
	 * @param arg0
	 */
	public MethodMatchersTests(String arg0) throws Exception {
		super(arg0);
		EXCEPTION_GETMESSAGE = Exception.class.getMethod("getMessage", null);
		ITESTBEAN_GETAGE = ITestBean.class.getMethod("getAge", null);
		ITESTBEAN_SETAGE = ITestBean.class.getMethod("setAge", new Class[] { int.class });
		IOTHER_ABSQUATULATE = IOther.class.getMethod("absquatulate", null);
	}

	public void testDefaultMatchesAll() throws Exception {
		MethodMatcher defaultMm = MethodMatcher.TRUE;
		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertTrue(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
	}

	public void testSingle() throws Exception {
		MethodMatcher defaultMm = MethodMatcher.TRUE;
		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertTrue(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
		defaultMm = MethodMatchers.intersection(defaultMm, new StartsWithMatcher("get"));

		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertFalse(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
	}

	
	public void testDynamicAndStaticMethodMatcherIntersection() throws Exception {
		MethodMatcher mm1 = MethodMatcher.TRUE;
		MethodMatcher mm2 = new TestDynamicMethodMatcherWhichMatches();
		MethodMatcher intersection = MethodMatchers.intersection(mm1, mm2);
		assertTrue("Intersection is a dynamic matcher", intersection.isRuntime());
		assertTrue("2Matched setAge method", intersection.matches(ITESTBEAN_SETAGE, TestBean.class));
		assertTrue("3Matched setAge method", intersection.matches(ITESTBEAN_SETAGE, TestBean.class, new Object[] { new Integer(5) }));
		// Knock out dynamic part
		intersection = MethodMatchers.intersection(intersection, new TestDynamicMethodMatcherWhichDoesNotMatch());
		assertTrue("Intersection is a dynamic matcher", intersection.isRuntime());
		assertTrue("2Matched setAge method", intersection.matches(ITESTBEAN_SETAGE, TestBean.class));
		assertFalse("3 - not Matched setAge method", intersection.matches(ITESTBEAN_SETAGE, TestBean.class, new Object[] { new Integer(5) }));
	}
	
	public void testStaticMethodMatcherUnion() throws Exception {
		MethodMatcher getterMatcher = new StartsWithMatcher("get");
		MethodMatcher setterMatcher = new StartsWithMatcher("set");
		MethodMatcher union = MethodMatchers.union(getterMatcher, setterMatcher);
		
		assertFalse("Union is a static matcher", union.isRuntime());
		assertTrue("Matched setAge method", union.matches(ITESTBEAN_SETAGE, TestBean.class));
		assertTrue("Matched getAge method", union.matches(ITESTBEAN_GETAGE, TestBean.class));
		assertFalse("Didn't matched absquatulate method", union.matches(IOTHER_ABSQUATULATE, TestBean.class));

	}


	public static class StartsWithMatcher extends StaticMethodMatcher {
		private String prefix;
		public StartsWithMatcher(String s) {
			this.prefix = s;
		}
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith(prefix);
		}
	}


	private static class TestDynamicMethodMatcherWhichMatches extends DynamicMethodMatcher {
		public boolean matches(Method m, Class targetClass, Object[] args) {
			return true;
		}
	}

	private static class TestDynamicMethodMatcherWhichDoesNotMatch extends DynamicMethodMatcher {
		public boolean matches(Method m, Class targetClass, Object[] args) {
			return false;
		}
	}

}
