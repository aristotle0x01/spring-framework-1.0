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

package org.springframework.jdbc.core;

import java.util.List;

/**
 * Extension of RowCallbackHandler interfaces that saves the
 * accumulated results as a List.
 * @author Rod Johnson
 * @version $Id: ResultReader.java,v 1.4 2004/03/18 02:46:08 trisberg Exp $
 */
public interface ResultReader extends RowCallbackHandler {
	 
	/**
	 * Return all results, disconnected from the JDBC ResultSet.
	 * Never returns null; returns the empty collection if there
	 * were no results.
	 */
	List getResults();

}
