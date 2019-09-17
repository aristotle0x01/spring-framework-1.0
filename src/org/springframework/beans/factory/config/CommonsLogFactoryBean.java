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

package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for
 * <a href="http://jakarta.apache.org/commons/logging.html">commons-logging</a>
 * Log instances.
 * Will expose the created Log object on getBean calls, and can be passed
 * to bean properites of type org.apache.commons.logging.Log.
 *
 * <p>Useful for sharing Log instances among multiple beans instead of using
 * one Log instance per class name, e.g. for common log topics.
 *
 * @author Juergen Hoeller
 * @since 16.11.2003
 * @see org.apache.commons.logging.Log
 */
public class CommonsLogFactoryBean implements FactoryBean {

  private Log log = null;

  public void setLogName(String logName) {
		if (logName == null) {
			throw new IllegalArgumentException("'logName' must be specified");
		}
    this.log = LogFactory.getLog(logName);
  }

  public Object getObject() {
    return log;
  }

	public Class getObjectType() {
		return (this.log != null ? this.log.getClass() : Log.class);
	}

  public boolean isSingleton() {
    return true;
  }

}
