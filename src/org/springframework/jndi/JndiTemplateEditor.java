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

package org.springframework.jndi;

import java.beans.PropertyEditorSupport;
import java.util.Properties;

import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * Properties editor for JndiTemplate objects. Allows properties of type
 * JndiTemplate to be populated with a properties-format string.
 * @author Rod Johnson
 * @since 09-May-2003
 * @version $Id: JndiTemplateEditor.java,v 1.3 2004/03/18 02:46:17 trisberg Exp $
 */
public class JndiTemplateEditor extends PropertyEditorSupport {

	private final PropertiesEditor propertiesEditor = new PropertiesEditor();

	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null) {
			throw new IllegalArgumentException("JndiTemplate cannot be created from null string");
		}
		if ("".equals(text)) {
			// empty environment
			setValue(new JndiTemplate());
		}
		else {
			// we have a non-empty properties string
			this.propertiesEditor.setAsText(text);
			Properties props = (Properties) this.propertiesEditor.getValue();
			setValue(new JndiTemplate(props));
		}
	}

}
