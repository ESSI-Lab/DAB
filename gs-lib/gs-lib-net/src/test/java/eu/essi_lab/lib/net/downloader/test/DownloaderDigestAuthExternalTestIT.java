package eu.essi_lab.lib.net.downloader.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;

/**
 * It works. If required, the handshake mechanism can be implemented in the {@link Downloader}
 * 
 * @author Fabrizio
 */
public class DownloaderDigestAuthExternalTestIT {

    /**
     * https://en.wikipedia.org/wiki/Digest_access_authentication
     * https://github.com/ron190/jsql-injection/blob/master/model/src/main/java/com/jsql/util/DigestUtil.java
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void digestAuthTest() throws IOException, InterruptedException, URISyntaxException, NoSuchAlgorithmException {

	Downloader downloader = new Downloader();

	String uri = "https://postman-echo.com/digest-auth";

	String user = "postman";
	String password = "password";

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, uri);

	HttpResponse<InputStream> response = downloader.downloadResponse(request);

	assertEquals(401, response.statusCode());

	//
	//
	//

	HttpHeaders headers = response.headers();

	String authenticate = headers.firstValue("www-authenticate").get();

	System.out.println("\nSERVER-DIGEST");
	System.out.println(authenticate);
	System.out.println("SERVER-DIGEST\n");

	authenticate = authenticate.replace("Digest", "");
	authenticate = authenticate.replace("digest", "");
	authenticate = authenticate.replace("\"", "");

	String opaque = findValue(authenticate, "opaque").orElse("");
	String realm = findValue(authenticate, "realm").get();
	String nonce = findValue(authenticate, "nonce").get();

	String path = new URL(uri).getFile();

	String digestHeader = buildDigestHeader(path, user, password, opaque, realm, nonce);

	System.out.println("\nCLIENT-DIGEST");
	System.out.println(digestHeader);
	System.out.println("CLIENT-DIGEST\n");

	HashMap<String, String> reqHeaders = new HashMap<String, String>();
	reqHeaders.put("Authorization", digestHeader);

	response = downloader.downloadResponse(uri, HttpHeaderUtils.build(reqHeaders));

	assertEquals(200, response.statusCode());
    }

    /**
     * @param uri
     * @param user
     * @param password
     * @param opaque
     * @param realm
     * @param nonce
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    private String buildDigestHeader(//
	    String uri, //
	    String user, //
	    String password, //
	    String opaque, //
	    String realm, //
	    String nonce) throws UnsupportedEncodingException, NoSuchAlgorithmException {

	String cnonce = getClientNonce();

	String nc = "00000001";

	String qop = "auth";

	//
	//
	//

	String ha1 = DigestUtils.md5Hex(String.format("%s:%s:%s", user, realm, password));

	String ha2 = DigestUtils.md5Hex(String.format("%s:%s", "GET", uri));

	String response = DigestUtils.md5Hex(String.format("%s:%s:%s:%s:%s:%s", ha1, nonce, nc, cnonce, qop, ha2));

	//
	//
	//

	String digestHeader = null;

	if (opaque.isEmpty()) {

	    digestHeader = String.format("Digest " + //
		    "username=\"%s\"," + //
		    "realm=\"%s\"," + //
		    "nonce=\"%s\"," + //
		    "uri=\"%s\"," + //
		    "response=\"%s\"," + //
		    "qop=\"%s\"," + //
		    "nc=\"%s\"," + //
		    "cnonce=\"%s\"", user, //
		    realm, //
		    nonce, //
		    uri, //
		    response, //
		    qop, //
		    nc, //
		    cnonce

	    );
	} else {

	    digestHeader = String.format("Digest " + //
		    "username=\"%s\"," + //
		    "realm=\"%s\"," + //
		    "nonce=\"%s\"," + //
		    "uri=\"%s\"," + //
		    "response=\"%s\"," + //
		    "opaque=\"%s\"," + //
		    "qop=\"%s\"," + //
		    "nc=\"%s\"," + //
		    "cnonce=\"%s\"", user, //
		    realm, //
		    nonce, //
		    uri, //
		    response, //
		    opaque, //
		    qop, //
		    nc, //
		    cnonce
	    );
	}

	return digestHeader;
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private String getClientNonce() throws NoSuchAlgorithmException, UnsupportedEncodingException {

	SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	random.setSeed(System.currentTimeMillis());
	byte[] nonceBytes = new byte[8];
	random.nextBytes(nonceBytes);

	return new String(Base64.encodeBase64(nonceBytes), "UTF-8");
    }

    /**
     * /**
     * 
     * @param target
     * @param key
     * @return
     */
    private Optional<String> findValue(String target, String key) {

	return Arrays.asList(target.split(",")).

		stream().map(v -> v.trim()).filter(v -> v.startsWith(key)).map(v -> v.replace(key + "=", "")).findFirst();

    }

}
