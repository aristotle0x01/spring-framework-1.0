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

package org.springframework.aop.target;

/**
 * TargetSource that creates a new instance of the target bean for each request.
 * Can only be used in a bean factory.
 * @author Rod Johnson
 * @version $Id: PrototypeTargetSource.java,v 1.5 2004/03/18 02:46:13 trisberg Exp $
 */
public final class PrototypeTargetSource extends AbstractPrototypeTargetSource {

	public Object getTarget() {
		return newPrototypeInstance();
	}
	
	public void releaseTarget(Object target) {
		// Do nothing
	}

}
