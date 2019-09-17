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

package org.springframework.web.servlet.view.freemarker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

/**
 * @author Juergen Hoeller
 * @since 14.03.2004
 */
public class FreeMarkerConfigurerTests extends TestCase {

	public void testFreemarkerConfigurationFactoryBeanWithConfigLocation() throws TemplateException {
		FreeMarkerConfigurationFactoryBean fcfb = new FreeMarkerConfigurationFactoryBean();
		fcfb.setConfigLocation(new FileSystemResource("myprops.properties"));
		Properties props = new Properties();
		props.setProperty("myprop", "/mydir");
		fcfb.setFreemarkerSettings(props);
		try {
			fcfb.afterPropertiesSet();
			fail("Should have thrown IOException");
		}
		catch (IOException ex) {
			// expected
		}
	}

	public void testVelocityEngineFactoryBeanWithResourceLoaderPath() throws IOException, TemplateException {
		final File[] files = new File[1];
		FreeMarkerConfigurationFactoryBean fcfb = new FreeMarkerConfigurationFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public void setDirectoryForTemplateLoading(File file) throws IOException {
						files[0] = file;
					}
				};
			}
		};
		fcfb.setTemplateLoaderPath("file:/mydir");
		fcfb.afterPropertiesSet();
		assertTrue(fcfb.getObject() instanceof Configuration);
		assertEquals(new File("/mydir").getPath(), files[0].getPath());
	}

	public void testFreemarkerConfigurationFactoryBeanWithNonFileResourceLoaderPath()
			throws IOException, TemplateException {
		FreeMarkerConfigurationFactoryBean fcfb = new FreeMarkerConfigurationFactoryBean();
		fcfb.setTemplateLoaderPath("file:/mydir");
		Properties settings = new Properties();
		settings.setProperty("localized_lookup", "false");
		fcfb.setFreemarkerSettings(settings);
		fcfb.setResourceLoader(new ResourceLoader() {
			public Resource getResource(String location) {
				if (!("file:/mydir".equals(location) || "file:/mydir/test".equals(location))) {
					throw new IllegalArgumentException(location);
				}
				return new InputStreamResource(new ByteArrayInputStream("test".getBytes()), "test");
			}
		});
		fcfb.afterPropertiesSet();
		assertTrue(fcfb.getObject() instanceof Configuration);
		Configuration fc = (Configuration) fcfb.getObject();
		Template ft = fc.getTemplate("test");
		assertEquals("test", FreeMarkerTemplateUtils.processTemplateIntoString(ft, new HashMap()));
	}

	public void testFreemarkerConfigurer() throws IOException, TemplateException {
		final File[] files = new File[1];
		FreeMarkerConfigurer fc = new FreeMarkerConfigurer() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public void setDirectoryForTemplateLoading(File file) throws IOException {
						files[0] = file;
					}
				};
			}
		};
		fc.setTemplateLoaderPath("file:/mydir");
		fc.afterPropertiesSet();
		assertTrue(fc.getConfiguration() instanceof Configuration);
		assertEquals(new File("/mydir").getPath(), files[0].getPath());
	}

}
