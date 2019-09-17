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

package org.springframework.jdbc.support.lob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.FileCopyUtils;

/**
 * LobHandler implementation for Oracle databases. Uses proprietary API to
 * create oracle.sql.BLOB and oracle.sql.CLOB instances, as necessary when
 * working with Oracle's JDBC driver. Developed and tested on Oracle 9i.
 *
 * <p>While most databases are able to work with DefaultLobHandler, Oracle just
 * accepts Blob/Clob instances created via its own proprietary BLOB/CLOB API,
 * and additionally doesn't accept large streams for PreparedStatement's
 * corresponding setter methods. Therefore, you need to use a strategy like
 * this LobHandler implementation.
 *
 * <p>Needs to work on a native JDBC Connection, to be able to cast it to
 * oracle.jdbc.OracleConnection. If you pass in Connections from a connection
 * pool (the usual case in a J2EE environment), you need to set an appropriate
 * NativeJdbcExtractor to allow for automatical retrieval of the underlying
 * native JDBC Connection. LobHandler and NativeJdbcExtractor are separate
 * concerns, therefore they are represented by separate strategy interfaces.
 *
 * <p>Coded via reflection to avoid dependencies on Oracle classes.
 * Even reads in Oracle constants via reflection because of different Oracle
 * drivers (classes12, ojdbc14) having different constant values!
 * As it initializes the Oracle classes on instantiation, do not define this
 * as eager-initializing singleton if you do not want to depend on the Oracle
 * JAR being in the class path: use "lazy-init=true" to avoid this issue.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see #setNativeJdbcExtractor
 * @see oracle.sql.BLOB
 * @see oracle.sql.CLOB
 */
public class OracleLobHandler implements LobHandler {

	private static final String CONNECTION_CLASS_NAME = "oracle.jdbc.OracleConnection";

	private static final String BLOB_CLASS_NAME = "oracle.sql.BLOB";

	private static final String CLOB_CLASS_NAME = "oracle.sql.CLOB";

	private static final String DURATION_SESSION_FIELD_NAME = "DURATION_SESSION";

	private static final String MODE_READWRITE_FIELD_NAME = "MODE_READWRITE";


	protected final Log logger = LogFactory.getLog(getClass());

	private final Class blobClass;

	private final Class clobClass;

	private final Map durationSessionConstants = new HashMap();

	private final Map modeReadWriteConstants = new HashMap();

	private NativeJdbcExtractor nativeJdbcExtractor;

	private Boolean cache = Boolean.TRUE;


	/**
	 * This constructor retrieves the oracle.sql.BLOB and oracle.sql.CLOB
	 * classes via reflection, and initializes the values for the
	 * DURATION_SESSION and MODE_READWRITE constants defined there.
	 * @see oracle.sql.BLOB#DURATION_SESSION
	 * @see oracle.sql.BLOB#MODE_READWRITE
	 * @see oracle.sql.CLOB#DURATION_SESSION
	 * @see oracle.sql.CLOB#MODE_READWRITE
	 */
	public OracleLobHandler() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		// initialize oracle.sql.BLOB class
		this.blobClass = getClass().getClassLoader().loadClass(BLOB_CLASS_NAME);
		this.durationSessionConstants.put(this.blobClass,
		                                  new Integer(this.blobClass.getField(DURATION_SESSION_FIELD_NAME).getInt(null)));
		this.modeReadWriteConstants.put(this.blobClass,
		                                new Integer(this.blobClass.getField(MODE_READWRITE_FIELD_NAME).getInt(null)));

