
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

package org.springframework.jndi;

import junit.framework.TestCase;


public class JndiTemplateEditorTests extends TestCase {

	
	public JndiTemplateEditorTests(String arg0) {
		super(arg0);
	}
	
	public void testNullIsIllegalArgument() {
		try {
			new JndiTemplateEditor().setAsText(null);
			fail("Null is illegal");
		}
		catch (IllegalArgumentException ex) {
			// OK
		}
	}
	
	/**
	 * There's a property string, but it's format is bogus.
	 *
	 */
//	public void testBogusFormat() {
//		try {
//			new JndiTemplateEditor().setAsText("=erever=====");
//			fail("Bogus properties");
//		}
//		catch (IllegalArgumentException ex) {
//			// OK
//		}
//	}
	
	public void testEmptyStringMeansNullEnvironment() {
		JndiTemplateEditor je = new JndiTemplateEditor();
		je.setAsText("");
		JndiTemplate jt = (JndiTemplate) je.getValue();
		assertTrue(jt.getEnvironment() == null);
	}
	
	public void testCustomEnvironment() {
		JndiTemplateEditor je = new JndiTemplateEditor();
		// These properties are meaningless for JNDI, but we don't worry about that:
		// the underlying JNDI implementation will throw exceptions when the user tries
		// to look anything up
		je.setAsText("jndiInitialSomethingOrOther=org.springframework.myjndi.CompleteRubbish\nfoo=bar");
		JndiTemplate jt = (JndiTemplate) je.getValue();
		assertTrue(jt.getEnvironment().size() == 2);
		assertTrue(jt.getEnvironment().getProperty("jndiInitialSomethingOrOther").equals("org.springframework.myjndi.CompleteRubbish"));
		assertTrue(jt.getEnvironment().getProperty("foo").equals("bar"));
	}

}
