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

package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.framework.autoproxy.target.AbstractPrototypeTargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Overrides generic PrototypeTargetSourceCreator to create a prototype only for beans
 * with names beginning with "prototype"
 * @author Rod Johnson
 * @version $Id: SelectivePrototypeTargetSourceCreator.java,v 1.3 2004/03/18 03:01:14 trisberg Exp $
 */
public class SelectivePrototypeTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	/**
	 * @see org.springframework.aop.framework.autoproxy.target.AbstractPrototypeTargetSourceCreator#createPrototypeTargetSource(java.lang.Object, java.lang.String, org.springframework.beans.factory.BeanFactory)
	 */
	protected AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		if (!beanName.startsWith("prototype"))
			return null;
		return new PrototypeTargetSource();
	}
}
