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

package org.springframework.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.theme.SessionThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;

/**
 * @author Juergen Hoeller
 * @since 21.05.2003
 */
public class ComplexWebApplicationContext extends StaticWebApplicationContext {

	public void refresh() throws BeansException {
		registerSingleton(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME, SessionLocaleResolver.class, null);
		registerSingleton(DispatcherServlet.THEME_RESOLVER_BEAN_NAME, SessionThemeResolver.class, null);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("mappings", "/form.do=localeHandler\n/locale.do=localeHandler\nloc.do=anotherLocaleHandler"));
		registerSingleton("myUrlMapping1", SimpleUrlHandlerMapping.class, pvs);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("mappings", "/form.do=localeHandler\n/unknown.do=unknownHandler"));
		pvs.addPropertyValue(new PropertyValue("order", "2"));
		registerSingleton("myUrlMapping2", SimpleUrlHandlerMapping.class, pvs);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("mappings", "/form.do=formHandler"));
		pvs.addPropertyValue(new PropertyValue("order", "1"));
		registerSingleton("myUrlMapping3", SimpleUrlHandlerMapping.class, pvs);

		registerSingleton("myDummyAdapter", MyDummyAdapter.class, null);
		registerSingleton("myHandlerAdapter", MyHandlerAdapter.class, null);
		registerSingleton("standardHandlerAdapter", SimpleControllerHandlerAdapter.class, null);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("basename", "org.springframework.web.servlet.complexviews"));
		registerSingleton(DispatcherServlet.VIEW_RESOLVER_BEAN_NAME, ResourceBundleViewResolver.class, pvs);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("commandClass", "org.springframework.beans.TestBean"));
		pvs.addPropertyValue(new PropertyValue("formView", "form"));
		registerSingleton("formHandler", SimpleFormController.class, pvs);

		registerSingleton("localeHandler", ComplexLocaleChecker.class, null);
		registerSingleton("anotherLocaleHandler", ComplexLocaleChecker.class, null);
		registerSingleton("unknownHandler", Object.class, null);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("order", "1");
		pvs.addPropertyValue("exceptionMappings", "java.lang.IllegalAccessException=failed2\norg.springframework.web.bind.ServletRequestBindingException=failed3");
		pvs.addPropertyValue("defaultErrorView", "failed0");
		registerSingleton("exceptionResolver1", SimpleMappingExceptionResolver.class, pvs);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("order", "0");
		pvs.addPropertyValue("exceptionMappings", "java.lang.Exception=failed1");
		List mappedHandlers = new ManagedList();
		mappedHandlers.add(new RuntimeBeanReference("anotherLocaleHandler"));
		pvs.addPropertyValue("mappedHandlers", mappedHandlers);
		registerSingleton("exceptionResolver2", SimpleMappingExceptionResolver.class, pvs);

		registerSingleton("multipartResolver", MockMultipartResolver.class, null);

		addMessage("test", Locale.ENGLISH, "test message");
		addMessage("test", Locale.CANADA, "Canadian & test message");

		super.refresh();

		SimpleUrlHandlerMapping myUrlMapping1 = (SimpleUrlHandlerMapping) getBean("myUrlMapping1");
		LocaleChangeInterceptor interceptor1 = new LocaleChangeInterceptor();
		LocaleChangeInterceptor interceptor2 = new LocaleChangeInterceptor();
		interceptor2.setParamName("locale2");
		ThemeChangeInterceptor interceptor3 = new ThemeChangeInterceptor();
		ThemeChangeInterceptor interceptor4 = new ThemeChangeInterceptor();
		interceptor4.setParamName("theme2");
		UserRoleAuthorizationInterceptor interceptor5 = new UserRoleAuthorizationInterceptor();
		interceptor5.setAuthorizedRoles(new String[] {"role1", "role2"});

		List interceptors = new ArrayList();
		interceptors.add(interceptor5);
		interceptors.add(interceptor1);
		interceptors.add(interceptor2);
		interceptors.add(interceptor3);
		interceptors.add(interceptor4);
		interceptors.add(new MyHandlerInterceptor1());
		interceptors.add(new MyHandlerInterceptor2());
		myUrlMapping1.setInterceptors((HandlerInterceptor[]) interceptors.toArray(new HandlerInterceptor[interceptors.size()]));
	}


	public static interface MyHandler {

		public void doSomething(HttpServletRequest request) throws ServletException, IllegalAccessException;

		public long lastModified();
	}


	public static class MyHandlerAdapter extends ApplicationObjectSupport implements HandlerAdapter, Ordered {

		public int getOrder() {
			return 99;
		}

		public boolean supports(Object handler) {
			return handler != null && MyHandler.class.isAssignableFrom(handler.getClass());
		}

		public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object delegate)
		    throws ServletException, IllegalAccessException {
			((MyHandler) delegate).doSomething(request);
			return null;
		}

		public long getLastModified(HttpServletRequest request, Object delegate) {
			return ((MyHandler) delegate).lastModified();
		}
	}


	public static class MyDummyAdapter extends ApplicationObjectSupport implements HandlerAdapter {

		public boolean supports(Object handler) {
			return handler != null && MyHandler.class.isAssignableFrom(handler.getClass());
		}

		public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object delegate)
		    throws IOException, ServletException {
			throw new ServletException("dummy");
		}

		public long getLastModified(HttpServletRequest request, Object delegate) {
			return -1;
		}
	}


	public static class MyHandlerInterceptor1 implements HandlerInterceptor {

		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		    throws ServletException {
			if (request.getAttribute("test2") != null) {
				throw new ServletException("Wrong interceptor order");
			}
			request.setAttribute("test1", "test1");
			request.setAttribute("test1x", "test1x");
			request.setAttribute("test1y", "test1y");
			return true;
		}

		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
													 ModelAndView modelAndView) throws ServletException {
			if (request.getAttribute("test2x") != null) {
				throw new ServletException("Wrong interceptor order");
			}
			if (!"test1x".equals(request.getAttribute("test1x"))) {
				throw new ServletException("Incorrect request attribute");
			}
			request.removeAttribute("test1x");
		}

		public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
																Object handler, Exception ex) throws ServletException {
			if (request.getAttribute("test2y") != null) {
				throw new ServletException("Wrong interceptor order");
			}
			request.removeAttribute("test1y");
		}
	}


	public static class MyHandlerInterceptor2 implements HandlerInterceptor {

		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		    throws ServletException {
			if (request.getAttribute("test1x") == null) {
				throw new ServletException("Wrong interceptor order");
			}
			if (request.getParameter("abort") != null) {
				return false;
			}
			request.setAttribute("test2", "test2");
			request.setAttribute("test2x", "test2x");
			request.setAttribute("test2y", "test2y");
			return true;
		}

		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
													 ModelAndView modelAndView) throws ServletException {
			if (request.getAttribute("test1x") == null) {
				throw new ServletException("Wrong interceptor order");
			}
			if (!"test2x".equals(request.getAttribute("test2x"))) {
				throw new ServletException("Incorrect request attribute");
			}
			request.removeAttribute("test2x");
		}

		public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
																Object handler, Exception ex) throws Exception {
			if (request.getAttribute("test1y") == null) {
				throw new ServletException("Wrong interceptor order");
			}
			request.removeAttribute("test2y");
		}
	}


	public static class ComplexLocaleChecker implements MyHandler {

		public void doSomething(HttpServletRequest request) throws ServletException, IllegalAccessException {
			if (!(request instanceof MultipartHttpServletRequest)) {
				throw new ServletException("Not in a MultipartHttpServletRequest");
			}
			if (!(RequestContextUtils.getWebApplicationContext(request) instanceof ComplexWebApplicationContext)) {
				throw new ServletException("Incorrect WebApplicationContext");
			}
			if (!(RequestContextUtils.getLocaleResolver(request) instanceof SessionLocaleResolver)) {
				throw new ServletException("Incorrect LocaleResolver");
			}
			if (!Locale.CANADA.equals(RequestContextUtils.getLocale(request))) {
				throw new ServletException("Incorrect Locale");
			}
			if (!(RequestContextUtils.getThemeResolver(request) instanceof SessionThemeResolver)) {
				throw new ServletException("Incorrect ThemeResolver");
			}
			if (!"theme".equals(RequestContextUtils.getThemeResolver(request).resolveThemeName(request))) {
				throw new ServletException("Incorrect theme name");
			}
			if (request.getParameter("fail") != null) {
				throw new ModelAndViewDefiningException(new ModelAndView("failed1"));
			}
			if (request.getParameter("access") != null) {
				throw new IllegalAccessException("illegal access");
			}
			if (request.getParameter("servlet") != null) {
				throw new ServletRequestBindingException("servlet");
			}
			if (request.getParameter("exception") != null) {
				throw new RuntimeException("servlet");
			}
		}

		public long lastModified() {
			return 99;
		}
	}


	public static class MockMultipartResolver implements MultipartResolver {

		private boolean resolved;
		public boolean cleaned;

		public boolean isMultipart(HttpServletRequest request) {
			return true;
		}

		public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
			if (request instanceof MultipartHttpServletRequest) {
				throw new IllegalStateException("Already a multipart request");
			}
			if (this.resolved) {
				throw new IllegalStateException("Already resolved");
			}
			this.resolved = true;
			return new AbstractMultipartHttpServletRequest(request) {
			};
		}

		public void cleanupMultipart(MultipartHttpServletRequest request) {
			if (this.cleaned) {
				throw new IllegalStateException("Already cleaned");
			}
			this.cleaned = true;
		}
	}

}
