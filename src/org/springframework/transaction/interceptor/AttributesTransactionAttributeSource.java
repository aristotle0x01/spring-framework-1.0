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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.support.AopUtils;
import org.springframework.metadata.Attributes;

/**
 * Implementation of TransactionAttributeSource that uses
 * attributes from an Attributes implementation.
 * Defaults to using class's transaction attribute if none is
 * associated with the target method.
 * Any transaction attribute associated with the target method completely
 * overrides a class transaction attribute.
 * <br>
 * This implementation caches attributes by method after they are first used.
 * If it's ever desirable to allow dynamic changing of transaction attributes
 * (unlikely) caching could be made configurable. Caching is desirable because
 * of the cost of evaluating rollback rules.
 * @author Rod Johnson
 * @see org.springframework.metadata.Attributes
 * @version $Id: AttributesTransactionAttributeSource.java,v 1.6 2004/03/18 02:46:05 trisberg Exp $
 */
public class AttributesTransactionAttributeSource implements TransactionAttributeSource {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	/**
	 * Canonical value held in cache to indicate no transaction attribute was
	 * found for this method, and we don't need to look again
	 */
	private final static Object NULL_TX_ATTRIBUTE = new Object();
	
	/**
	 * Underlying Attributes implementation we're using
	 */
	private final Attributes attributes;
	
	/**
	 * Cache of TransactionAttributes, keyed by Method and target class
	 */
	private HashMap cache = new HashMap();

	public AttributesTransactionAttributeSource(Attributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * Return the transaction attribute for this method invocation.
	 * Defaults to the class's transaction attribute if no method
	 * attribute is found
	 * @param method method for the current invocation. Can't be null
	 * @param targetClass target class for this invocation. May be null.
	 * @return TransactionAttribute for this method, or null if the method is non-transactional
	 */
	public TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {
		// First, see if we have a cached value
		Object cacheKey = cacheKey(method, targetClass);
		Object cached = cache.get(cacheKey);
		if (cached != null) {
			// Value will either be canonical value indicating there is no transaction attribute,
			// or an actual transaction attribute
			if (cached == NULL_TX_ATTRIBUTE) {
				return null;
			}
			else {
				return (TransactionAttribute) cached;
			}
		}
		else {
			// We need to work it out
			TransactionAttribute txAtt = computeTransactionAttribute(method, targetClass);
			// Put it in the cache
			if (txAtt == null) {
				cache.put(cacheKey, NULL_TX_ATTRIBUTE);
			}
			else {
				cache.put(cacheKey, txAtt);
			}
			return txAtt;
		}
	}
	
	private Object cacheKey(Method method, Class targetClass) {
		// Class may be null, method can't
		return targetClass + "" + System.identityHashCode(method);
	}
	
	/**
	 * Same return as getTransactionAttribute method, but doesn't cache the result.
	 * getTransactionAttribute is a caching decorator for this method.
	 */
	protected TransactionAttribute computeTransactionAttribute(Method method, Class targetClass) {
		// The method may be on an interface, but we need attributes from the target class.
		// The AopUtils class provides a convenience method for this. If the target class
		// is null, the method will be unchanged.
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		
		// First try is the method in the target class
		TransactionAttribute txAtt = findTransactionAttribute(attributes.getAttributes(specificMethod));
		if (txAtt != null)
			return txAtt;
		
		// Second try is the transaction attribute on the target class
		txAtt = findTransactionAttribute(attributes.getAttributes(specificMethod.getDeclaringClass()));
		if (txAtt != null)
			return txAtt;
		
		if (specificMethod != method ) {
			// Fallback is to look at the original method
			txAtt = findTransactionAttribute(attributes.getAttributes(method));
			if (txAtt != null)
				return txAtt;
			// Last fallback is the class of the original method
			return findTransactionAttribute(attributes.getAttributes(method.getDeclaringClass()));
		}
		return null;
	}


	/**
	 * Return the transaction attribute, given this set of attributes
	 * attached to a method or class.
	 * Protected rather than private as subclasses may want to customize
	 * how this is done: for example, returning a TransactionAttribute
	 * affected by the values of other attributes.
	 * This implementation takes into account RollbackRuleAttributes, if
	 * the TransactionAttribute is a RuleBasedTransactionAttribute.
	 * Return null if it's not transactional. 
	 * @param atts attributes attached to a method or class. May
	 * be null, in which case a null TransactionAttribute will be returned.
	 * @return TransactionAttribute configured transaction attribute, or null
	 * if none was found
	 */
	protected TransactionAttribute findTransactionAttribute(Collection atts) {
		if (atts == null)
			return null;
		TransactionAttribute txAttribute = null;
		// Check there is a transaction attribute
		for (Iterator itr = atts.iterator(); itr.hasNext() && txAttribute == null; ) {
			Object att = itr.next();
			if (att instanceof TransactionAttribute) {
				txAttribute = (TransactionAttribute) att;
			}
		}

		if (txAttribute instanceof RuleBasedTransactionAttribute) {
			RuleBasedTransactionAttribute rbta = (RuleBasedTransactionAttribute) txAttribute;
			// We really want value: bit of a hack
			List l = new LinkedList();
			for (Iterator itr = atts.iterator(); itr.hasNext(); ) {
				Object att = itr.next();
				if (att instanceof RollbackRuleAttribute) {
					if (logger.isDebugEnabled())
						logger.debug("Found RollbackRule " + att);
					l.add(att);
				}
			}
			// Repeatedly setting this isn't elegant, but it works
			rbta.setRollbackRules(l);
		}
		
		return txAttribute;
	}

}
