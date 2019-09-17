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

package org.springframework.transaction;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jndi.JndiTemplate;
import org.springframework.jndi.support.SimpleNamingContext;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Juergen Hoeller
 * @since 12.05.2003
 */
public class JtaTransactionTestSuite extends TestCase {

	public static TransactionTemplate getTransactionTemplateForJta(final String utName, final UserTransaction ut) {
		JndiTemplate jndiTemplate = new JndiTemplate() {
			private boolean invoked = false;
			protected Context createInitialContext() throws NamingException {
				if (invoked) {
					throw new IllegalStateException("Should only invoke createInitialContext once");
				}
				invoked = true;
				Context mockContext = new SimpleNamingContext();
				mockContext.bind(utName, ut);
				return mockContext;
			}
		};
		JtaTransactionManager tm = new JtaTransactionManager();
		tm.setJndiTemplate(jndiTemplate);
		tm.setUserTransactionName(utName);
		tm.afterPropertiesSet();
		return new TransactionTemplate(tm);
	}

	public void testJtaTransactionManagerWithCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCommit(false);
		synchControl.setVoidCallable(1);
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		assertEquals(JtaTransactionManager.SYNCHRONIZATION_ALWAYS, ((JtaTransactionManager) tt.getTransactionManager()).getTransactionSynchronization());
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// something transactional
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithCommitAndSynchronizationOnActual() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCommit(false);
		synchControl.setVoidCallable(1);
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// something transactional
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithCommitAndSynchronizationNever() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_NEVER);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithRollback() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.setTransactionTimeout(10);
		utControl.setVoidCallable(1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.rollback();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta("test", ut);
		tt.setTimeout(10);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithRollbackAndSynchronizationOnActual() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.setTransactionTimeout(10);
		utControl.setVoidCallable(1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.rollback();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta("test", ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
		tt.setTimeout(10);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithRollbackAndSynchronizationNever() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.setTransactionTimeout(10);
		utControl.setVoidCallable(1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.rollback();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta("test", ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronizationName("SYNCHRONIZATION_NEVER");
		tt.setTimeout(10);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithExistingTransaction() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.setRollbackOnly();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithExistingTransactionAndSynchronizationOnActual() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.setRollbackOnly();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithExistingTransactionAndSynchronizationNever() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.setRollbackOnly();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_NEVER);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithExistingAndPropagationSupports() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.setRollbackOnly();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithPropagationSupports() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synch.beforeCompletion();
		synchControl.setVoidCallable(1);
		synch.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
		synchControl.setVoidCallable(1);
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				TransactionSynchronizationManager.registerSynchronization(synch);
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithPropagationSupportsAndSynchronizationOnActual() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithPropagationSupportsAndSynchronizationNever() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		utControl.replay();

		MockControl synchControl = MockControl.createControl(TransactionSynchronization.class);
		final TransactionSynchronization synch = (TransactionSynchronization) synchControl.getMock();
		synchControl.replay();

		TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		((JtaTransactionManager) tt.getTransactionManager()).setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_NEVER);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		tt.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
				status.setRollbackOnly();
			}
		});
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		synchControl.verify();
	}

	public void testJtaTransactionManagerWithIsolationLevel() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown InvalidIsolationLevelException");
		}
		catch (InvalidIsolationLevelException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithoutJtaSupport() throws Exception {
		JtaTransactionManager tm = new JtaTransactionManager();
		try {
			tm.afterPropertiesSet();
			fail("Should have thrown CannotCreateTransactionException");
		}
		catch (CannotCreateTransactionException ex) {
			// expected
		}
	}

	public void testJtaTransactionManagerWithSystemExceptionOnIsExisting() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setThrowable(new SystemException("system exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithNotSupportedExceptionOnBegin() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.begin();
		utControl.setThrowable(new NotSupportedException("not supported"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown IllegalTransactionStateException");
		}
		catch (IllegalTransactionStateException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithUnsupportedOperationExceptionOnBegin() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.begin();
		utControl.setThrowable(new UnsupportedOperationException("not supported"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown IllegalTransactionStateException");
		}
		catch (IllegalTransactionStateException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithSystemExceptionOnBegin() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.begin();
		utControl.setThrowable(new SystemException("system exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithRollbackExceptionOnCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setThrowable(new RollbackException("unexpected rollback"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCompletion(int status) {
							assertTrue("Correct completion status", status == TransactionSynchronization.STATUS_ROLLED_BACK);
						}
					});
				}
			});
			fail("Should have thrown UnexpectedRollbackException");
		}
		catch (UnexpectedRollbackException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithHeuristicMixedExceptionOnCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setThrowable(new HeuristicMixedException("heuristic exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCompletion(int status) {
							assertTrue("Correct completion status", status == TransactionSynchronization.STATUS_UNKNOWN);
						}
					});
				}
			});
			fail("Should have thrown HeuristicCompletionException");
		}
		catch (HeuristicCompletionException ex) {
			// expected
			assertTrue(ex.getOutcomeState() == HeuristicCompletionException.STATE_MIXED);
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithHeuristicRollbackExceptionOnCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setThrowable(new HeuristicRollbackException("heuristic exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCompletion(int status) {
							assertTrue("Correct completion status", status == TransactionSynchronization.STATUS_UNKNOWN);
						}
					});
				}
			});
			fail("Should have thrown HeuristicCompletionException");
		}
		catch (HeuristicCompletionException ex) {
			// expected
			assertTrue(ex.getOutcomeState() == HeuristicCompletionException.STATE_ROLLED_BACK);
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithSystemExceptionOnCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setThrowable(new SystemException("system exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					// something transactional
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCompletion(int status) {
							assertTrue("Correct completion status", status == TransactionSynchronization.STATUS_UNKNOWN);
						}
					});
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithSystemExceptionOnRollback() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.rollback();
		utControl.setThrowable(new SystemException("system exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCompletion(int status) {
							assertTrue("Correct completion status", status == TransactionSynchronization.STATUS_UNKNOWN);
						}
					});
					status.setRollbackOnly();
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithIllegalStateExceptionOnRollbackOnly() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.setRollbackOnly();
		utControl.setThrowable(new IllegalStateException("no existing transaction"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					status.setRollbackOnly();
				}
			});
			fail("Should have thrown NoTransactionException");
		}
		catch (NoTransactionException ex) {
			// expected
		}

		utControl.verify();
	}

	public void testJtaTransactionManagerWithSystemExceptionOnRollbackOnly() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.setRollbackOnly();
		utControl.setThrowable(new SystemException("system exception"));
		utControl.replay();

		try {
			TransactionTemplate tt = getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					status.setRollbackOnly();
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCompletion(int status) {
							assertTrue("Correct completion status", status == TransactionSynchronization.STATUS_UNKNOWN);
						}
					});
				}
			});
			fail("Should have thrown TransactionSystemException");
		}
		catch (TransactionSystemException ex) {
			// expected
		}

		utControl.verify();
	}

}
