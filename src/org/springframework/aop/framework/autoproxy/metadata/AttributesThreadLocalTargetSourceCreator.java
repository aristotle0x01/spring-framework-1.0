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

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;

import org.springframework.aop.framework.autoproxy.target.AbstractPrototypeTargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.metadata.Attributes;

/**
 * PrototypeTargetSourceCreator driven by metadata.
 * Creates a ThreadLocalTargetSource
 * only if there's a ThreadLocalAttribute associated with the class.
 * @author Rod Johnson
 * @version $Id: AttributesThreadLocalTargetSourceCreator.java,v 1.2 2004/03/18 02:46:08 trisberg Exp $
 */
public class AttributesThreadLocalTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	private final Attributes attributes;

	public AttributesThreadLocalTargetSourceCreator(Attributes attributes) {
		this.attributes = attributes;
	}

	protected AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory bf) {
		Class beanClass = bean.getClass();
		// See if there's a pooling attribute
		Collection atts = attributes.getAttributes(beanClass, ThreadLocalAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute: don't create a custom TargetSource
			return null;
		}
		else {
			return new ThreadLocalTargetSource();
		}
	}

}