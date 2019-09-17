
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

import junit.framework.TestCase;

import org.springframework.aop.ClassFilter;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.core.NestedRuntimeException;

/**
 * @author Rod Johnson
 * @version $Id: ClassFiltersTests.java,v 1.4 2004/03/18 03:01:17 trisberg Exp $
 */
public class ClassFiltersTests extends TestCase {
	
	ClassFilter exceptionFilter = new RootClassFilter(Exception.class);
	
	ClassFilter itbFilter = new RootClassFilter(ITestBean.class);
	
	ClassFilter hasRootCauseFilter = new RootClassFilter(NestedRuntimeException.class);

	/**
	 * Constructor for ClassFiltersTests.
	 * @param arg0
	 */
	public ClassFiltersTests(String arg0) {
		super(arg0);
	}
	
	public void testUnion() {
		assertTrue(exceptionFilter.matches(RuntimeException.class));
		assertFalse(exceptionFilter.matches(TestBean.class));
		assertFalse(itbFilter.matches(Exception.class));
		assertTrue(itbFilter.matches(TestBean.class));
		ClassFilter union = ClassFilters.union(exceptionFilter, itbFilter);
		assertTrue(union.matches(RuntimeException.class));
		assertTrue(union.matches(TestBean.class));
	}
	
	public void testIntersection() {
		assertTrue(exceptionFilter.matches(RuntimeException.class));
		assertTrue(hasRootCauseFilter.matches(NestedRuntimeException.class));
		
		ClassFilter intersection = ClassFilters.intersection(exceptionFilter, hasRootCauseFilter);
		assertFalse(intersection.matches(RuntimeException.class));
		assertFalse(intersection.matches(TestBean.class));
		assertTrue(intersection.matches(NestedRuntimeException.class));
	}

}
