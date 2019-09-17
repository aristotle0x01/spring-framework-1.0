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

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Convenient superclass for TargetSource creators that create pooling TargetSources.
 * @author Rod Johnson
 * @version $Id: AbstractPoolingTargetSourceCreator.java,v 1.4 2004/03/18 02:46:16 trisberg Exp $
 */
public abstract class AbstractPoolingTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	protected final AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		PoolingAttribute poolingAttribute = getPoolingAttribute(bean, beanName, factory);
		if (poolingAttribute == null) {
			// No pooling attribute
			return null;
		}
		else {
			AbstractPoolingTargetSource poolingTargetSource = newPoolingTargetSource(poolingAttribute);
			return poolingTargetSource;
		}
	}
	
	/**
	 * Create a new AbstractPoolingTargetSource. This implementation creates
	 * a CommonsPoolTargetSource, but subclasses may wish to override that
	 * behaviour. Don't need to set bean name or call setBeanFactory.
	 */
	protected AbstractPoolingTargetSource newPoolingTargetSource(PoolingAttribute poolingAttribute) {
		AbstractPoolingTargetSource poolingTargetSource = new CommonsPoolTargetSource();
		poolingTargetSource.setMaxSize(poolingAttribute.getSize());
		return poolingTargetSource;
	}

	/**
	 * Create a PoolingAttribute for the given bean, if any.
	 * @param bean the bean to create a PoolingAttribute for
	 * @param beanName the name of the bean
	 * @param beanFactory the current bean factory
	 * @return the PoolingAttribute, or null for no pooling
	 */
	protected abstract PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory beanFactory);

}