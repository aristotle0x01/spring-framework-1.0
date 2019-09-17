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

package org.springframework.remoting.jaxrpc;

import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean for locally defined JAX-RPC Service references.
 * Uses LocalJaxRpcServiceFactory's facilities underneath.
 *
 * <p>Alternatively, JAX-RPC Service references can be looked up
 * in the JNDI environment of the J2EE container.
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see javax.xml.rpc.Service
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see JaxRpcPortProxyFactoryBean
 */
public class LocalJaxRpcServiceFactoryBean extends LocalJaxRpcServiceFactory
		implements FactoryBean, InitializingBean {

	private Service service;

	public void afterPropertiesSet() throws ServiceException {
		this.service = createJaxRpcService();
	}

	public Object getObject() throws Exception {
		return this.service;
	}

	public Class getObjectType() {
		return (this.service != null ? this.service.getClass() : Service.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
