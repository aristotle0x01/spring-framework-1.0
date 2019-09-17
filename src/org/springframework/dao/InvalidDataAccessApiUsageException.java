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
 * Exception thrown on incorrect usage of the API, such as failing to
 * "compile" a query object that needed compilation before execution.
 *
 * <p>This represents a problem in our Java data access framework,
 * not the underlying data access infrastructure.
 *
 * @author Rod Johnson
 */
public class InvalidDataAccessApiUsageException extends DataAccessException {

	/**
	 * Constructor for InvalidDataAccessApiUsageException.
	 * @param msg message
	 */
	public InvalidDataAccessApiUsageException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for InvalidDataAccessApiUsageException.
	 * @param msg message
	 * @param ex root cause, from an underlying API such as JDBC
	 */
	public InvalidDataAccessApiUsageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
