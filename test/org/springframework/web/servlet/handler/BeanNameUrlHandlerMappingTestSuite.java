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

package org.springframework.web.servlet.handler;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class BeanNameUrlHandlerMappingTestSuite extends TestCase {

	public static final String CONF = "/org/springframework/web/servlet/handler/map1.xml";
	
	private ConfigurableWebApplicationContext wac;

	public void setUp() throws Exception {
		MockServletContext sc = new MockServletContext("");
		wac = new XmlWebApplicationContext();
		wac.setServletContext(sc);
		wac.setConfigLocations(new String[] {CONF});
		wac.refresh();
	}

	public void testRequestsWithoutHandlers() throws Exception {
		HandlerMapping hm = (HandlerMapping) wac.getBean("handlerMapping");

		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/mypath/nonsense.html");
		req.setContextPath("/myapp");
		Object h = hm.getHandler(req);
		assertTrue("Handler is null", h == null);

		req = new MockHttpServletRequest(null, "GET", "/foo/bar/baz.html");
		h = hm.getHandler(req);
		assertTrue("Handler is null", h == null);
	}

	public void testRequestsWithSubPaths() throws Exception {
		HandlerMapping hm = (HandlerMapping) wac.getBean("handlerMapping");
		Object bean = wac.getBean("godCtrl");

		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/mypath/welcome.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/myapp/mypath/welcome.html");
		req.setContextPath("/myapp");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
		
		req = new MockHttpServletRequest(null, "GET", "/myapp/mypath/welcome.html");
		req.setContextPath("/myapp");
		req.setServletPath("/mypath/welcome.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/myapp/myservlet/mypath/welcome.html");
		req.setContextPath("/myapp");
		req.setServletPath("/myservlet");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/myapp/myapp/mypath/welcome.html");
		req.setContextPath("/myapp");
		req.setServletPath("/myapp");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/mypath/show.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/mypath/bookseats.html");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
	}
	
	public void testRequestsWithFullPaths() throws Exception {
		BeanNameUrlHandlerMapping hm = new BeanNameUrlHandlerMapping();
		hm.setAlwaysUseFullPath(true);
		hm.setApplicationContext(wac);
		Object bean = wac.getBean("godCtrl");

		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/mypath/welcome.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/myapp/mypath/welcome.html");
		req.setContextPath("/myapp");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/mypath/welcome.html");
		req.setContextPath("");
		req.setServletPath("/mypath");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/myapp/mypath/welcome.html");
		req.setContextPath("/myapp");
		req.setServletPath("/mypath");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);
	}

	public void testAsteriskMatches() throws Exception {
		HandlerMapping hm = (HandlerMapping) wac.getBean("handlerMapping");
		Object bean = wac.getBean("godCtrl");

		MockHttpServletRequest req = new MockHttpServletRequest(null, "GET", "/mypath/test.html");
		HandlerExecutionChain hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/mypath/testarossa");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec != null && hec.getHandler() == bean);

		req = new MockHttpServletRequest(null, "GET", "/mypath/tes");
		hec = hm.getHandler(req);
		assertTrue("Handler is correct bean", hec == null);
	}

	public void testDoubleMappings() throws ServletException {
		BeanNameUrlHandlerMapping hm = (BeanNameUrlHandlerMapping) wac.getBean("handlerMapping");
		try {
			hm.registerHandler("/mypath/welcome.html", "handler");
			fail("Should have thrown ApplicationContextException");
		}
		catch (ApplicationContextException ex) {
			// expected
		}
	}

}
