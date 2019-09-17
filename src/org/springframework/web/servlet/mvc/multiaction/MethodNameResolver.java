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

package org.springframework.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface that parameterizes the MultiActionController class
 * using the <b>Strategy</b> GoF Design pattern, allowing
 * the mapping from incoming request to handler method name
 * to be varied without affecting other application code.
 * <br>Illustrates how delegation can be more flexible than
 * subclassing.
 * @author Rod Johnson
 */
public interface MethodNameResolver {
	
	/**
	 * Return a method name that can handle this request. Such
	 * mappings are typically, but not necessarily, based on URL.
	 * @return a method name that can handle this request.
	 * Never returns null; throws exception
	 * @throws NoSuchRequestHandlingMethodException if no method
	 * can be found for this URL
	 */
	String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException;
}