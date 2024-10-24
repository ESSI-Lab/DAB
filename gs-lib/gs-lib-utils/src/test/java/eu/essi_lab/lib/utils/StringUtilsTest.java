package eu.essi_lab.lib.utils;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void test() throws NoSuchAlgorithmException, UnsupportedEncodingException {
	String hash = StringUtils.hashSHA1messageDigest("");
	System.out.println(hash);
	assertEquals("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709", hash);
	
	hash = StringUtils.hashSHA1messageDigest("pippo");
	System.out.println(hash);
	assertEquals("D012F68144ED0F121D3CC330A17EEC528C2E7D59", hash);
    }

}
