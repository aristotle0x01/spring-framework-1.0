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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ACATest;
import org.springframework.context.AbstractApplicationContextTests;
import org.springframework.context.BeanThatListens;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for static application context.
 * @author Rod Johnson
 * @version $Id: StaticApplicationContextTestSuite.java,v 1.26 2004/03/18 03:01:15 trisberg Exp $
 */
public class StaticApplicationContextTestSuite extends AbstractApplicationContextTests {

	protected StaticApplicationContext sac;

	/** Run for each test */
	protected ConfigurableApplicationContext createContext() throws Exception {
		StaticApplicationContext parent = new StaticApplicationContext();
		parent.addListener(parentListener) ;
		Map m = new HashMap();
		m.put("name", "Roderick");
		parent.registerPrototype("rod", TestBean.class, new MutablePropertyValues(m));
		m.put("name", "Albert");
		parent.registerPrototype("father", TestBean.class, new MutablePropertyValues(m));
		parent.refresh();

		StaticMessageSource parentMessageSource = (StaticMessageSource) parent.getBean("messageSource");
		parentMessageSource.addMessage("code1", Locale.getDefault(), "message1");

		this.sac = new StaticApplicationContext(parent);
		sac.addListener(listener);
		sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
		sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
		sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());
		PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(sac.getDefaultListableBeanFactory());
		reader.loadBeanDefinitions(new ClassPathResource("testBeans.properties", getClass()));
		sac.refresh();

		StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
		sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");

		return sac;
	}

	/** Overridden */
	public void testCount() {
		assertCount(16);
	}

}
