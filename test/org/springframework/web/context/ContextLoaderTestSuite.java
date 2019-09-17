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

package org.springframework.web.context;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.LifecycleBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockServletConfig;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.SimpleWebApplicationContext;

/**
 * @author Juergen Hoeller
 * @since 12.08.2003
 */
public class ContextLoaderTestSuite extends TestCase {

	public void testContextLoaderListenerWithDefaultContext() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM,
												"/org/springframework/web/context/WEB-INF/applicationContext.xml /org/springframework/web/context/WEB-INF/context-addition.xml");
		ServletContextListener listener = new ContextLoaderListener();
		ServletContextEvent event = new ServletContextEvent(sc);
		listener.contextInitialized(event);
		WebApplicationContext context = (WebApplicationContext)sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		assertTrue("Correct WebApplicationContext exposed in ServletContext", context instanceof XmlWebApplicationContext);
		LifecycleBean lb = (LifecycleBean) context.getBean("lifecycle");
		assertTrue("Has father", context.containsBean("father"));
		assertTrue("Has rod", context.containsBean("rod"));
		assertTrue("Has kerry", context.containsBean("kerry"));
		assertTrue("Not destroyed", !lb.isDestroyed());
		listener.contextDestroyed(event);
		assertTrue("Destroyed", lb.isDestroyed());
	}

	public void testContextLoaderServletWithDefaultContext() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "/org/springframework/web/context/WEB-INF/applicationContext.xml");
		HttpServlet servlet = new ContextLoaderServlet();
		ServletConfig config = new MockServletConfig(sc, "test");
		servlet.init(config);
		WebApplicationContext wc = (WebApplicationContext)sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		assertTrue("Correct WebApplicationContext exposed in ServletContext", wc instanceof XmlWebApplicationContext);
		LifecycleBean lb = (LifecycleBean) wc.getBean("lifecycle");
		assertTrue("Not destroyed", !lb.isDestroyed());
		servlet.destroy();
		assertTrue("Destroyed", lb.isDestroyed());
	}

	public void testContextLoaderWithCustomContext() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, "org.springframework.web.servlet.SimpleWebApplicationContext");
		ServletContextListener listener = new ContextLoaderListener();
		ServletContextEvent event = new ServletContextEvent(sc);
		listener.contextInitialized(event);
		WebApplicationContext wc = (WebApplicationContext) sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		assertTrue("Correct WebApplicationContext exposed in ServletContext", wc instanceof SimpleWebApplicationContext);
	}

	public void testContextLoaderWithInvalidLocation() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "/WEB-INF/myContext.xml");
		ServletContextListener listener = new ContextLoaderListener();
		ServletContextEvent event = new ServletContextEvent(sc);
		try {
			listener.contextInitialized(event);
			fail("Should have thrown ApplicationContextException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
			assertTrue(ex.getCause() instanceof FileNotFoundException);
		}
	}

	public void testContextLoaderWithInvalidContext() throws Exception {
		MockServletContext sc = new MockServletContext("");
		sc.addInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, "org.springframework.web.context.support.InvalidWebApplicationContext");
		ServletContextListener listener = new ContextLoaderListener();
		ServletContextEvent event = new ServletContextEvent(sc);
		try {
			listener.contextInitialized(event);
			fail("Should have thrown ApplicationContextException");
		}
		catch (ApplicationContextException ex) {
			// expected
			assertTrue(ex.getCause() instanceof ClassNotFoundException);
		}
	}

	public void testContextLoaderWithDefaultLocation() throws Exception {
		MockServletContext sc = new MockServletContext("");
		ServletContextListener listener = new ContextLoaderListener();
		ServletContextEvent event = new ServletContextEvent(sc);
		try {
			listener.contextInitialized(event);
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
			ex.printStackTrace();
			assertTrue(ex.getCause() instanceof IOException);
			assertTrue(ex.getCause().getMessage().indexOf("/WEB-INF/applicationContext.xml") != -1);
		}
	}

	public void testFrameworkServletWithDefaultLocation() throws Exception {
		DispatcherServlet servlet = new DispatcherServlet();
		servlet.setContextClass(XmlWebApplicationContext.class);
		try {
			servlet.init(new MockServletConfig(new MockServletContext(""), "test"));
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
			assertTrue(ex.getCause() instanceof IOException);
			assertTrue(ex.getCause().getMessage().indexOf("/WEB-INF/test-servlet.xml") != -1);
		}
	}

	public void testFrameworkServletWithCustomLocation() throws Exception {
		DispatcherServlet servlet = new DispatcherServlet();
		servlet.setContextConfigLocation("/org/springframework/web/context/WEB-INF/testNamespace.xml /org/springframework/web/context/WEB-INF/context-addition.xml");
		servlet.init(new MockServletConfig(new MockServletContext(""), "test"));
		assertTrue(servlet.getWebApplicationContext().containsBean("kerry"));
		assertTrue(servlet.getWebApplicationContext().containsBean("kerryX"));
	}

	public void testClassPathXmlApplicationContext() throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext("/org/springframework/web/context/WEB-INF/applicationContext.xml");
		assertTrue("Has father", context.containsBean("father"));
		assertTrue("Has rod", context.containsBean("rod"));
		assertFalse("Hasn't kerry", context.containsBean("kerry"));
		assertTrue("Doesn't have spouse", ((TestBean) context.getBean("rod")).getSpouse() == null);
		assertTrue("myinit not evaluated", "Roderick".equals(((TestBean) context.getBean("rod")).getName()));

		context = new ClassPathXmlApplicationContext(new String[] {"/org/springframework/web/context/WEB-INF/applicationContext.xml",
		                                                           "/org/springframework/web/context/WEB-INF/context-addition.xml"});
		assertTrue("Has father", context.containsBean("father"));
		assertTrue("Has rod", context.containsBean("rod"));
		assertTrue("Has kerry", context.containsBean("kerry"));
	}

}
