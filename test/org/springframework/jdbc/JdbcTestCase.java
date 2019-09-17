/*
 * JdbcTestCase.java
 *
 * Copyright (C) 2002 by Interprise Software.  All rights reserved.
 */
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

package org.springframework.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * @task enter type comments
 * 
 * @author <a href="mailto:tcook@interprisesoftware.com">Trevor D. Cook</a>
 * @version $Id: JdbcTestCase.java,v 1.4 2004/03/18 03:01:15 trisberg Exp $
 */
public abstract class JdbcTestCase extends TestCase {

	protected MockControl ctrlDataSource;
	protected DataSource mockDataSource;
	protected MockControl ctrlConnection;
	protected Connection mockConnection;
	
	/**
	 * Set to true if the user wants verification, indicated
	 * by a call to replay(). We need to make this optional,
	 * otherwise we setUp() will always result in verification failures
	 */
	private boolean shouldVerify;

	/**
	 * 
	 */
	public JdbcTestCase() {
		super();
	}

	/**
	 * @param name
	 */
	public JdbcTestCase(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		this.shouldVerify = false;
		super.setUp();

		ctrlConnection = MockControl.createControl(Connection.class);
		mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.getMetaData();
		ctrlConnection.setDefaultReturnValue(null);
		mockConnection.close();
		ctrlConnection.setDefaultVoidCallable();

		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		ctrlDataSource.setDefaultReturnValue(mockConnection);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		// We shouldn't verify unless the user called replay()
		if (shouldVerify()) {
			ctrlDataSource.verify();
			ctrlConnection.verify();
		}
	}

	protected boolean shouldVerify() {
		return this.shouldVerify;
	}

	protected void replay() {
		this.shouldVerify = true;
		ctrlDataSource.replay();
		ctrlConnection.replay();
	}

}
