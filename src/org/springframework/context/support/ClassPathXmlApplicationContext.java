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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Standalone XML application context, taking the context definition
 * files from the class path. Mainly useful for test harnesses,
 * but also for application contexts embedded within JARs.
 *
 * <p>Treats resource paths as class path resources, when using
 * ApplicationContext.getResource. Only supports full classpath resource
 * names that include the package path, like "mypackage/myresource.dat".
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 * @version $Id: ClassPathXmlApplicationContext.java,v 1.11 2004/03/18 14:53:36 jhoeller Exp $
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML file.
	 * @param configLocation file path
	 */
	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this.configLocations = new String[] {configLocation};
		refresh();
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 */
	public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
		this.configLocations = configLocations;
		refresh();
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent)
			throws BeansException {
		super(parent);
		this.configLocations = configLocations;
		refresh();
	}

	protected String[] getConfigLocations() {
		return this.configLocations;
	}

}
