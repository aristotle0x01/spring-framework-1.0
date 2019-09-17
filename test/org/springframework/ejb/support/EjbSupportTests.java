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

package org.springframework.ejb.support;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.jms.Message;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * Most of the value of the tests here is in being forced
 * to implement ejbCreate() methods.
 * @author Rod Johnson
 * @since 21-May-2003
 * @version $Id: EjbSupportTests.java,v 1.11 2004/03/18 03:01:38 trisberg Exp $
 */
public class EjbSupportTests extends TestCase {

	public void testSfsb() throws CreateException {
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
		
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLocator bfl = new BeanFactoryLocator() {
			public BeanFactoryReference useBeanFactory(String factoryKey)
					throws FatalBeanException {
				return new BeanFactoryReference() {
					public BeanFactory getFactory() {
						return bf;
					}
					public void release() throws FatalBeanException {
						// nothing to do in default implementation
					}
				};
			}
		};
		
		// Basically the test is what needed to be implemented here!
		class MySfsb extends AbstractStatefulSessionBean {
			public void ejbCreate() throws CreateException {
				loadBeanFactory();
				assertTrue(getBeanFactory() == bf);
			}
			public void ejbActivate() throws EJBException, RemoteException {
				throw new UnsupportedOperationException("ejbActivate");
			}
			public void ejbPassivate() throws EJBException, RemoteException {
				throw new UnsupportedOperationException("ejbPassivate");
			}

		}
		
		MySfsb sfsb = new MySfsb();
		sfsb.setBeanFactoryLocator(bfl);
		sfsb.setSessionContext(sc);
		sfsb.ejbCreate();
		assertTrue(sc == sfsb.getSessionContext());
	}
	
	/**
	 * Check there's a helpful message if no JNDI key is present
	 */
	public void testHelpfulNamingLookupMessage() throws NamingException, CreateException {
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
	
		// Leave with default XmlBeanFactoryLoader
	
		// Basically the test is what needed to be implemented here!
		AbstractStatelessSessionBean slsb = new AbstractStatelessSessionBean() {
			public void onEjbCreate() {
			}
		};
	
		slsb.setSessionContext(sc);
		try {
			slsb.ejbCreate();
			fail();
		}
		catch (BeansException ex) {
			assertTrue(ex.getMessage().indexOf("environment") != -1);
			assertTrue(ex.getMessage().indexOf("ejb/BeanFactoryPath") != -1);
		}
	}
	
	public void testSlsb() throws Exception {
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
		
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLocator bfl = new BeanFactoryLocator() {
			public BeanFactoryReference useBeanFactory(String factoryKey)
					throws FatalBeanException {
				return new BeanFactoryReference() {
					public BeanFactory getFactory() {
						return bf;
					}
					public void release() throws FatalBeanException {
						// nothing to do in default implementation
					}
				};
			}
		};
	
		AbstractStatelessSessionBean slsb = new AbstractStatelessSessionBean() {
			protected void onEjbCreate() throws CreateException {
				assertTrue(getBeanFactory() == bf);
			}
		};
		// Must call this method before ejbCreate()
		slsb.setBeanFactoryLocator(bfl);
		slsb.setSessionContext(sc);
		assertTrue(sc == slsb.getSessionContext());
		slsb.ejbCreate();
		try {
			slsb.ejbActivate();
			fail("Shouldn't allow activation of SLSB");
		}
		catch (IllegalStateException ex) {
			// Ok
		}
		try {
			slsb.ejbPassivate();
			fail("Shouldn't allow passivation of SLSB");
		}
		catch (IllegalStateException ex) {
			// Ok
		}
	}


	public void testJmsMdb() throws Exception {
		MockControl mc = MockControl.createControl(MessageDrivenContext.class);
		MessageDrivenContext sc = (MessageDrivenContext) mc.getMock();
		mc.replay();
	
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLocator bfl = new BeanFactoryLocator() {
			public BeanFactoryReference useBeanFactory(String factoryKey)
					throws FatalBeanException {
				return new BeanFactoryReference() {
					public BeanFactory getFactory() {
						return bf;
					}
					public void release() throws FatalBeanException {
						// nothing to do in default implementation
					}
				};
			}
		};

		AbstractJmsMessageDrivenBean mdb = new AbstractJmsMessageDrivenBean() {
			protected void onEjbCreate() {
				assertTrue(getBeanFactory() == bf);
			}

			public void onMessage(Message arg0) {
				throw new UnsupportedOperationException("onMessage");
			}
		};
		// Must call this method before ejbCreate()
		mdb.setBeanFactoryLocator(bfl);
		mdb.setMessageDrivenContext(sc);
		assertTrue(sc == mdb.getMessageDrivenContext());
		mdb.ejbCreate();
	}
	
	public void testCannotLoadBeanFactory() throws Exception {
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
	
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLocator bfl = new BeanFactoryLocator() {
			public BeanFactoryReference useBeanFactory(String factoryKey)
					throws FatalBeanException {
				throw new BootstrapException("", null);
		}};

		AbstractStatelessSessionBean slsb = new AbstractStatelessSessionBean() {
			protected void onEjbCreate() throws CreateException {
			}
		};
		// Must call this method before ejbCreate()
		slsb.setBeanFactoryLocator(bfl);
		slsb.setSessionContext(sc);
		
		try {
			slsb.ejbCreate();
			fail();
		}
		catch (BeansException ex) {
			// Ok
		}
	}
}
