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

package org.springframework.web.servlet.tags;

import org.springframework.util.StringUtils;

/**
 * Simple adapter to expose status of a field or object.
 * Set as a variable by the bind tag. Intended for use by
 * JSP and JSTL expressions, and to allow for tag cooperation.
 *
 * <p>Obviously, object status representations do not have an
 * expression and a value but only error codes and messages.
 * For simplicity's sake and to be able to use the same tag,
 * the same status class is used for both scenarios.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class BindStatus {
	
	private final String expression;

	private final Object value;

	private final String[] errorCodes;

	private final String[] errorMessages;

	/**
	 * Create a new BindStatus instance,
	 * representing a field or object status.
	 * @param expression expression suitable for HTML input name,
	 * or null if not field-specific
	 * @param value current field value,
	 * or null if not field-specific
	 * @param errorCodes error codes for the field or object
	 * @param errorMessages resolved error messages for the field or object
	 */
	protected BindStatus(String expression, Object value, String[] errorCodes, String[] errorMessages) {
		this.expression = expression;
		this.value = value;
		this.errorCodes = errorCodes;
		this.errorMessages = errorMessages;
	}

	/**
	 * Return a bind expression that can be used in HTML forms as input name
	 * for the respective field, or null if not field-specific.
	 * <p>Returns a bind path appropriate for resubmission, e.g. "address.street".
	 * Note that the complete bind path as required by the bind tag is
	 * "customer.address.street", if bound to a "customer" bean.
	 */
	public String getExpression() {
		return this.expression;
	}

	/**
	 * Return the current value of the field, i.e. either the property value
	 * or a rejected update, or null if not field-specific.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Return a suitable display value for the field, i.e. empty string
	 * instead of a null value, or null if not field-specific.
	 */
	public String getDisplayValue() {
		return (value != null) ? value.toString() : "";
	}

	/**
	 * Return if this status represents a field or object error.
	 */
	public boolean isError() {
		// need to check array size since BindTag creates empty String[]
		return (errorCodes != null) && (errorCodes.length > 0);
	}

	/**
	 * Return the error codes for the field or object, if any.
	 * Returns an empty array instead of null if none.
	 */
	public String[] getErrorCodes() {
		return (errorCodes != null ? errorCodes : new String[0]);
	}

	/**
	 * Return the first error codes for the field or object, if any.
	 */
	public String getErrorCode() {
		return (errorCodes != null && errorCodes.length > 0 ? errorCodes[0] : "");
	}

	/**
	 * Return the resolved error messages for the field or object,
	 * if any. Returns an empty array instead of null if none.
	 */
	public String[] getErrorMessages() {
		return (errorMessages != null ? errorMessages : new String[0]);
	}

	/**
	 * Return the first error message for the field or object, if any.
	 */
	public String getErrorMessage() {
		return (errorMessages != null && errorMessages.length > 0 ? errorMessages[0] : "");
	}

	/**
	 * Return an error message string, concatenating all messages
	 * separated by the given delimiter.
	 * @param delimiter separator string, e.g. ", " or "<br>"
	 * @return the error message string
	 */
	public String getErrorMessagesAsString(String delimiter) {
		if (errorMessages == null)
			return "";
		return StringUtils.arrayToDelimitedString(errorMessages, delimiter);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("BindStatus: value=[" + value + "]");
		if (isError()) {
			sb.append("; error codes='" + errorCodes + "'; error messages='" + errorMessages + "'; ");
		}
		sb.append("source=" + (isError() ? "error" : "bean"));
		return sb.toString();
	}

}
