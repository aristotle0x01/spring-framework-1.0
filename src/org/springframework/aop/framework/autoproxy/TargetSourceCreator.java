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

import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Implementations can create special target sources, such as pooling target
 * sources, for particular beans. For example, they may base their choice
 * on attributes, such as a pooling attribute, on the target class.
 *
 * <p>AbstractAutoProxyCreator can support a number of TargetSourceCreators,
 * which will be applied in order.
 *
 * @author Rod Johnson
 * @version $Id: TargetSourceCreator.java,v 1.4 2004/03/18 02:46:16 trisberg Exp $
 */
public interface TargetSourceCreator {
	
	/**
	 * Create a special TargetSource for the given bean, if any.
	 * @param bean the bean to create a TargetSource for
	 * @param beanName the name of the bean
	 * @param factory the containing factory
	 * @return a special TargetSource or null if this TargetSourceCreator isn't
	 * interested in the particular bean
	 */
	TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory);

}
