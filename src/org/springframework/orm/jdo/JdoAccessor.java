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

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

/**
 * Base class for JdoTemplate and JdoInterceptor, defining common
 * properties like flushing behavior.
 *
 * <p>Note: With JDO, modifications to persistent objects are just possible
 * within a transaction (in contrast to Hibernate). Therefore, eager flushing
 * will just get applied when in a transaction. Furthermore, there is explicit
 * notion of flushing never, as this would not imply a performance gain due to
 * JDO's field interception mechanism that doesn't involve snapshot comparison.
 *
 * <p>Eager flushing is just available for specific JDO implementations.
 * You need to a corresponding JdoDialect to make eager flushing work.
 *
 * <p>Not intended to be used directly. See JdoTemplate and JdoInterceptor.
 *
 * @author Juergen Hoeller
 * @since 02.11.2003
 * @see JdoTemplate
 * @see JdoInterceptor
 * @see #setFlushEager
 */
public class JdoAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private PersistenceManagerFactory persistenceManagerFactory;

	private JdoDialect jdoDialect;

	private boolean flushEager = false;

	/**
	 * Set the JDO PersistenceManagerFactory that should be used to create
	 * PersistenceManagers.
	 */
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
	}

	/**
	 * Return the JDO PersistenceManagerFactory that should be used to create
	 * PersistenceManagers.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return persistenceManagerFactory;
	}

	/**
	 * Set the JDO dialect to use for this accessor.
	 * <p>The dialect object can be used to retrieve the underlying JDBC
	 * connection or to eagerly flush changes to the database.
	 */
	public void setJdoDialect(JdoDialect jdoDialect) {
		this.jdoDialect = jdoDialect;
	}

	/**
	 * Return the JDO dialect to use for this accessor.
	 */
	public JdoDialect getJdoDialect() {
		return jdoDialect;
	}

	/**
	 * Set if this accessor should flush changes to the database eagerly.
	 * <p>Eager flushing leads to immediate synchronization with the database,
	 * even if in a transaction. This causes inconsistencies to show up and throw
	 * a respective exception immediately, and JDBC access code that participates
	 * in the same transaction will see the changes as the database is already
	 * aware of them then. But the drawbacks are:
	 * <ul>
	 * <li>additional communication roundtrips with the database, instead of a
	 * single batch at transaction commit;
	 * <li>the fact that an actual database rollback is needed if the Hibernate
	 * transaction rolls back (due to already submitted SQL statements).
	 * </ul>
	 */
	public void setFlushEager(boolean flushEager) {
		this.flushEager = flushEager;
	}

	/**
	 * Return if this accessor should flush changes to the database eagerly.
	 */
	public boolean isFlushEager() {
		return flushEager;
	}

	public void afterPropertiesSet() {
		if (this.persistenceManagerFactory == null) {
			throw new IllegalArgumentException("persistenceManagerFactory is required");
		}
		if (this.flushEager && this.jdoDialect == null) {
			throw new IllegalArgumentException("Cannot flush eagerly without a jdoDialect setting");
		}
	}

	/**
	 * Flush the given JDO persistence manager if necessary.
	 * @param pm the current JDO PersistenceManage
	 * @param existingTransaction if executing within an existing transaction
	 * @throws JDOException in case of JDO flushing errors
	 */
	public void flushIfNecessary(PersistenceManager pm, boolean existingTransaction) throws JDOException {
		if (this.flushEager && this.jdoDialect != null) {
			logger.debug("Eagerly flushing JDO persistence manager");
			this.jdoDialect.flush(pm);
		}
	}

	/**
	 * Convert the given JDOException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Delegates to the JdoDialect if set, falls
	 * back to PersistenceManagerFactoryUtils' standard exception translation else.
	 * May be overridden in subclasses.
	 * @param ex JDOException that occured
	 * @return the corresponding DataAccessException instance
	 * @see JdoDialect#translateException
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 */
	public DataAccessException convertJdoAccessException(JDOException ex) {
		if (getJdoDialect() != null) {
			return getJdoDialect().translateException(ex);
		}
		else {
			return PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
		}
	}

}
