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

package org.springframework.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @version $Id: MailTestSuite.java,v 1.15 2004/03/18 03:01:39 trisberg Exp $
 */
public class MailTestSuite extends TestCase {

	public void testSimpleMessage() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("me@mail.org");
		message.setTo("you@mail.org");

		SimpleMailMessage messageCopy = new SimpleMailMessage(message);
		assertEquals("me@mail.org", messageCopy.getFrom());
		assertEquals("you@mail.org", messageCopy.getTo()[0]);

		message.setCc(new String[] {"he@mail.org", "she@mail.org"});
		message.setBcc(new String[] {"us@mail.org", "them@mail.org"});
		message.setSubject("my subject");
		message.setText("my text");

		assertEquals("me@mail.org", message.getFrom());
		assertEquals("you@mail.org", message.getTo()[0]);
		List ccs = Arrays.asList(message.getCc());
		assertTrue(ccs.contains("he@mail.org"));
		assertTrue(ccs.contains("she@mail.org"));
		List bccs = Arrays.asList(message.getBcc());
		assertTrue(bccs.contains("us@mail.org"));
		assertTrue(bccs.contains("them@mail.org"));
		assertEquals("my subject", message.getSubject());
		assertEquals("my text", message.getText());

		messageCopy = new SimpleMailMessage(message);
		assertEquals("me@mail.org", messageCopy.getFrom());
		assertEquals("you@mail.org", messageCopy.getTo()[0]);
		ccs = Arrays.asList(messageCopy.getCc());
		assertTrue(ccs.contains("he@mail.org"));
		assertTrue(ccs.contains("she@mail.org"));
		bccs = Arrays.asList(message.getBcc());
		assertTrue(bccs.contains("us@mail.org"));
		assertTrue(bccs.contains("them@mail.org"));
		assertEquals("my subject", messageCopy.getSubject());
		assertEquals("my text", messageCopy.getText());
	}

	public void testJavaMailSenderWithSimpleMessage() throws MessagingException, IOException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setPort(30);
		sender.setUsername("username");
		sender.setPassword("password");

		SimpleMailMessage simpleMessage = new SimpleMailMessage();
		simpleMessage.setFrom("me@mail.org");
		simpleMessage.setTo("you@mail.org");
		simpleMessage.setCc(new String[] {"he@mail.org", "she@mail.org"});
		simpleMessage.setBcc(new String[] {"us@mail.org", "them@mail.org"});
		simpleMessage.setSubject("my subject");
		simpleMessage.setText("my text");
		sender.send(simpleMessage);

		assertEquals("host", sender.transport.getConnectedHost());
		assertEquals(30, sender.transport.getConnectedPort());
		assertEquals("username", sender.transport.getConnectedUsername());
		assertEquals("password", sender.transport.getConnectedPassword());
		assertTrue(sender.transport.isCloseCalled());

		assertEquals(1, sender.transport.getSentMessages().size());
		MimeMessage sentMessage = sender.transport.getSentMessage(0);
		List froms = Arrays.asList(sentMessage.getFrom());
		assertEquals(1, froms.size());
		assertEquals("me@mail.org", ((InternetAddress) froms.get(0)).getAddress());
		List tos = Arrays.asList(sentMessage.getRecipients(Message.RecipientType.TO));
		assertEquals(1, tos.size());
		assertEquals("you@mail.org", ((InternetAddress) tos.get(0)).getAddress());
		List ccs = Arrays.asList(sentMessage.getRecipients(Message.RecipientType.CC));
		assertEquals(2, ccs.size());
		assertEquals("he@mail.org", ((InternetAddress) ccs.get(0)).getAddress());
		assertEquals("she@mail.org", ((InternetAddress) ccs.get(1)).getAddress());
		List bccs = Arrays.asList(sentMessage.getRecipients(Message.RecipientType.BCC));
		assertEquals(2, bccs.size());
		assertEquals("us@mail.org", ((InternetAddress) bccs.get(0)).getAddress());
		assertEquals("them@mail.org", ((InternetAddress) bccs.get(1)).getAddress());
		assertEquals("my subject", sentMessage.getSubject());
		assertEquals("my text", sentMessage.getContent());
	}

	public void testJavaMailSenderWithSimpleMessages() throws MessagingException, IOException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		SimpleMailMessage simpleMessage1 = new SimpleMailMessage();
		simpleMessage1.setTo("he@mail.org");
		SimpleMailMessage simpleMessage2 = new SimpleMailMessage();
		simpleMessage2.setTo("she@mail.org");
		sender.send(new SimpleMailMessage[] {simpleMessage1, simpleMessage2});

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());

		assertEquals(2, sender.transport.getSentMessages().size());
		MimeMessage sentMessage1 = sender.transport.getSentMessage(0);
		List tos1 = Arrays.asList(sentMessage1.getRecipients(Message.RecipientType.TO));
		assertEquals(1, tos1.size());
		assertEquals("he@mail.org", ((InternetAddress) tos1.get(0)).getAddress());
		MimeMessage sentMessage2 = sender.transport.getSentMessage(1);
		List tos2 = Arrays.asList(sentMessage2.getRecipients(Message.RecipientType.TO));
		assertEquals(1, tos2.size());
		assertEquals("she@mail.org", ((InternetAddress) tos2.get(0)).getAddress());
	}

	public void testJavaMailSenderWithMimeMessage() throws MessagingException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage = sender.createMimeMessage();
		mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("you@mail.org"));
		sender.send(mimeMessage);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(1, sender.transport.getSentMessages().size());
		assertEquals(mimeMessage, sender.transport.getSentMessage(0));
	}

	public void testJavaMailSenderWithMimeMessages() throws MessagingException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage1 = sender.createMimeMessage();
		mimeMessage1.setRecipient(Message.RecipientType.TO, new InternetAddress("he@mail.org"));
		MimeMessage mimeMessage2 = sender.createMimeMessage();
		mimeMessage2.setRecipient(Message.RecipientType.TO, new InternetAddress("she@mail.org"));
		sender.send(new MimeMessage[] {mimeMessage1, mimeMessage2});

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(2, sender.transport.getSentMessages().size());
		assertEquals(mimeMessage1, sender.transport.getSentMessage(0));
		assertEquals(mimeMessage2, sender.transport.getSentMessage(1));
	}

	public void testJavaMailSenderWithMimeMessagePreparator() {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		final List messages = new ArrayList();

		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("you@mail.org"));
				messages.add(mimeMessage);
			}
		};
		sender.send(preparator);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(1, sender.transport.getSentMessages().size());
		assertEquals(messages.get(0), sender.transport.getSentMessage(0));
	}

	public void testJavaMailSenderWithMimeMessagePreparators() {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		final List messages = new ArrayList();

		MimeMessagePreparator preparator1 = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("he@mail.org"));
				messages.add(mimeMessage);
			}
		};
		MimeMessagePreparator preparator2 = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("she@mail.org"));
				messages.add(mimeMessage);
			}
		};
		sender.send(new MimeMessagePreparator[] {preparator1, preparator2});

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(2, sender.transport.getSentMessages().size());
		assertEquals(messages.get(0), sender.transport.getSentMessage(0));
		assertEquals(messages.get(1), sender.transport.getSentMessage(1));
	}

	public void testJavaMailSenderWithParseExceptionOnSimpleMessage() {
		MockJavaMailSender sender = new MockJavaMailSender();
		SimpleMailMessage simpleMessage = new SimpleMailMessage();
		simpleMessage.setFrom("");
		try {
			sender.send(simpleMessage);
		}
		catch (MailParseException ex) {
			// expected
			assertTrue(ex.getCause() instanceof AddressException);
		}
	}

	public void testJavaMailSenderWithParseExceptionOnMimeMessagePreparator() {
		MockJavaMailSender sender = new MockJavaMailSender();
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setFrom(new InternetAddress(""));
			}
		};
		try {
			sender.send(preparator);
		}
		catch (MailParseException ex) {
			// expected
			assertTrue(ex.getCause() instanceof AddressException);
		}
	}

	public void testJavaMailSenderWithCustomSession() throws MessagingException {
		final Session session = Session.getInstance(new Properties());
		MockJavaMailSender sender = new MockJavaMailSender() {
			protected Transport getTransport(Session sess) throws NoSuchProviderException {
				assertEquals(session, sess);
				return super.getTransport(sess);
			}
		};
		sender.setSession(session);
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage = sender.createMimeMessage();
		mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("you@mail.org"));
		sender.send(mimeMessage);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(1, sender.transport.getSentMessages().size());
		assertEquals(mimeMessage, sender.transport.getSentMessage(0));
	}

	public void testJavaMailProperties() throws MessagingException {
		Properties props = new Properties();
		props.setProperty("bogusKey", "bogusValue");
		MockJavaMailSender sender = new MockJavaMailSender() {
			protected Transport getTransport(Session sess) throws NoSuchProviderException {
				assertEquals("bogusValue", sess.getProperty("bogusKey"));
				return super.getTransport(sess);
			}
		};
		sender.setJavaMailProperties(props);
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage = sender.createMimeMessage();
		mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("you@mail.org"));
		sender.send(mimeMessage);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(1, sender.transport.getSentMessages().size());
		assertEquals(mimeMessage, sender.transport.getSentMessage(0));
	}

	public void testFailedMailServerConnect() throws Exception {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost(null);
		sender.setUsername("username");
		sender.setPassword("password");
		SimpleMailMessage simpleMessage1 = new SimpleMailMessage();
		try {
			sender.send(simpleMessage1);
			fail("Should have thrown MailSendException");
		}
		catch (MailSendException ex) {
			// expected
			assertTrue(ex.getFailedMessages() != null);
			assertTrue(ex.getFailedMessages().isEmpty());
		}
	}

	public void testFailedSimpleMessage() throws Exception {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		SimpleMailMessage simpleMessage1 = new SimpleMailMessage();
		simpleMessage1.setTo("he@mail.org");
		simpleMessage1.setSubject("fail");
		SimpleMailMessage simpleMessage2 = new SimpleMailMessage();
		simpleMessage2.setTo("she@mail.org");

		try {
			sender.send(new SimpleMailMessage[] {simpleMessage1, simpleMessage2});
		}
		catch (MailSendException ex) {
			assertEquals(sender.transport.getConnectedHost(), "host");
			assertEquals(sender.transport.getConnectedUsername(), "username");
			assertEquals(sender.transport.getConnectedPassword(), "password");
			assertTrue(sender.transport.isCloseCalled());
			assertEquals(1, sender.transport.getSentMessages().size());
			assertEquals(new InternetAddress("she@mail.org"), sender.transport.getSentMessage(0).getAllRecipients()[0]);
			assertEquals(1, ex.getFailedMessages().size());
			assertEquals(simpleMessage1, ex.getFailedMessages().keySet().iterator().next());
			Object subEx = ex.getFailedMessages().values().iterator().next();
			assertTrue(subEx instanceof MessagingException);
			assertEquals("failed", ((MessagingException) subEx).getMessage());
		}
	}

	public void testFailedMimeMessage() throws Exception {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage1 = sender.createMimeMessage();
		mimeMessage1.setRecipient(Message.RecipientType.TO, new InternetAddress("he@mail.org"));
		mimeMessage1.setSubject("fail");
		MimeMessage mimeMessage2 = sender.createMimeMessage();
		mimeMessage2.setRecipient(Message.RecipientType.TO, new InternetAddress("she@mail.org"));

		try {
			sender.send(new MimeMessage[] {mimeMessage1, mimeMessage2});
		}
		catch (MailSendException ex) {
			assertEquals(sender.transport.getConnectedHost(), "host");
			assertEquals(sender.transport.getConnectedUsername(), "username");
			assertEquals(sender.transport.getConnectedPassword(), "password");
			assertTrue(sender.transport.isCloseCalled());
			assertEquals(1, sender.transport.getSentMessages().size());
			assertEquals(mimeMessage2, sender.transport.getSentMessage(0));
			assertEquals(1, ex.getFailedMessages().size());
			assertEquals(mimeMessage1, ex.getFailedMessages().keySet().iterator().next());
			Object subEx = ex.getFailedMessages().values().iterator().next();
			assertTrue(subEx instanceof MessagingException);
			assertEquals("failed", ((MessagingException) subEx).getMessage());
		}
	}


	private static class MockJavaMailSender extends JavaMailSenderImpl {

		private MockTransport transport;

		protected Transport getTransport(Session session) throws NoSuchProviderException {
			this.transport = new MockTransport(session, null);
			return transport;
		}
	}


	private static class MockTransport extends Transport {

		private String connectedHost = null;
		private int connectedPort = -2;
		private String connectedUsername = null;
		private String connectedPassword = null;
		private boolean closeCalled = false;
		private List sentMessages = new ArrayList();

		public MockTransport(Session session, URLName urlName) {
			super(session, urlName);
		}

		public String getConnectedHost() {
			return connectedHost;
		}

		public int getConnectedPort() {
			return connectedPort;
		}

		public String getConnectedUsername() {
			return connectedUsername;
		}

		public String getConnectedPassword() {
			return connectedPassword;
		}

		public boolean isCloseCalled() {
			return closeCalled;
		}

		public List getSentMessages() {
			return sentMessages;
		}

		public MimeMessage getSentMessage(int index) {
			return (MimeMessage) this.sentMessages.get(index);
		}

		public void connect(String host, int port, String username, String password) throws MessagingException {
			if (host == null) {
				throw new MessagingException("no host");
			}
			this.connectedHost = host;
			this.connectedPort = port;
			this.connectedUsername = username;
			this.connectedPassword = password;
		}

		public synchronized void close() throws MessagingException {
			this.closeCalled = true;
		}

		public void sendMessage(Message message, Address[] addresses) throws MessagingException {
			if ("fail".equals(message.getSubject())) {
				throw new MessagingException("failed");
			}
			List addr1 = Arrays.asList(message.getAllRecipients());
			List addr2 = Arrays.asList(addresses);
			if (!addr1.equals(addr2)) {
				throw new MessagingException("addresses not correct");
			}
			this.sentMessages.add(message);
		}
	}

}
