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
 * Exception thrown when we couldn't cleanup after a data
 * access operation, but the actual operation went OK.
 * For example, this exception or a subclass might be thrown if a JDBC Connection
 * couldn't be closed after it had been used successfully.
 * @author Rod Johnson
 */
public class CleanupFailureDataAccessException extends DataAccessException {

	/**
	 * Constructor for CleanupFailureDataAccessException.
	 * @param msg Message
	 * @param ex Root cause from the underlying data access API,
	 * such as JDBC
	 */
	public CleanupFailureDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
