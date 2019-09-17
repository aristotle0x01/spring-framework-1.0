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

package org.springframework.dao;

/**
 * Root for exceptions thrown when we use a data access resource incorrectly.
 * Thrown for example on specifying bad SQL when using a RDBMS.
 * Resource-specific subclasses will probably be supplied by data access packages.
 * @author Rod Johnson
 * @version $Id: InvalidDataAccessResourceUsageException.java,v 1.4 2004/03/18 02:46:07 trisberg Exp $
 */
public class InvalidDataAccessResourceUsageException extends DataAccessException {
	
	/**
	 * Constructor for InvalidDataAccessResourceUsageException.
	 * @param msg message
	 */
	public InvalidDataAccessResourceUsageException(String msg) {
		super(msg);
	}
	/**
	 * Constructor for InvalidDataAccessResourceUsageException.
	 * @param msg message
	 * @param ex root cause
	 */
	public InvalidDataAccessResourceUsageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
