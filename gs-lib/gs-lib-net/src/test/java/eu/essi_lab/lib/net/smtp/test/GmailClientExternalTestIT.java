package eu.essi_lab.lib.net.smtp.test;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.junit.Test;

import eu.essi_lab.lib.net.smtp.GmailClient;

public class GmailClientExternalTestIT {

	@Test
	public void test() throws AddressException, MessagingException {
		GmailClient client = new GmailClient();
		client.send("[TEST]", "just a test", System.getProperty("test.email"));
	}

}
