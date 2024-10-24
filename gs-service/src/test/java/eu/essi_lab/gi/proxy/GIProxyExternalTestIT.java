package eu.essi_lab.gi.proxy;

import static org.junit.Assert.fail;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.smtp.GmailClient;

public class GIProxyExternalTestIT {

    private String[] getEmailsForReport() {
	return System.getProperty("report.emails").split(",");
    }

    @Test
    public void test() throws AddressException, MessagingException {
	String issue = "";
	try {
	    String str = System.getProperty("system.proxy.url")+"/get?url=https://google.com";
	    Downloader d = new Downloader();
	    String response = d.downloadOptionalString(str).get();
	    if (response.contains("Not allowed host: google.com")) {
		// fine!
		return;
	    }
	    issue = "security issue.. it shouldn't be allowed to retrieve this page: "+str;
	} catch (Exception e) {
	    issue = e.getMessage() + " " + e.getStackTrace().toString();
	}
	sendEmailReport(issue);
	fail(issue);

    }

    protected void sendEmailReport(String message) throws AddressException, MessagingException {
	String[] emails = getEmailsForReport();
	if (emails.length > 0) {
	    File localFile = new File("/home/boldrini");
	    if (localFile.exists()) {
		System.out.println("not sending report, as it is manually launched from developer machine");
		return;
	    }
	    GmailClient client = new GmailClient();
	    client.send("[GI-PROXY] GI-proxy is not working fine!",
		    "[GI-PROXY] GI-proxy is not working fine!\n"+System.getProperty("system.proxy.url")+"\n" + message, emails);

	}
    }

}
