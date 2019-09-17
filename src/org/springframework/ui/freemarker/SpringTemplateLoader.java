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

package org.springframework.ui.freemarker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import freemarker.cache.TemplateLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * FreeMarker TemplateLoader adapter that loads via a Spring ResourceLoader.
 * Used by FreeMarkerConfigurationFactory for any resource loader path that
 * cannot be resolved to a java.io.File.
 *
 * <p>Note that this loader does not allow for modification detection:
 * Use FreeMarker's default TemplateLoader for java.io.File resources.
 *
 * @author Juergen Hoeller
 * @since 14.03.2004
 * @see FreeMarkerConfigurationFactory#setTemplateLoaderPath
 * @see freemarker.template.Configuration#setDirectoryForTemplateLoading
 */
public class SpringTemplateLoader implements TemplateLoader {

	protected final Log logger = LogFactory.getLog(getClass());

	private final ResourceLoader resourceLoader;

	private final String templateLoaderPath;

	/**
	 * Create a new SpringTemplateLoader.
	 * @param resourceLoader the Spring ResourceLoader to use
	 * @param templateLoaderPath the template loader path to use
	 */
	public SpringTemplateLoader(ResourceLoader resourceLoader, String templateLoaderPath) {
		this.resourceLoader = resourceLoader;
		if (!templateLoaderPath.endsWith("/")) {
			templateLoaderPath += "/";
		}
		this.templateLoaderPath = templateLoaderPath;
		logger.info("SpringTemplateLoader for FreeMarker: using resource loader [" + this.resourceLoader +
								"] and template loader path [" + this.templateLoaderPath + "]");
	}

	public Object findTemplateSource(String name) throws IOException {
		Resource resource = this.resourceLoader.getResource(this.templateLoaderPath + name);
		return (resource.exists() ? resource : null);
	}

	public long getLastModified(Object templateSource) {
		return -1;
	}

	public Reader getReader(Object templateSource, String encoding) throws IOException {
		Resource resource = (Resource) templateSource;
		return new InputStreamReader(resource.getInputStream(), encoding);
	}

	public void closeTemplateSource(Object templateSource) throws IOException {
	}

}
