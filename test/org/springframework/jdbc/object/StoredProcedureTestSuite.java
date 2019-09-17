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

package org.springframework.jdbc.object;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.easymock.MockControl;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.JdbcTestCase;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterMapper;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Tests for StoredProcedure class
 * @author Thomas Risberg
 * @author Trevor Cook
 * @author Rod Johnson
 * @version $Id: StoredProcedureTestSuite.java,v 1.14 2004/03/18 03:01:19 trisberg Exp $
 */
public class StoredProcedureTestSuite extends JdbcTestCase {

	private MockControl ctrlCallable;
	private CallableStatement mockCallable;

	public StoredProcedureTestSuite(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		ctrlCallable = MockControl.createControl(CallableStatement.class);
		mockCallable = (CallableStatement) ctrlCallable.getMock();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (shouldVerify()) {
			ctrlCallable.verify();
		}
	}

	protected void replay() {
		super.replay();
		ctrlCallable.replay();
	}

	public void testNoSuchStoredProcedure() throws Exception {
		SQLException sex =
			new SQLException(
				"Syntax error or access violation exception",
				"42000");
		mockCallable.execute();
		ctrlCallable.setThrowable(sex);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + NoSuchStoredProcedure.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		NoSuchStoredProcedure sproc = new NoSuchStoredProcedure(mockDataSource);
		try {
			sproc.execute();
			fail("Shouldn't succeed in running stored procedure which doesn't exist");
		} catch (BadSqlGrammarException ex) {
			// OK
		}
	}

	private void testAddInvoice(final int amount, final int custid)
		throws Exception {
		AddInvoice adder = new AddInvoice(mockDataSource);
		int id = adder.execute(amount, custid);
		assertEquals(4, id);
	}

