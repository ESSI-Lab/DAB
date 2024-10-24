package eu.essi_lab.wrapper.marklogic.ssl.test;

import java.net.URI;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.Transaction;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.SecurityOptions;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class MarkLogicWrapperSSLExternalTestIT {

    /**
     * 
     */
    // private static final String[] PROTOCOLS = { "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" };
    private static final String[] PROTOCOLS = new String[] { "TLSv1.3" };

    static {

	// System.setProperty("javax.net.debug", "all");

	// Provider[] providers = Security.getProviders();
	//
	// for (int i = 0; i < providers.length; i++) {
	//
	// Provider provider = providers[i];
	// String info = provider.getInfo();S
	// System.out.println(info);
	// }
    }

    @Test
    public void xccsTest() {

	boolean xccsTest = false;

	for (int i = 0; i < PROTOCOLS.length; i++) {

	    xccsTest |= xccsTest(false, PROTOCOLS, PROTOCOLS[i], false, false);
	    // xccsTest |= xccsTest(false, PROTOCOLS, PROTOCOLS[i], false, true);
	    // xccsTest |= xccsTest(false, PROTOCOLS, PROTOCOLS[i], true, false);
	    // xccsTest |= xccsTest(false, PROTOCOLS, PROTOCOLS[i], true, true);
	    //
	    // xccsTest |= xccsTest(true, PROTOCOLS, PROTOCOLS[i], false, false);
	    // xccsTest |= xccsTest(true, PROTOCOLS, PROTOCOLS[i], false, true);
	    // xccsTest |= xccsTest(true, PROTOCOLS, PROTOCOLS[i], true, false);
	    // xccsTest |= xccsTest(true, PROTOCOLS, PROTOCOLS[i], true, true);
	}

	Assert.assertTrue(xccsTest);
    }

    @Test
    public void restTest() {

	boolean restTest = false;

	for (int i = 0; i < PROTOCOLS.length; i++) {

	    restTest |= restTest(false, PROTOCOLS, PROTOCOLS[i], false, false);
	    // restTest |= restTest(false, PROTOCOLS, PROTOCOLS[i], false, true);
	    // restTest |= restTest(false, PROTOCOLS, PROTOCOLS[i], true, false);
	    // restTest |= restTest(false, PROTOCOLS, PROTOCOLS[i], true, true);
	    //
	    // restTest |= restTest(true, PROTOCOLS, PROTOCOLS[i], false, false);
	    // restTest |= restTest(true, PROTOCOLS, PROTOCOLS[i], false, true);
	    // restTest |= restTest(true, PROTOCOLS, PROTOCOLS[i], true, false);
	    // restTest |= restTest(true, PROTOCOLS, PROTOCOLS[i], true, true);
	}

	Assert.assertTrue(restTest);
    }

    /**
     * @param setProtocolsSystemProperty
     * @param protocols
     * @param protocol
     * @param enableProtocol
     * @param enableSuite
     * @return
     */
    private boolean xccsTest(//
	    boolean setProtocolsSystemProperty, //
	    String[] protocols, //
	    String protocol, //
	    boolean enableProtocol, //
	    boolean enableSuite) {

	if (setProtocolsSystemProperty) {

	    System.setProperty("https.protocols", Arrays.asList(protocols).stream().collect(Collectors.joining(",")));

	} else {

	    System.setProperty("https.protocols", "");
	}

	//
	//
	//
	try {

//	    SecurityOptions securityOptions = createSecurityOptions(protocol, null, enableProtocol, enableSuite);
	    SecurityOptions securityOptions = createTrustAllSecurityOptions(protocol);

	    String xcssUri = "xccs://" + //
		    System.getProperty("dBUser") + ":" + //
		    System.getProperty("dBPassword") + "@" + //
		    System.getProperty("dBHost") + ":" + //
		    System.getProperty("dBXDBCPort") + "/" + //
		    System.getProperty("dBName");


	    
	    ContentSource contentSource = ContentSourceFactory.newContentSource(new URI(xcssUri));

	    String xquery = "let $x := 3 return $x";

	    ResultSequence response = submit(contentSource, xquery);

	    String result = response.asString();

	    return result.equals("3");

	} catch (Exception ex) {
ex.printStackTrace();
	    return false;
	}
    }



    /**
     * @param setProtocolsSystemProperty
     * @param user
     * @param password
     * @throws Exception
     */
    private boolean restTest(//

	    boolean setProtocolsSystemProperty, //
	    String[] protocols, //
	    String protocol, //
	    boolean enableProtocol, //
	    boolean enableSuite) {

	if (setProtocolsSystemProperty) {

	    System.setProperty("https.protocols", Arrays.asList(protocols).stream().collect(Collectors.joining(",")));

	} else {

	    System.setProperty("https.protocols", "");
	}

	try {

	    SecurityOptions securityOptions = createSecurityOptions(protocol, null, enableProtocol, enableSuite);

	    DigestAuthContext digestAuthContext = new DigestAuthContext(System.getProperty("dBUser"), System.getProperty("dBPassword"));

	    digestAuthContext.withSSLContext(securityOptions.getSslContext(), createTrustManager());

	    DatabaseClient databaseClient = DatabaseClientFactory.newClient(//

		    System.getProperty("dBHost"), //
		    Integer.valueOf(System.getProperty("dBRESTPort")), //

		    System.getProperty("dBName"), //
		    digestAuthContext);

	    JSONObject jsonObject = new JSONObject();
	    jsonObject.put("key", "value");

	    storeJSON(databaseClient, "test", jsonObject);

	    return true;

	} catch (Exception ex) {

	    // GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());

	    return false;
	}
    }

    /**
     * @param databaseClient
     * @param uri
     * @param object
     * @return
     * @throws Exception
     */
    private boolean storeJSON(DatabaseClient databaseClient, String uri, JSONObject object) throws Exception {

	Transaction transaction = openTransaction(databaseClient);
	JSONDocumentManager jsonManager = databaseClient.newJSONDocumentManager();

	try {

	    DocumentDescriptor exists = jsonManager.exists(uri, transaction);
	    boolean out = false;

	    if (exists == null) { // if the doc no not exists

		JSONWriteHandle domHandle = new StringHandle(object.toString(3));

		jsonManager.write(uri, domHandle, transaction);

		out = true;
	    }

	    transaction.commit();

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);

	    transaction.rollback();

	    throw ex;
	}
    }

    /**
     * @param databaseClient
     * @return
     */
    private Transaction openTransaction(DatabaseClient databaseClient) {
	try {
	    Transaction ret = databaseClient.openTransaction(ISO8601DateTimeUtils.getISO8601DateTime(), 60);
	    return ret;
	} catch (Throwable e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw e;
	}
    }

    /**
     * @param xquery
     * @param requestID
     * @return
     * @throws RequestException
     */
    private ResultSequence submit(ContentSource contentSource, String xquery) throws RequestException {

	Session session = contentSource.newSession();

	AdhocQuery request = session.newAdhocQuery(xquery);

	ResultSequence result = null;
	try {

	    result = session.submitRequest(request);

	} catch (Throwable t) {

	    GSLoggerFactory.getLogger(getClass()).error(t.getMessage());

	    throw t;

	} finally {

	    session.close();
	}

	return result;
    }

    /**
     * @return
     */
    private X509TrustManager createTrustManager() {

	return new X509TrustManager() {

	    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
	    }

	    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
	    }

	    public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[] {};
	    }
	};
    }

    private SecurityOptions createTrustAllSecurityOptions(String protocol) throws Exception {
	        TrustManager[] trust = new TrustManager[] { new X509TrustManager() {
	            public X509Certificate[] getAcceptedIssuers() {
	                return new X509Certificate[0];
	            }

	            /**
	             * @throws CertificateException
	             */
	            public void checkClientTrusted(X509Certificate[] certs,
	                    String authType) throws CertificateException {
	                // no exception means it's okay
	            }

	            /**
	             * @throws CertificateException
	             */
	            public void checkServerTrusted(X509Certificate[] certs,
	                    String authType) throws CertificateException {
	                // no exception means it's okay
	            }
	        } };

	        SSLContext sslContext = SSLContext.getInstance(protocol);
	        sslContext.init(null, trust, null);
	        return new SecurityOptions(sslContext);
    }
    
    /**
     * @param protocol
     * @param suite
     * @param enableProtocol
     * @param enableSuite
     * @return
     * @throws Exception
     */
    private SecurityOptions createSecurityOptions(String protocol, String suite, boolean enableProtocol, boolean enableSuite)
	    throws Exception {

	TrustManager[] trust = new TrustManager[] { createTrustManager() };

	Provider provider = Arrays.asList(Security.getProviders()).//
		stream().//
		filter(p -> p.getName().equals("SunJSSE")).//
		findFirst().//
		get();

	SSLContext sslContext = SSLContext.getInstance(protocol);

	sslContext.init(null, trust, null);

	SecurityOptions securityOptions = new SecurityOptions(sslContext);

	if (enableProtocol) {
	    securityOptions.setEnabledProtocols(new String[] { protocol });
	}

	// if (enableSuite) {
	// securityOptions.setEnabledCipherSuites(new String[] { "TLS_AES_128_GCM_SHA256" });
	// }

	return securityOptions;
    }
}
