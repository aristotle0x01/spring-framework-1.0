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

package org.springframework.aop;

import org.aopalliance.aop.Advice;


/**
 * Superinterface for all before advice. Spring supports only method before
 * advice. Although this is unlikely to change, this API is designed to
 * allow field advice in future if desired.
 * @see org.springframework.aop.MethodBeforeAdvice
 * @author Rod Johnson
 * @version $Id: BeforeAdvice.java,v 1.3 2004/03/19 16:54:36 johnsonr Exp $
 */
public interface BeforeAdvice extends Advice {

}
