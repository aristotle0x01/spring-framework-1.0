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

package org.springframework.mail.javamail;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Callback interface for preparation of JavaMail MIME messages.
 *
 * <p>The corresponding send methods of JavaMailSender will take care
 * of the actual creation of a MimeMessage instance, and of proper
 * exception conversion.
 *
 * <p>It is often convenient to use a MimeMessageHelper for populating
 * the passed-in MimeMessage, particularly when working with attachments
 * or special character sets.
 *
 * @author Juergen Hoeller
 * @since 07.10.2003
 * @version $Id: MimeMessagePreparator.java,v 1.4 2004/03/18 02:46:14 trisberg Exp $
 * @see JavaMailSender#send(MimeMessagePreparator)
 * @see JavaMailSender#send(MimeMessagePreparator[])
 * @see MimeMessageHelper
 */
public interface MimeMessagePreparator {

	/**
	 * Prepare the given new MimeMessage instance.
	 * @param mimeMessage the message to prepare
	 * @throws MessagingException passing any exceptions thrown by MimeMessage
	 * methods through for automatic conversion to the MailException hierarchy
	 * @throws IOException passing any exceptions thrown by MimeMessage methods
	 * through for automatic conversion to the MailException hierarchy
	 */
	void prepare(MimeMessage mimeMessage) throws MessagingException, IOException;

}
