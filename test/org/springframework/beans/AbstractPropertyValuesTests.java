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

package org.springframework.beans;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;


/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractPropertyValuesTests extends TestCase { 


	/** Creates new SeatingPlanTest */
	public AbstractPropertyValuesTests(String name) {
		super(name);
	}

	/** Run for each test */
	protected void setUp() throws Exception {
	}

/** Must contain: forname=Tony surname=Blair age=50
 * 
 */
	
	protected void testTony(PropertyValues pvs) throws Exception {
		
		assertTrue("Contains 3", pvs.getPropertyValues().length == 3);
		assertTrue("Contains forname", pvs.contains("forname"));
		assertTrue("Contains surname", pvs.contains("surname"));
		assertTrue("Contains age", pvs.contains("age"));
		assertTrue("Doesn't contain tory", !pvs.contains("tory"));
		
		PropertyValue[] ps = pvs.getPropertyValues();
		Map m = new HashMap();
		m.put("forname", "Tony");
		m.put("surname", "Blair");
		m.put("age", "50");
		for (int i = 0; i < ps.length; i++) {
			Object val = m.get(ps[i].getName());
			assertTrue("Can't have unexpected value", val != null);
			assertTrue("Val i string", val instanceof String);
			assertTrue("val matches expected", val.equals(ps[i].getValue()));
			m.remove(ps[i].getName());
		}
		assertTrue("Map size is 0", m.size() == 0);
	}
	
	// NULL TESTS ETC.

	

}
