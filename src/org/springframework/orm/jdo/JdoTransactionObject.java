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

package org.springframework.orm.jdo;

/**
 * JDO transaction object, representing a PersistenceManagerHolder.
 * Used as transaction object by JdoTransactionManager.
 *
 * <p>Instances of this class are the transaction objects that
 * JdoTransactionManager returns. They nest the thread-bound
 * PersistenceManagerHolder internally.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 */
public class JdoTransactionObject {

	private PersistenceManagerHolder persistenceManagerHolder;

	private boolean newPersistenceManagerHolder;

	/**
	 * Create JdoTransactionObject for new PersistenceManagerHolder.
	 */
	protected JdoTransactionObject() {
	}

	/**
	 * Create JdoTransactionObject for existing PersistenceManagerHolder.
	 */
	protected JdoTransactionObject(PersistenceManagerHolder persistenceManagerHolder) {
		this.persistenceManagerHolder = persistenceManagerHolder;
		this.newPersistenceManagerHolder = false;
	}

	/**
	 * Set new PersistenceManagerHolder.
	 */
	protected void setPersistenceManagerHolder(PersistenceManagerHolder persistenceManagerHolder) {
		this.persistenceManagerHolder = persistenceManagerHolder;
		this.newPersistenceManagerHolder = (persistenceManagerHolder != null);
	}

	public PersistenceManagerHolder getPersistenceManagerHolder() {
		return persistenceManagerHolder;
	}

	public boolean isNewPersistenceManagerHolder() {
		return newPersistenceManagerHolder;
	}

	public boolean hasTransaction() {
		return (persistenceManagerHolder != null && persistenceManagerHolder.getPersistenceManager() != null &&
		    persistenceManagerHolder.getPersistenceManager().currentTransaction().isActive());
	}

}
