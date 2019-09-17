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

package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Simple factory for shared List instances. Allows for central setup
 * of Lists via the "list" element in XML bean definitions.
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class ListFactoryBean implements FactoryBean, InitializingBean {

	private List sourceList;

	private Class targetListClass = ArrayList.class;

	private List targetList;

	private boolean singleton = true;

	/**
	 * Set the source List, typically populated via XML "list" elements.
	 */
	public void setSourceList(List sourceList) {
		this.sourceList = sourceList;
	}

	/**
	 * Set the class to use for the target List.
	 * Default is <code>java.util.ArrayList</code>.
	 * @see java.util.ArrayList
	 */
	public void setTargetListClass(Class targetListClass) {
		if (targetListClass == null) {
			throw new IllegalArgumentException("targetListClass must not be null");
		}
		if (!List.class.isAssignableFrom(targetListClass)) {
			throw new IllegalArgumentException("targetListClass must implement java.util.List");
		}
		this.targetListClass = targetListClass;
	}

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void afterPropertiesSet() {
		if (this.sourceList == null) {
			throw new IllegalArgumentException("sourceList is required");
		}
		if (this.singleton) {
			this.targetList = (List) BeanUtils.instantiateClass(this.targetListClass);
			this.targetList.addAll(this.sourceList);
		}
	}

	public Object getObject() {
		if (this.singleton) {
			return this.targetList;
		}
		else {
			List result = (List) BeanUtils.instantiateClass(this.targetListClass);
			result.addAll(this.sourceList);
			return result;
		}
	}

	public Class getObjectType() {
		return List.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
