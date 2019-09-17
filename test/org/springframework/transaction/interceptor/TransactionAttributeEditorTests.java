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

package org.springframework.transaction.interceptor;

import javax.ejb.EJBException;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.transaction.TransactionDefinition;

/**
 * Tests to check conversion from String to TransactionAttribute
 * @since 26-Apr-2003
 * @version $Id: TransactionAttributeEditorTests.java,v 1.4 2004/03/18 03:01:17 trisberg Exp $
 * @author Rod Johnson
 */
public class TransactionAttributeEditorTests extends TestCase {
	
	public void testNull() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText(null);
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertTrue(ta == null);
	}
	
	public void testEmptyString() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText("");
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertTrue(ta == null);
	}
	
	/**
	 * Format is PROPAGATION
	 */
	public void testValidPropagationCodeOnly() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText("PROPAGATION_REQUIRED");
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertTrue(ta != null);
		assertTrue(ta.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED);
		assertTrue(ta.getIsolationLevel() == TransactionDefinition.ISOLATION_DEFAULT);
		assertTrue(!ta.isReadOnly());
	}
	
	public void testInvalidPropagationCodeOnly() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		try {
			pe.setAsText("XXPROPAGATION_REQUIRED");
			fail("Should have failed with bogus propagation code");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testValidPropagationCodeAndIsolationCode() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText("PROPAGATION_REQUIRED,ISOLATION_READ_UNCOMMITTED");
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertTrue(ta != null);
		assertTrue(ta.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED);
		assertTrue(ta.getIsolationLevel() == TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
	}
	
	public void testValidPropagationAndIsolationCodesAndInvalidRollbackRule() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		try {
			pe.setAsText("PROPAGATION_REQUIRED,ISOLATION_READ_UNCOMMITTED,XXX");
			fail("Should have failed with bogus rollback rule");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testValidPropagationCodeAndIsolationCodeAndRollbackRules1() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText("PROPAGATION_MANDATORY,ISOLATION_REPEATABLE_READ,timeout_10,-ServletException,+EJBException");
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertNotNull(ta);
		assertEquals(ta.getPropagationBehavior(), TransactionDefinition.PROPAGATION_MANDATORY);
		assertEquals(ta.getIsolationLevel(), TransactionDefinition.ISOLATION_REPEATABLE_READ);
		assertEquals(ta.getTimeout(), 10);
		assertFalse(ta.isReadOnly());
		assertTrue(ta.rollbackOn(new RuntimeException()));
		assertFalse(ta.rollbackOn(new Exception()));
		// Check for our bizarre customized rollback rules
		assertTrue(ta.rollbackOn(new ServletException()));
		assertTrue(!ta.rollbackOn(new EJBException()));
	}

	public void testValidPropagationCodeAndIsolationCodeAndRollbackRules2() {
		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText("+ServletException,readOnly,ISOLATION_READ_COMMITTED,-EJBException,PROPAGATION_SUPPORTS");
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertNotNull(ta);
		assertEquals(ta.getPropagationBehavior(), TransactionDefinition.PROPAGATION_SUPPORTS);
		assertEquals(ta.getIsolationLevel(), TransactionDefinition.ISOLATION_READ_COMMITTED);
		assertEquals(ta.getTimeout(), TransactionDefinition.TIMEOUT_DEFAULT);
		assertTrue(ta.isReadOnly());
		assertTrue(ta.rollbackOn(new RuntimeException()));
		assertFalse(ta.rollbackOn(new Exception()));
		// Check for our bizarre customized rollback rules
		assertFalse(ta.rollbackOn(new ServletException()));
		assertTrue(ta.rollbackOn(new EJBException()));
	}

	public void testDefaultTransactionAttributeToString() {
		DefaultTransactionAttribute source = new DefaultTransactionAttribute();
		source.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		source.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
		source.setTimeout(10);
		source.setReadOnly(true);

		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText(source.toString());
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertEquals(ta, source);
		assertEquals(ta.getPropagationBehavior(), TransactionDefinition.PROPAGATION_SUPPORTS);
		assertEquals(ta.getIsolationLevel(), TransactionDefinition.ISOLATION_REPEATABLE_READ);
		assertEquals(ta.getTimeout(), 10);
		assertTrue(ta.isReadOnly());
		assertTrue(ta.rollbackOn(new RuntimeException()));
		assertFalse(ta.rollbackOn(new Exception()));

		source.setTimeout(9);
		assertNotSame(ta, source);
		source.setTimeout(10);
		assertEquals(ta, source);
	}

	public void testRuleBasedTransactionAttributeToString() {
		RuleBasedTransactionAttribute source = new RuleBasedTransactionAttribute();
		source.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		source.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
		source.setTimeout(10);
		source.setReadOnly(true);
		source.getRollbackRules().add(new RollbackRuleAttribute("IllegalArgumentException"));
		source.getRollbackRules().add(new NoRollbackRuleAttribute("IllegalStateException"));

		TransactionAttributeEditor pe = new TransactionAttributeEditor();
		pe.setAsText(source.toString());
		TransactionAttribute ta = (TransactionAttribute) pe.getValue();
		assertEquals(ta, source);
		assertEquals(ta.getPropagationBehavior(), TransactionDefinition.PROPAGATION_SUPPORTS);
		assertEquals(ta.getIsolationLevel(), TransactionDefinition.ISOLATION_REPEATABLE_READ);
		assertEquals(ta.getTimeout(), 10);
		assertTrue(ta.isReadOnly());
		assertTrue(ta.rollbackOn(new IllegalArgumentException()));
		assertFalse(ta.rollbackOn(new IllegalStateException()));

		source.getRollbackRules().clear();
		assertNotSame(ta, source);
		source.getRollbackRules().add(new RollbackRuleAttribute("IllegalArgumentException"));
		source.getRollbackRules().add(new NoRollbackRuleAttribute("IllegalStateException"));
		assertEquals(ta, source);
	}

}
