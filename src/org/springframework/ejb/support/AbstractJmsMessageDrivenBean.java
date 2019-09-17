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

import javax.jms.MessageListener;

/** 
 * Convenient superclass for JMS MDBs.
 * Requires subclasses to implement the JMS interface MessageListener.
 * @author Rod Johnson
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public abstract class AbstractJmsMessageDrivenBean 
	extends AbstractMessageDrivenBean
	implements MessageListener {
	
	// Empty: the purpose of this class is to ensure
	// that subclasses implement javax.jms.MessageListener
	
} 
