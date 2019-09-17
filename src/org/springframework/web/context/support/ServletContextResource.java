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

package org.springframework.web.context.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.springframework.core.io.AbstractResource;

/**
 * Resource implementation for ServletContext resources,
 * interpreting relative paths within the web application root.
 *
 * <p>Always supports stream access, but only allows java.io.File
 * access when the web application archive is expanded.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see javax.servlet.ServletContext#getResourceAsStream
 * @see javax.servlet.ServletContext#getRealPath
 */
public class ServletContextResource extends AbstractResource {

	private final ServletContext servletContext;

	private final String path;

	/**
	 * Create a new ServletContextResource.
	 * @param servletContext the ServletContext to load from
	 * @param path the path of the resource
	 */
	public ServletContextResource(ServletContext servletContext, String path) {
		this.servletContext = servletContext;
		this.path = path;
	}

	/**
	 * This implementation delegates to ServletContext.getResourceAsStream,
	 * but throws a FileNotFoundException if not found.
	 * @see javax.servlet.ServletContext#getResourceAsStream
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = this.servletContext.getResourceAsStream(this.path);
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	public URL getURL() throws IOException {
		URL url = this.servletContext.getResource(this.path);
		if (url == null) {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to URL " +
																			"because it does not exist");
		}
		return url;
	}

	/**
	 * This implementation delegates to ServletContext.getRealPath,
	 * but throws a FileNotFoundException if not found or not resolvable.
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public File getFile() throws IOException {
		String realPath = this.servletContext.getRealPath(this.path);
		if (realPath == null) {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path - " +
																			"web application archive not expanded?");
		}
		return new File(realPath);
	}

	public String getDescription() {
		return "resource [" + this.path + "] of ServletContext";
	}

}
