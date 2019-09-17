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

package org.springframework.web.servlet.mvc;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rod Johnson
 */
public class CommandControllerTestSuite extends TestCase {

	public void testNoArgsNoErrors() throws Exception {
		TestController mc = new TestController();
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(request.getServletPath()));
		TestBean person = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("command and errors non null", person != null && errors != null);
		assertTrue("no errors", !errors.hasErrors());
		assertTrue("Correct caching", response.getHeader("Cache-Control") == null);
		assertTrue("Correct expires header", response.getHeader("Expires") == null);
	}

	public void test2ArgsNoErrors() throws Exception {
		TestController mc = new TestController();
		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		String name = "Rod";
		int age = 32;
		request.addParameter("name", name);
		request.addParameter("age", "" + age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(request.getServletPath()));
		TestBean person = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("command and errors non null", person != null && errors != null);
		assertTrue("no errors", !errors.hasErrors());
		assertTrue("command name bound ok", person.getName().equals(name));
		assertTrue("command age bound ok", person.getAge() == age);
	}
	
	public void test2Args1Mismatch() throws Exception {
		TestController mc = new TestController();
		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		String name = "Rod";
		String age = "32x";
		request.addParameter("name", name);
		request.addParameter("age", age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(request.getServletPath()));
		TestBean person = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("command and errors non null", person != null && errors != null);
		assertTrue("has 1 errors", errors.getErrorCount() == 1);
		assertTrue("command name bound ok", person.getName().equals(name));
		assertTrue("command age default", person.getAge() == new TestBean().getAge());
		assertTrue("has error on field age", errors.hasFieldErrors("age"));
		FieldError fe = errors.getFieldError("age");
		assertTrue("Saved invalid value", fe.getRejectedValue().equals(age));
		assertTrue("Correct field", fe.getField().equals("age"));
	}

	public void testSupportedMethods() throws Exception {
		TestController mc = new TestController();
		mc.setSupportedMethods(new String[] {"POST"});
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		HttpServletResponse response = new MockHttpServletResponse();
		try {
			mc.handleRequest(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}
	}

	public void testRequireSessionWithoutSession() throws Exception {
		TestController mc = new TestController();
		mc.setRequireSession(true);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		HttpServletResponse response = new MockHttpServletResponse();
		try {
			mc.handleRequest(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}
	}

	public void testRequireSessionWithSession() throws Exception {
		TestController mc = new TestController();
		mc.setRequireSession(true);
		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		request.setSession(new MockHttpSession());
		HttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
	}

	public void testNoCaching() throws Exception {
		TestController mc = new TestController();
		mc.setCacheSeconds(0);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires").equals("" + 1L));
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("no-cache"));
	}

	public void testNoCachingWithoutExpires() throws Exception {
		TestController mc = new TestController();
		mc.setCacheSeconds(0);
		mc.setUseExpiresHeader(false);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("No expires header", response.getHeader("Expires") == null);
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("no-cache"));
	}

	public void testNoCachingWithoutCacheControl() throws Exception {
		TestController mc = new TestController();
		mc.setCacheSeconds(0);
		mc.setUseCacheControlHeader(false);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires").equals("" + 1L));
		assertTrue("No cache control", response.getHeader("Cache-Control") == null);
	}

	public void testCaching() throws Exception {
		TestController mc = new TestController();
		mc.setCacheSeconds(10);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires") != null);
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("max-age=10"));
	}

	public void testCachingWithoutExpires() throws Exception {
		TestController mc = new TestController();
		mc.setCacheSeconds(10);
		mc.setUseExpiresHeader(false);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("No expires header", response.getHeader("Expires") == null);
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("max-age=10"));
	}

	public void testCachingWithoutCacheControl() throws Exception {
		TestController mc = new TestController();
		mc.setCacheSeconds(10);
		mc.setUseCacheControlHeader(false);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires") != null);
		assertTrue("No cache control", response.getHeader("Cache-Control") == null);
	}

	public void testCachingWithLastModified() throws Exception {
		class LastModifiedTestController extends TestController implements LastModified {
			public long getLastModified(HttpServletRequest request) {
				return 0;
			}
		}
		LastModifiedTestController mc = new LastModifiedTestController();
		mc.setCacheSeconds(10);
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires") != null);
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("max-age=10, must-revalidate"));
	}

	public void testCachingWithCustomCacheForSecondsCall() throws Exception {
		TestController mc = new TestController() {
			protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) {
				cacheForSeconds(response, 5);
				return super.handle(request, response, command, errors);
			}
		};
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires") != null);
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("max-age=5"));
	}

	public void testCachingWithCustomApplyCacheSecondsCall1() throws Exception {
		TestController mc = new TestController() {
			protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) {
				applyCacheSeconds(response, 5);
				return super.handle(request, response, command, errors);
			}
		};
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires") != null);
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("max-age=5"));
	}

	public void testCachingWithCustomApplyCacheSecondsCall2() throws Exception {
		TestController mc = new TestController() {
			protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) {
				applyCacheSeconds(response, 0);
				return super.handle(request, response, command, errors);
			}
		};
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("Correct expires header", response.getHeader("Expires").equals("" + 1L));
		assertTrue("Correct cache control", response.getHeader("Cache-Control").equals("no-cache"));
	}

	public void testCachingWithCustomApplyCacheSecondsCall3() throws Exception {
		TestController mc = new TestController() {
			protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) {
				applyCacheSeconds(response, -1);
				return super.handle(request, response, command, errors);
			}
		};
		HttpServletRequest request = new MockHttpServletRequest(null, "GET", "/ok.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		mc.handleRequest(request, response);
		assertTrue("No expires header", response.getHeader("Expires") == null);
		assertTrue("No cache control", response.getHeader("Cache-Control") == null);
	}

	public void testCustomDateEditorWithAllowEmpty() throws Exception {
		final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
		TestController mc = new TestController() {
			protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
				binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
			}
		};

		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("date", "1.5.2003");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		TestBean tb = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("No field error", !errors.hasFieldErrors("date"));
		assertTrue("Correct date property", df.parse("1.5.2003").equals(tb.getDate()));
		assertTrue("Correct date value", "01.05.2003".equals(errors.getFieldValue("date")));

		request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("date", "");
		response = new MockHttpServletResponse();
		mv = mc.handleRequest(request, response);
		tb = (TestBean) mv.getModel().get("command");
		errors = (Errors) mv.getModel().get("errors");
		assertTrue("No field error", !errors.hasFieldErrors("date"));
		assertTrue("Correct date property", tb.getDate() == null);
		assertTrue("Correct date value", errors.getFieldValue("date") == null);
	}

	public void testCustomDateEditorWithoutAllowEmpty() throws Exception {
		final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
		TestController mc = new TestController() {
			protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
				binder.registerCustomEditor(Date.class, new CustomDateEditor(df, false));
			}
		};

		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("date", "1.5.2003");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		TestBean tb = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("No field error", !errors.hasFieldErrors("date"));
		assertTrue("Correct date property", df.parse("1.5.2003").equals(tb.getDate()));
		assertTrue("Correct date value", "01.05.2003".equals(errors.getFieldValue("date")));

		request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("date", "");
		response = new MockHttpServletResponse();
		mv = mc.handleRequest(request, response);
		tb = (TestBean) mv.getModel().get("command");
		errors = (Errors) mv.getModel().get("errors");
		assertTrue("Has field error", errors.hasFieldErrors("date"));
		assertTrue("Correct date property", tb.getDate() != null);
		assertTrue("Correct date value", errors.getFieldValue("date") != null);
	}

	public void testCustomNumberEditorWithAllowEmpty() throws Exception {
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);

		TestController mc = new TestController() {
			protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
				binder.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, nf, true));
			}
		};

		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("myFloat", "5,1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		TestBean tb = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("No field error", !errors.hasFieldErrors("myFloat"));
		assertTrue("Correct float property", (new Float(5.1)).equals(tb.getMyFloat()));
		assertTrue("Correct float value", "5,1".equals(errors.getFieldValue("myFloat")));

		request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("myFloat", "");
		response = new MockHttpServletResponse();
		mv = mc.handleRequest(request, response);
		tb = (TestBean) mv.getModel().get("command");
		errors = (Errors) mv.getModel().get("errors");
		assertTrue("No field error", !errors.hasFieldErrors("myFloat"));
		assertTrue("Correct float property", tb.getMyFloat() == null);
		assertTrue("Correct float value", errors.getFieldValue("myFloat") == null);
	}

	public void testCustomNumberEditorWithoutAllowEmpty() throws Exception {
		final NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);

		TestController mc = new TestController() {
			protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
				binder.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, nf, false));
			}
		};

		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("myFloat", "5,1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		TestBean tb = (TestBean) mv.getModel().get("command");
		Errors errors = (Errors) mv.getModel().get("errors");
		assertTrue("No field error", !errors.hasFieldErrors("myFloat"));
		assertTrue("Correct float property", (new Float(5.1)).equals(tb.getMyFloat()));
		assertTrue("Correct float value", "5,1".equals(errors.getFieldValue("myFloat")));

		request = new MockHttpServletRequest(null, "GET", "/welcome.html");
		request.addParameter("myFloat", "");
		response = new MockHttpServletResponse();
		mv = mc.handleRequest(request, response);
		tb = (TestBean) mv.getModel().get("command");
		errors = (Errors) mv.getModel().get("errors");
		assertTrue("Has field error", errors.hasFieldErrors("myFloat"));
		assertTrue("Correct float property", tb.getMyFloat() != null);
		assertTrue("Correct float value", errors.getFieldValue("myFloat") != null);
	}


	private static class TestController extends AbstractCommandController {
		
		private TestController() {
			super(TestBean.class, "person");
		}
		
		protected ModelAndView handle(HttpServletRequest request,	HttpServletResponse response,	Object command,	BindException errors) {
			Map m = new HashMap();
			assertTrue("Command not null", command != null);
			assertTrue("errors not null", errors != null);
			m.put("errors", errors);
			m.put("command", command);
			return new ModelAndView(request.getServletPath(), m);
		}
	}

}
