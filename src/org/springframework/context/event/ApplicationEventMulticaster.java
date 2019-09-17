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

package org.springframework.context.event;

import org.springframework.context.ApplicationListener;

/**
 * Subinterface of ApplicationListener to be implemented by
 * listeners that can broadcast events to other listeners.
 * @author Rod Johnson
 */
public interface ApplicationEventMulticaster extends ApplicationListener {

	/**
	 * Add a listener to be notified of all events
	 * @param listener listener to add
	 */
	void addApplicationListener(ApplicationListener listener);

	/**
	 * Remove a listener in the notification list]
	 * @param listener listener to remove
	 */
	void removeApplicationListener(ApplicationListener listener);

	/**
	 * Remove all listeners registered with this multicaster.
	 * It will perform no action on event notification until more
	 * listeners are registered.
	 */
	void removeAllListeners();

}

