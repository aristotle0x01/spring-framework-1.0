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

package org.springframework.context.support;

import junit.framework.TestCase;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class ClasspathXmlApplicationContextTests extends TestCase {
	
	public ClasspathXmlApplicationContextTests(String name) {
		super(name);
	}
	
	public void testMultiple() throws Exception {
		DefaultResourceLoader dr = new DefaultResourceLoader();
		Resource r1 = dr.getResource("/org/springframework/context/support/contextB.xml");
		Resource r = dr.getResource("classpath:/org/springframework/context/TestListener.class");
		Resource r2 = dr.getResource("file:/data/logs/exception-2020-03-09.0.log");

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { 
				"/org/springframework/context/support/contextB.xml",
				"/org/springframework/context/support/contextC.xml",
				"/org/springframework/context/support/contextA.xml" });
	}

}
