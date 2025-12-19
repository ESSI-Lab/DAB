package eu.essi_lab.lib.net.smtp.test;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.junit.Test;

import eu.essi_lab.lib.net.smtp.SMTPClient;

public class SMTPClientExternalTestIT {

    @Test
    public void test() throws AddressException, MessagingException {
	SMTPClient client = new SMTPClient();
	client.send("[TEST]", "just a test", System.getProperty("test.email"));
    }
}
