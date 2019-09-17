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

import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * @author Juergen Hoeller
 * @since 20.03.2003
 */
public class LocaleResolverTestSuite extends TestCase {

	public LocaleResolverTestSuite(String name) {
		super(name);
	}

	private void internalTest(LocaleResolver localeResolver, boolean shouldSet) {
		// create mocks
		MockServletContext context = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(context, "GET", "/test");
		request.addPreferredLocale(Locale.UK);
		MockHttpServletResponse response = new MockHttpServletResponse();
		// check original locale
		Locale locale = localeResolver.resolveLocale(request);
		assertEquals(locale, Locale.UK);
		// set new locale
		try {
			localeResolver.setLocale(request, response, Locale.GERMANY);
			if (!shouldSet)
				fail("should not be able to set Locale");
			// check new locale
			locale = localeResolver.resolveLocale(request);
			assertEquals(locale, Locale.GERMANY);
		} catch (IllegalArgumentException ex) {
			if (shouldSet)
				fail("should be able to set Locale");
		}
	}

	public void testAcceptHeaderLocaleResolver() {
		internalTest(new AcceptHeaderLocaleResolver(), false);
	}

	public void testCookieLocaleResolver() {
		internalTest(new CookieLocaleResolver(), true);
	}

	public void testSessionLocaleResolver() {
		internalTest(new SessionLocaleResolver(), true);
	}
}
