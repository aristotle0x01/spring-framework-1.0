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

package org.springframework.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Interface for accessing attributes at runtime. This is a facade,
 * which can accommodate any attributes API such as Jakarta Commons Attributes,
 * or (possibly in future) a Spring attributes implementation.
 *
 * <p>The purpose of using this interface is to decouple Spring code from any
 * specific attributes implementation. Even once JSR-175 is available, there
 * is still value in such a facade interface, as it allows for hierarchical
 * attribute sources: for example, an XML file or properties file might override
 * some attributes defined in source-level metadata with JSR-175 or another framework.
 *
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 * @author Rod Johnson
 * @since Sep 30, 2003
 * @version $Id: Attributes.java,v 1.4 2004/03/18 02:46:17 trisberg Exp $
 */
public interface Attributes {

	/**
	 * Return the class attributes of the target class.
	 * @param targetClass the class that contains attribute information
	 * @return a collection of attributes, possibly an empty collection, never null
	 */
	Collection getAttributes(Class targetClass);

	/**
	 * Return the class attributes of the target class of a given type.
	 * The class attributes are filtered by providing a java.lang.Class
	 * reference to indicate the type to filter on. This is useful if you know
	 * the type of the attribute you are looking for and don't want to sort
	 * through the unfiltered Collection yourself.
	 * @param targetClass the class that contains attribute information
	 * @param filter specify that only this type of class should be returned
	 * @return return only the Collection of attributes that are of the filter type
	 */
	Collection getAttributes(Class targetClass, Class filter);

	/**
	 * Return the method attributes of the target method.
	 * @param targetMethod the method that contains attribute information
	 * @return a Collection of attributes, possibly an empty Collection, never null
	 */
	Collection getAttributes(Method targetMethod);

	/**
	 * Return the method attributes of the target method of a given type.
	 * The method attributes are filtered by providing a java.lang.Class
	 * reference to indicate the type to filter on. This is useful if you know
	 * the type of the attribute you are looking for and don't want to sort
	 * through the unfiltered Collection yourself.
	 * @param targetMethod the method that contains attribute information
	 * @param filter specify that only this type of class should be returned
	 * @return a Collection of attributes, possibly an empty Collection, never null
	 */
	Collection getAttributes(Method targetMethod, Class filter);

	/**
	 * Return the field attributes of the target field.
	 * @param targetField the field that contains attribute information
	 * @return a Collection of attribute, possibly an empty Collection, never null
	 */
	Collection getAttributes(Field targetField);

	/**
	 * Return the field attributes of the target method of a given type.
	 * The field attributes are filtered by providing a java.lang.Class
	 * reference to indicate the type to filter on. This is useful if you know
	 * the type of the attribute you are looking for and don't want to sort
	 * through the unfiltered Collection yourself.
	 * @param targetField the field that contains attribute information
	 * @param filter specify that only this type of class should be returned
	 * @return a Collection of attributes, possibly an empty Collection, never null
	 */
	Collection getAttributes(Field targetField, Class filter);

}