	public void testAddInvoices() throws Exception {
		mockCallable.setObject(1, new Integer(1106), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.setObject(2, new Integer(3), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.registerOutParameter(3, Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getObject(3);
		ctrlCallable.setReturnValue(new Integer(4));
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + AddInvoice.SQL + "(?, ?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		testAddInvoice(1106, 3);
	}

	public void testAddInvoicesWithinTransaction() throws Exception {
		mockCallable.setObject(1, new Integer(1106), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.setObject(2, new Integer(3), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.registerOutParameter(3, Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getObject(3);
		ctrlCallable.setReturnValue(new Integer(4));
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + AddInvoice.SQL + "(?, ?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		TransactionSynchronizationManager.bindResource(
			mockDataSource,
			new ConnectionHolder(mockConnection));

		try {
			testAddInvoice(1106, 3);
		}
		finally {
			TransactionSynchronizationManager.unbindResource(mockDataSource);
		}
	}

		
	/**
	 * Confirm no connection was used to get metadata.
	 * Does not use superclass replay mechanism.
	 * @throws Exception
	 */
	public void testStoredProcedureConfiguredViaJdbcTemplateWithCustomExceptionTranslator() throws Exception {					
		mockCallable.setObject(1, new Integer(11), Types.INTEGER);
		ctrlCallable.setVoidCallable(1);
		mockCallable.registerOutParameter(2, Types.INTEGER);
		ctrlCallable.setVoidCallable(1);
		mockCallable.execute();
		ctrlCallable.setReturnValue(false, 1);
		mockCallable.getObject(2);
		ctrlCallable.setReturnValue(new Integer(5), 1);
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable(1);
		// Must call this here as we're not using setUp()/tearDown() mechanism
		ctrlCallable.replay();

		ctrlConnection = MockControl.createControl(Connection.class);
		mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.prepareCall("{call " + StoredProcedureConfiguredViaJdbcTemplate.SQL + "(?, ?)}");
		ctrlConnection.setReturnValue(mockCallable, 1);
		mockConnection.close();
		ctrlConnection.setVoidCallable(1);
		ctrlConnection.replay();
		
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource localDs = (DataSource) dsControl.getMock();
		localDs.getConnection();
		dsControl.setReturnValue(mockConnection, 1);
		dsControl.replay();

		class TestJdbcTemplate extends JdbcTemplate {
			int calls;
			public Map call(CallableStatementCreator csc, List declaredParameters) throws DataAccessException {
				calls++;
				return super.call(csc, declaredParameters);
			}

		}
		TestJdbcTemplate t = new TestJdbcTemplate();
		t.setDataSource(localDs);
		// Will fail without the following, because we're not able to get a connection from the
		// DataSource here if we need to to create an ExceptionTranslator
		t.setExceptionTranslator(new SQLStateSQLExceptionTranslator());
		StoredProcedureConfiguredViaJdbcTemplate sp = new StoredProcedureConfiguredViaJdbcTemplate(t);
		
		assertEquals(sp.execute(11), 5);
		assertEquals(1, t.calls);
		
		dsControl.verify();
		ctrlCallable.verify();
		ctrlConnection.verify();
	}
	
	/**
	 * Confirm our JdbcTemplate is used
	 * @throws Exception
	 */
	public void testStoredProcedureConfiguredViaJdbcTemplate() throws Exception {
		mockCallable.setObject(1, new Integer(1106), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.registerOutParameter(2, Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getObject(2);
		ctrlCallable.setReturnValue(new Integer(4));
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + StoredProcedureConfiguredViaJdbcTemplate.SQL + "(?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();	
		JdbcTemplate t = new JdbcTemplate();
		t.setDataSource(mockDataSource);
		StoredProcedureConfiguredViaJdbcTemplate sp = new StoredProcedureConfiguredViaJdbcTemplate(t);
	
		assertEquals(sp.execute(1106), 4);
	}

	public void testNullArg() throws Exception {
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);

		mockCallable.setNull(1, Types.VARCHAR);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + NullArg.SQL + "(?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();
		ctrlResultSet.replay();

		NullArg na = new NullArg(mockDataSource);
		na.execute((String) null);
	}

	public void testUnnamedParameter() throws Exception {
		replay();
		try {
			UnnamedParameterStoredProcedure unp =
				new UnnamedParameterStoredProcedure(mockDataSource);
			fail("Shouldn't succeed in creating stored procedure with unnamed parameter");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testMissingParameter() throws Exception {
		replay();

		try {
			MissingParameterStoredProcedure mp =
				new MissingParameterStoredProcedure(mockDataSource);
			mp.execute();
			fail("Shouldn't succeed in running stored procedure with missing required parameter");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testStoredProcedureExceptionTranslator() throws Exception {
		SQLException sex =
			new SQLException(
				"Syntax error or access violation exception",
				"42000");
		mockCallable.execute();
		ctrlCallable.setThrowable(sex);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + StoredProcedureExceptionTranslator.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		StoredProcedureExceptionTranslator sproc =
			new StoredProcedureExceptionTranslator(mockDataSource);
		try {
			sproc.execute();
			fail("Custom exception should be thrown");
		} catch (CustomDataException ex) {
			// OK
		}
	}

	public void testStoredProcedureWithResultSet() throws Exception {
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockCallable.execute();
		ctrlCallable.setReturnValue(true);
		mockCallable.getResultSet();
		ctrlCallable.setReturnValue(mockResultSet);
		mockCallable.getMoreResults();
		ctrlCallable.setReturnValue(false);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + StoredProcedureWithResultSet.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();
		ctrlResultSet.replay();

		StoredProcedureWithResultSet sproc =
			new StoredProcedureWithResultSet(mockDataSource);
		sproc.execute();

		ctrlResultSet.verify();
		assertEquals(2, sproc.getCount());
		
	}
	
	public void testStoredProcedureWithResultSetMapped() throws Exception {
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(2);
		ctrlResultSet.setReturnValue("Foo");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(2);
		ctrlResultSet.setReturnValue("Bar");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockCallable.execute();
		ctrlCallable.setReturnValue(true);
		mockCallable.getResultSet();
		ctrlCallable.setReturnValue(mockResultSet);
		mockCallable.getMoreResults();
		ctrlCallable.setReturnValue(false);
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + StoredProcedureWithResultSetMapped.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();
		ctrlResultSet.replay();

		StoredProcedureWithResultSetMapped sproc =
			new StoredProcedureWithResultSetMapped(mockDataSource);
		Map res = sproc.execute();

		ctrlResultSet.verify();
		
		List rs = (List) res.get("rs");
		assertEquals(2, rs.size());
		assertEquals("Foo", rs.get(0));
		assertEquals("Bar", rs.get(1));		

	}

	public void testParameterMapper() throws Exception {
		mockCallable.setObject(1, "EasyMock for interface java.sql.Connection", Types.VARCHAR);
		ctrlCallable.setVoidCallable();
		mockCallable.registerOutParameter(2, Types.VARCHAR);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getObject(2);
		ctrlCallable.setReturnValue("OK");
		mockCallable.getWarnings();
		ctrlCallable.setReturnValue(null);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + ParameterMapperStoredProcedure.SQL + "(?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();
		ParameterMapperStoredProcedure pmsp =
			new ParameterMapperStoredProcedure(mockDataSource);
		Map out = pmsp.executeTest();
		assertEquals("OK", out.get("out"));
	}

	private class StoredProcedureConfiguredViaJdbcTemplate extends StoredProcedure {
		public static final String SQL = "configured_via_jt";
		public StoredProcedureConfiguredViaJdbcTemplate(JdbcTemplate t) {
			setJdbcTemplate(t);
			setSql(SQL);
			declareParameter(new SqlParameter("intIn", Types.INTEGER));
			declareParameter(new SqlOutParameter("intOut", Types.INTEGER));
			compile();
		}

		public int execute(int intIn) {
			Map in = new HashMap();
			in.put("intIn", new Integer(intIn));
			Map out = execute(in);
			Number intOut = (Number) out.get("intOut");
			return intOut.intValue();
		}
	}

	private class AddInvoice extends StoredProcedure {
		public static final String SQL = "add_invoice";
		public AddInvoice(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(new SqlParameter("amount", Types.INTEGER));
			declareParameter(new SqlParameter("custid", Types.INTEGER));
			declareParameter(new SqlOutParameter("newid", Types.INTEGER));
			compile();
		}

		public int execute(int amount, int custid) {
			Map in = new HashMap();
			in.put("amount", new Integer(amount));
			in.put("custid", new Integer(custid));
			Map out = execute(in);
			Number id = (Number) out.get("newid");
			return id.intValue();
		}
	}

	private class NullArg extends StoredProcedure {

		public static final String SQL = "takes_null";

		public NullArg(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(new SqlParameter("ptest", Types.VARCHAR));
			compile();
		}

		public void execute(String s) {
			Map in = new HashMap();
			in.put("ptest", s);
			Map out = execute(in);
		}
	}

	private class NoSuchStoredProcedure extends StoredProcedure {

		public static final String SQL = "no_sproc_with_this_name";

		public NoSuchStoredProcedure(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class UncompiledStoredProcedure extends StoredProcedure {
		public static final String SQL = "uncompile_sp";
		public UncompiledStoredProcedure(DataSource ds) {
			super(ds, SQL);
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class UnnamedParameterStoredProcedure extends StoredProcedure {

		public UnnamedParameterStoredProcedure(DataSource ds) {
			super(ds, "unnamed_parameter_sp");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		public void execute(int id) {
			Map in = new HashMap();
			in.put("id", new Integer(id));
			Map out = execute(in);

		}
	}

	private class MissingParameterStoredProcedure extends StoredProcedure {

		public MissingParameterStoredProcedure(DataSource ds) {
			setDataSource(ds);
			setSql("takes_string");
			declareParameter(new SqlParameter("mystring", Types.VARCHAR));
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class StoredProcedureWithResultSet extends StoredProcedure {
		public static final String SQL = "sproc_with_result_set";

		private int count = 0;

		public StoredProcedureWithResultSet(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(
				new SqlReturnResultSet("rs", new RowCallbackHandlerImpl()));
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}

		public int getCount() {
			return count;
		}

		private class RowCallbackHandlerImpl implements RowCallbackHandler {
			public void processRow(ResultSet rs) throws SQLException {
				count++;
			}
		}

	}

	private class StoredProcedureWithResultSetMapped extends StoredProcedure {
		public static final String SQL = "sproc_with_result_set";

		public StoredProcedureWithResultSetMapped(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(
				new SqlReturnResultSet("rs", new RowMapperImpl()));
			compile();
		}

		public Map execute() {
			Map out = execute(new HashMap());
			return out;
		}

		private class RowMapperImpl implements RowMapper {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(2);
			}
		}

	}

	private class ParameterMapperStoredProcedure extends StoredProcedure {

		public static final String SQL = "parameter_mapper_sp";

		public ParameterMapperStoredProcedure(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(new SqlParameter("in", Types.VARCHAR));
			declareParameter(new SqlOutParameter("out", Types.VARCHAR));
			compile();
		}

		public Map executeTest() {
			Map out = null;
			out = execute(new TestParameterMapper());
			return out;
		}
		
		private class TestParameterMapper implements ParameterMapper {
			
			private TestParameterMapper() {
			}
			
			public Map createMap(Connection conn) throws SQLException {
				Map inParms = new HashMap();
				String testValue = conn.toString();
				inParms.put("in", testValue);
				return inParms;
			}
		
		}

		
	}

	private class StoredProcedureExceptionTranslator extends StoredProcedure {
		public static final String SQL = "no_sproc_with_this_name";
		public StoredProcedureExceptionTranslator(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			getJdbcTemplate().setExceptionTranslator(new SQLExceptionTranslator() {
				public DataAccessException translate(
					String task,
					String sql,
					SQLException sqlex) {
					return new CustomDataException(sql, sqlex);
				}

			});
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class CustomDataException extends DataAccessException {

		public CustomDataException(String s) {
			super(s);
		}

		public CustomDataException(String s, Throwable ex) {
			super(s, ex);
		}
	}

}
