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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

/**
 * Very simple implementation of TransactionAttributeSource which will always return
 * the same TransactionAttribute for all methods fed to it. The TransactionAttribute
 * may be specified, but will otherwise default to PROPOGATION_REQUIRED. This may be
 * used in the cases where you want to use the same transaction attribute with all
 * methods being handled by a transaction interceptor.
 * @author Colin Sampaleanu
 * @since 15.10.2003
 * @version $Id: MatchAlwaysTransactionAttributeSource.java,v 1.5 2004/03/18 02:46:05 trisberg Exp $
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
 */
public class MatchAlwaysTransactionAttributeSource implements TransactionAttributeSource {
  
	private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();

	/**
	 * Allows a transaction attribute to be specified, using the String form, for
	 * example, "PROPOGATION_REQUIRED".
	 * @param transactionAttribute The String form of the transactionAttribute to use.
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public void setTransactionAttribute(TransactionAttribute transactionAttribute) {
		this.transactionAttribute = transactionAttribute;
	}

	public TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {
		return transactionAttribute;
	}

}
