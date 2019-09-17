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

package org.springframework.validation;

import java.util.List;

/**
 * Interface to be implemented by objects that can store and expose
 * information about data binding errors.
 *
 * <p>Field names can be properties of the given object (e.g. "name"
 * when binding to a customer object), or nested fields in case of
 * subobjects (e.g. "address.street"). Supports subtree navigation
 * via setNestedPath, e.g. an AddressValidator validates "address",
 * not being aware that this is a subobject of customer.
 *
 * <p>Note: Errors objects are single-threaded.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setNestedPath
 */
public interface Errors {

	/**
	 * Return the name of the bound object.
	 */
	String getObjectName();

	/**
	 * Reject the current object, using the given error description.
	 * @param errorCode error code, interpretable as message key
	 * @param defaultMessage fallback default message
	 */
	void reject(String errorCode, String defaultMessage);

	/**
	 * Reject the current object, using the given error description.
	 * @param errorCode error code, interpretable as message key
	 * @param errorArgs error arguments, for argument binding via MessageFormat
	 * (can be null)
	 * @param defaultMessage fallback default message
	 */
	void reject(String errorCode, Object[] errorArgs, String defaultMessage);

	/**
	 * Reject the given field of the current object, using the given error description.
	 * @param field field name
	 * @param errorCode error code, interpretable as message key
	 * @param defaultMessage fallback default message
	 */
	void rejectValue(String field, String errorCode, String defaultMessage);

	/**
	 * Reject the given field of the current object, using the given error description.
	 * @param field field name
	 * @param errorCode error code, interpretable as message key
	 * @param errorArgs error arguments, for argument binding via MessageFormat
	 * (can be null)
	 * @param defaultMessage fallback default message
	 */
	void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage);

	/**
	 * Return if there were any errors.
	 */
	boolean hasErrors();

	/**
	 * Return the total number of errors.
	 */
	int getErrorCount();

	/**
	 * Get all errors, both global and field ones.
	 * @return List of ObjectError instances
	 */
	List getAllErrors();

	/**
	 * Return if there were any global (i.e. not field-specific) errors.
	 */
	boolean hasGlobalErrors();

	/**
	 * Return the number of global (i.e. not field-specific) errors.
	 */
	int getGlobalErrorCount();

	/**
	 * Get all global errors.
	 * @return List of ObjectError instances
	 */
	List getGlobalErrors();

	/**
	 * Get the first global error, if any.
	 * @return the global error, or null
	 */
	ObjectError getGlobalError();

	/**
	 * Return if there are any errors associated with the given field.
	 * @param field field name
	 * @return if there were any errors associated with the given field
	 */
	boolean hasFieldErrors(String field);

	/**
	 * Return the number of errors associated with the given field.
	 * @param field field name
	 * @return the number of errors associated with the given field
	 */
	int getFieldErrorCount(String field);

	/**
	 * Get all errors associated with the given field.
	 * @param field field name
	 * @return List of FieldError instances
	 */
	List getFieldErrors(String field);

	/**
	 * Get the first error associated with the given field, if any.
	 * @return the field-specific error, or null
	 */
	FieldError getFieldError(String field);

	/**
	 * Return the current value of the given field, either the current
	 * bean property value or a rejected update from the last binding.
	 * Allows for convenient access to user-specified field values,
	 * even if there were type mismatches.
	 * @param field field name
	 * @return the current value of the given field
	 */
	Object getFieldValue(String field);

	/**
	 * Allow context to be changed so that standard validators can validate
	 * subtrees. Reject calls prepend the given path to the field names.
	 * <p>For example, an address validator could validate the subobject
	 * "address" of a customer object.
	 * @param nestedPath nested path within this object,
	 * e.g. "address" (defaults to "", null is also acceptable).
	 * Can end with a dot: both "address" and "address." are valid.
	 */
	void setNestedPath(String nestedPath);

	/**
	 * Return the current nested path of this Errors object.
	 * <p>Returns a nested path with a dot, i.e. "address.", for easy
	 * building of concatenated paths. Default is an empty String.
	 */
	String getNestedPath();

}