		// initialize oracle.sql.CLOB class
		this.clobClass = getClass().getClassLoader().loadClass(CLOB_CLASS_NAME);
		this.durationSessionConstants.put(this.clobClass,
		                                  new Integer(this.clobClass.getField(DURATION_SESSION_FIELD_NAME).getInt(null)));
		this.modeReadWriteConstants.put(this.clobClass,
		                                new Integer(this.clobClass.getField(MODE_READWRITE_FIELD_NAME).getInt(null)));
	}

	/**
	 * Set an appropriate NativeJdbcExtractor to be able to retrieve the underlying
	 * native oracle.jdbc.OracleConnection. This is necessary for DataSource-based
	 * connection pools, as such pools need to return wrapped JDBC object handles.
	 * <p>Effectively, this LobHandler just invokes a single NativeJdbcExtractor
	 * method, namely getNativeConnectionFromStatement with a PreparedStatement
	 * argument, falling back to a PreparedStatement.getConnection() call if no
	 * extractor is set. So if PreparedStatement.getConnection() returns a native
	 * JDBC Connection with your pool, you don't need to specify an extractor.
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor#getNativeConnectionFromStatement
	 * @see oracle.jdbc.OracleConnection
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor nativeJdbcExtractor) {
		this.nativeJdbcExtractor = nativeJdbcExtractor;
	}

	/**
	 * Set whether to cache the temporary LOB in the buffer cache.
	 * This value will be passed into BLOB/CLOB.createTemporary. Default is true.
	 * @see oracle.sql.BLOB#createTemporary
	 * @see oracle.sql.CLOB#createTemporary
	 */
	public void setCache(boolean cache) {
		this.cache = new Boolean(cache);
	}


	public byte[] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as bytes");
		Blob blob = rs.getBlob(columnIndex);
		return (blob != null ? blob.getBytes(1, (int) blob.length()) : new byte[0]);
	}

	public InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as binary stream");
		Blob blob = rs.getBlob(columnIndex);
		return (blob != null ? blob.getBinaryStream() : new ByteArrayInputStream(new byte[0]));
	}

	public String getClobAsString(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as string");
		Clob clob = rs.getClob(columnIndex);
		return (clob != null ? clob.getSubString(1, (int) clob.length()) : "");
	}

	public InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as ASCII stream");
		Clob clob = rs.getClob(columnIndex);
		return (clob != null ? clob.getAsciiStream() : new ByteArrayInputStream(new byte[0]));
	}

	public Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as character stream");
		Clob clob = rs.getClob(columnIndex);
		return (clob != null ? clob.getCharacterStream() : new StringReader(""));
	}

	public LobCreator getLobCreator() {
		return new OracleLobCreator();
	}


	/**
	 * LobCreator implementation for Oracle databases.
	 * Creates Oracle-style temporary BLOBs and CLOBs that it frees on close.
	 * @see #close
	 */
	protected class OracleLobCreator implements LobCreator {

		private final List createdLobs = new ArrayList();

		public void setBlobAsBytes(PreparedStatement ps, int parameterIndex, final byte[] content)
				throws SQLException {
			if (content != null) {
				Blob blob = (Blob) createLob(ps, blobClass, new LobCallback() {
					public void populateLob(Object lob) throws Exception {
						Method methodToInvoke = lob.getClass().getMethod("getBinaryOutputStream", new Class[0]);
						OutputStream out = (OutputStream) methodToInvoke.invoke(lob, null);
						try {
							out.write(content);
							out.flush();
						}
						finally {
							try {
								out.close();
							}
							catch (IOException ex) {
								logger.warn("Could not close BLOB OutputStream", ex);
							}
						}
					}
				});
				ps.setBlob(parameterIndex, blob);
				logger.debug("Set bytes for BLOB with length " + blob.length());
			}
			else {
				ps.setBlob(parameterIndex, null);
				logger.debug("Set BLOB to null");
			}
		}

		public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex,
		                                  final InputStream binaryStream, int contentLength)
				throws SQLException {
			if (binaryStream != null) {
				Blob blob = (Blob) createLob(ps, blobClass, new LobCallback() {
					public void populateLob(Object lob) throws Exception {
						Method methodToInvoke = lob.getClass().getMethod("getBinaryOutputStream", null);
						FileCopyUtils.copy(binaryStream, ((OutputStream) methodToInvoke.invoke(lob, null)));
					}
				});
				ps.setBlob(parameterIndex, blob);
				logger.debug("Set binary stream for BLOB with length " + blob.length());
			}
			else {
				ps.setBlob(parameterIndex, null);
				logger.debug("Set BLOB to null");
			}
		}

		public void setClobAsString(PreparedStatement ps, int parameterIndex, final String content)
		    throws SQLException {
			if (content != null) {
				Clob clob = (Clob) createLob(ps, clobClass, new LobCallback() {
					public void populateLob(Object lob) throws Exception {
						Method methodToInvoke = lob.getClass().getMethod("getCharacterOutputStream", null);
						Writer writer = ((Writer) methodToInvoke.invoke(lob, null));
						try {
							writer.write(content);
							writer.flush();
						}
						finally {
							try {
								writer.close();
							}
							catch (IOException ex) {
								logger.warn("Could not close CLOB Writer", ex);
							}
						}
					}
				});
				ps.setClob(parameterIndex, clob);
				logger.debug("Set string for CLOB with length " + clob.length());
			}
			else {
				ps.setClob(parameterIndex, null);
				logger.debug("Set CLOB to null");
			}
		}

		public void setClobAsAsciiStream(PreparedStatement ps, int parameterIndex,
		                                 final InputStream asciiStream, int contentLength)
		    throws SQLException {
			if (asciiStream != null) {
				Clob clob = (Clob) createLob(ps, clobClass, new LobCallback() {
					public void populateLob(Object lob) throws Exception {
						Method methodToInvoke = lob.getClass().getMethod("getAsciiOutputStream", null);
						FileCopyUtils.copy(asciiStream, ((OutputStream) methodToInvoke.invoke(lob, null)));
					}
				});
				ps.setClob(parameterIndex, clob);
				logger.debug("Set ASCII stream for CLOB with length " + clob.length());
			}
			else {
				ps.setClob(parameterIndex, null);
				logger.debug("Set CLOB to null");
			}
		}

		public void setClobAsCharacterStream(PreparedStatement ps, int parameterIndex,
		                                     final Reader characterStream, int contentLength)
		    throws SQLException {
			if (characterStream != null) {
				Clob clob = (Clob) createLob(ps, clobClass, new LobCallback() {
					public void populateLob(Object lob) throws Exception {
						Method methodToInvoke = lob.getClass().getMethod("getCharacterOutputStream", null);
						FileCopyUtils.copy(characterStream, ((Writer) methodToInvoke.invoke(lob, null)));
					}
				});
				ps.setClob(parameterIndex, clob);
				logger.debug("Set character stream for CLOB with length " + clob.length());
			}
			else {
				ps.setClob(parameterIndex, null);
				logger.debug("Set CLOB to null");
			}
		}


		/**
		 * Create a LOB instance for the given PreparedStatement,
		 * populating it via the given callback.
		 */
		protected Object createLob(PreparedStatement ps, Class lobClass, LobCallback callback) throws SQLException {
			try {
				Object lob = prepareLob(getOracleConnection(ps), lobClass);
				callback.populateLob(lob);
				lob.getClass().getMethod("close", null).invoke(lob, null);
				this.createdLobs.add(lob);
				logger.debug("Created new Oracle LOB");
				return lob;
			}
			catch (SQLException ex) {
				throw ex;
			}
			catch (InvocationTargetException ex) {
				if (ex.getTargetException() instanceof SQLException) {
					throw (SQLException) ex.getTargetException();
				}
				else {
					throw new DataAccessResourceFailureException("Could not create Oracle LOB", ex.getTargetException());
				}
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not create Oracle LOB", ex);
			}
		}

		/**
		 * Retrieve the underlying OracleConnection, using a NativeJdbcExtractor if set.
		 */
		protected Connection getOracleConnection(PreparedStatement ps) throws SQLException, ClassNotFoundException {
			Connection conToUse = (nativeJdbcExtractor != null) ?
					nativeJdbcExtractor.getNativeConnectionFromStatement(ps) : ps.getConnection();
			Class oracleConnectionClass = Class.forName(CONNECTION_CLASS_NAME);
			if (!oracleConnectionClass.isAssignableFrom(conToUse.getClass())) {
				throw new InvalidDataAccessApiUsageException("OracleLobHandler needs to work on OracleConnection - " +
																										 "maybe set the nativeJdbcExtractor property?");
			}
			return conToUse;
		}

		/**
		 * Create and open an oracle.sql.BLOB/CLOB instance via reflection.
		 */
		protected Object prepareLob(Connection con, Class lobClass) throws Exception {
			/*
			BLOB blob = BLOB.createTemporary(con, false, BLOB.DURATION_SESSION);
			blob.open(BLOB.MODE_READWRITE);
			return blob;
			*/
			Method createTemporary = lobClass.getMethod("createTemporary",
			                                            new Class[] {Connection.class, boolean.class, int.class});
			Object lob = createTemporary.invoke(null, new Object[] {con, cache,
			                                                        durationSessionConstants.get(lobClass)});
			Method open = lobClass.getMethod("open", new Class[] {int.class});
			open.invoke(lob, new Object[] {modeReadWriteConstants.get(lobClass)});
			return lob;
		}

		/**
		 * Free all temporary BLOBs and CLOBs created by this creator.
		 */
		public void close() {
			try {
				for (Iterator it = this.createdLobs.iterator(); it.hasNext();) {
					/*
					BLOB blob = (BLOB) it.next();
					blob.freeTemporary();
					*/
					Object lob = it.next();
					Method freeTemporary = lob.getClass().getMethod("freeTemporary", new Class[0]);
					freeTemporary.invoke(lob, new Object[0]);
					it.remove();
				}
			}
			catch (InvocationTargetException ex) {
				logger.error("Could not free Oracle LOB", ex.getTargetException());
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not free Oracle LOB", ex);
			}
		}
	}


	/**
	 * Internal callback interface for use with createLob.
	 * @see OracleLobCreator#createLob
	 */
	protected static interface LobCallback {

		/**
		 * Populate the given BLOB or CLOB instance with content.
		 * @throws Exception any exception including InvocationTargetException
		 */
		void populateLob(Object lob) throws Exception;
	}

}
