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

/**
 * Callback interface for JDO code. To be used with JdoTemplate's execute
 * method, assumably often as anonymous classes within a method implementation.
 * The typical implementation will call PersistenceManager CRUD to perform
 * some operations on persistent objects.
 *
 * <p>Note that JDO works on bytecode-modified Java objects, to be able to
 * perform dirty detection on each modification of a persistent instance field.
 * In contrast to Hibernate, using returned objects outside of an active
 * PersistenceManager poses a problem: To be able to read and modify fields
 * e.g. in a web GUI, one has to explicitly make the instances "transient".
 * Reassociation with a new PersistenceManager, e.g. for updates when coming
 * back from the GUI, isn't possible, as the JDO instances have lost their
 * identity when turned transient. This means that either value objects have
 * to be used as parameters, or the contents of the outside-modified instance
 * have to be copied to a freshly loaded active instance on reassociation.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTemplate
 * @see org.springframework.orm.hibernate.HibernateCallback
 */
public interface JdoCallback {

	/**
	 * Gets called by JdoTemplate.execute with an active PersistenceManager.
	 * Does not need to care about activating or closing the PersistenceManager,
	 * or handling transactions.
	 *
	 * <p>Note that JDO callback code will not flush any modifications to the
	 * database if not executed within a transaction. Thus, you need to make
	 * sure that JdoTransactionManager has initiated a JDO transaction when
	 * the callback gets called, at least if you want to write to the database.
	 *
	 * <p>Allows for returning a result object created within the callback,
	 * i.e. a domain object or a collection of domain objects.
	 * A thrown RuntimeException is treated as application exception,
	 * it gets propagated to the caller of the template.
	 *
	 * @param pm active PersistenceManager
	 * @return a result object, or null if none
	 * @throws javax.jdo.JDOException in case of JDO errors
	 * @see JdoTemplate#execute
	 * @see JdoTransactionManager
	 */
	Object doInJdo(PersistenceManager pm) throws JDOException;

}
