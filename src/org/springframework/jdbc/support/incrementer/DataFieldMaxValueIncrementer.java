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

package org.springframework.jdbc.support.incrementer;

import org.springframework.dao.DataAccessException;

/**
 * Interface that defines contract of incrementing any data store field's
 * maximum value. Works much like a sequence number generator.
 *
 * <p>Typical implementations can use RDBMS SQL, native RDBMS sequences,
 * and/or Stored Procedures to do the job.
 *
 * @author Dmitriy Kopylenko
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @version $Id: DataFieldMaxValueIncrementer.java,v 1.3 2004/03/18 02:46:11 trisberg Exp $
 */
public interface DataFieldMaxValueIncrementer {

	/**
	 * Increments data store field's max value as int.
	 * @return int next data store value such as <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException
	 */
	int nextIntValue() throws DataAccessException;

	/**
	 * Increments data store field's max value as long.
	 * @return int next data store value such as <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException
	 */
	long nextLongValue() throws DataAccessException;

	/**
	 * Increments data store field's max value as String.
	 * @return next data store value such as <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException
	 */
	String nextStringValue() throws DataAccessException;

}
