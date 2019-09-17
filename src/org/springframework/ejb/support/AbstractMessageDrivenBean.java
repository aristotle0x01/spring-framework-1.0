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

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Convenient superclass for MDBs.
 * Doesn't require JMS, as EJB 2.1 MDBs are no longer
 * JMS-specific: see the AbstractJmsMessageDrivenBean subclass.
 *
 * <p>This class ensures that subclasses have access to the
 * MessageDrivenContext provided by the EJB container, and implement
 * a no argument ejbCreate() method as required by the EJB specification.
 * This ejbCreate() method loads a BeanFactory, before invoking the
 * onEjbCreate() method, which should contain subclass-specific
 * initialization.
 *
 * <p>NB: We cannot use final methods to implement EJB API methods,
 * as this violates the EJB specification. However, there should
 * be no need to override the setMessageDrivenContext() or
 * ejbCreate() methods.
 *
 * @author Rod Johnson
 * @version $Id: AbstractMessageDrivenBean.java,v 1.5 2004/03/18 02:46:14 trisberg Exp $
 */
public abstract class AbstractMessageDrivenBean extends AbstractEnterpriseBean
    implements MessageDrivenBean {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private MessageDrivenContext	messageDrivenContext;
	
	/**
	 * Required lifecycle method. Sets the MessageDriven context.
	 * @param messageDrivenContext MessageDrivenContext
	 */
	public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) {
		this.messageDrivenContext = messageDrivenContext;
	}
	
	/**
	 * Convenience method for subclasses to use.
	 * @return the MessageDrivenContext passed to this EJB by the EJB container
	 */
	protected final MessageDrivenContext getMessageDrivenContext() {
		return messageDrivenContext;
	}

	/**
	 * Lifecycle method required by the EJB specification but not the
	 * MessageDrivenBean interface. This implementation loads the BeanFactory.
	 * <p>Don't override it (although it can't be made final): code initialization
	 * in onEjbCreate(), which is called when the BeanFactory is available.
	 * <p>Unfortunately we can't load the BeanFactory in setSessionContext(),
	 * as resource manager access isn't permitted and the BeanFactory may require it.
	 */
	public void ejbCreate() {
		loadBeanFactory();
		onEjbCreate();
	}
	
	/**
	 * Subclasses must implement this method to do any initialization
	 * they would otherwise have done in an ejbCreate() method. In contrast
	 * to ejbCreate, the BeanFactory will have been loaded here.
	 * <p>The same restrictions apply to the work of this method as
	 * to an ejbCreate() method.
	 */
	protected abstract void onEjbCreate();

}
