/**
 * 
 */
package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;

/**
 * @author Fabrizio
 */
public class StringUtils {

    /**
     * 
     */
    public static final String SHA1_IDENTIFIER = "SHA-1";

    public static final String SHA256_IDENTIFIER = "SHA-256";

    /**
     * 
     */
    public static String format(int value) {

	return format((double) value);
    }

    /**
     * 
     */
    public static String format(double value) {

	DecimalFormat decimalFormat = new DecimalFormat();
	decimalFormat.setGroupingSize(3);
	decimalFormat.setGroupingUsed(true);
	decimalFormat.setMaximumFractionDigits(3);

	DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	symbols.setGroupingSeparator('.'); // grouping separator is , for English countries
	symbols.setDecimalSeparator(','); // decimal separator is . for English countries
	decimalFormat.setDecimalFormatSymbols(symbols);

	return decimalFormat.format(value);
    }

    /**
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String hashSHA1messageDigest(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	return hashMessageDigest(SHA1_IDENTIFIER, value);
    }

    public static String hashSHA256messageDigest(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	return hashMessageDigest(SHA256_IDENTIFIER, value);
    }

    public static String hashMessageDigest(String algorithm, String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	MessageDigest msdDigest = MessageDigest.getInstance(algorithm);
	msdDigest.update(value.getBytes(StandardCharsets.UTF_8), 0, value.length());
	char[] ret = Hex.encodeHex(msdDigest.digest());
	return new String(ret).toUpperCase();
    }

    public static Long hashSHA1messageDigestLong(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	return hashMessageDigestLong(SHA1_IDENTIFIER, value);
    }

    public static Long hashSHA256messageDigestLong(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	return hashMessageDigestLong(SHA256_IDENTIFIER, value);
    }

    public static Long hashMessageDigestLong(String algorithm, String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	MessageDigest msdDigest = MessageDigest.getInstance(algorithm);
	msdDigest.update(value.getBytes("UTF-8"), 0, value.length());
	byte[] hashBytes = msdDigest.digest();
	return ByteBuffer.wrap(hashBytes).getLong();

    }

    /**
     *
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String toUUID(String value){

 	try {
	    MessageDigest digest = MessageDigest.getInstance("SHA-256");

	    byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

	    ByteBuffer bb = ByteBuffer.wrap(hash, 0, 16);
	    long high = bb.getLong();
	    long low = bb.getLong();

	    UUID uuid = new UUID(high, low);
	    return uuid.toString();

	} catch (NoSuchAlgorithmException e) {

	    throw new RuntimeException(e);
	}
    }

    /**
     * URL encodes the provided string with UTF-8 using {@link StandardCharsets} defined as "Constant definitions for
     * the
     * standard Charsets. These charsets are guaranteed to be available on every implementation of the Java platform".
     * 
     * @param string
     */
    public static String URLEncodeUTF8(String string) {

	try {
	    return URLEncoder.encode(string, StandardCharsets.UTF_8.name());
	} catch (UnsupportedEncodingException e) {
	}
	return null;// no way
    }

    public static String normalize(String input) {
	return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD);
    }

    public static String removeAccents(String input) {
	return normalize(input).replaceAll("\\p{M}", "");
    }

    /**
     * URL decodes the provided string with UTF-8 using {@link StandardCharsets} defined as "Constant definitions for
     * the
     * standard Charsets. These charsets are guaranteed to be available on every implementation of the Java platform".
     * 
     * @param string
     */
    public static String URLDecodeUTF8(String string) {

	try {
	    return URLDecoder.decode(string, StandardCharsets.UTF_8.name());
	} catch (UnsupportedEncodingException e) {
	}
	return null;// no way
    }

    /**
     * Encodes the provided string with UTF-8 using {@link StandardCharsets} defined as "Constant definitions for the
     * standard Charsets. These charsets are guaranteed to be available on every implementation of the Java platform".
     * 
     * @param string
     */
    public static String encodeUTF8(String string) {

	return new String(string.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes the provided string with UTF-8 using {@link StandardCharsets} defined as "Constant definitions for the
     * standard Charsets. These charsets are guaranteed to be available on every implementation of the Java platform".
     * 
     * @param string
     */
    public static String decodeUTF8(String string) {

	try {
	    return URLDecoder.decode(string, StandardCharsets.UTF_8.name());
	} catch (UnsupportedEncodingException e) {
	}

	return null;// no way
    }

    /**
     * Removes also the trailing non-breakable space (NBSP) discarding the ones inside the string (if any).<br>
     * Reference to this solution: https://stackoverflow.com/questions/28295504/how-to-trim-no-break-space-in-java
     * Removes also the BOM character https://stackoverflow.com/questions/21891578/removing-bom-characters-using-java
     * 
     * @param string
     * @return
     */
    public static String trimNBSP(String string) {

	return string.replace('\u00A0', ' ').replace('\u2007', ' ').replace('\u202F', ' ').replace('\uFEFF', ' ').trim();
    }

    /**
     * Returns true if the string is null or equals to "null"
     * 
     * @param string
     * @return
     */
    public static boolean isNull(String string) {

	return string == null || string.equals("null");
    }

    /**
     * Avoid the use of the '!' operator to negate the {@link #isNull(String)} method
     * 
     * @param string
     * @return
     */
    public static boolean isNotNull(String string) {

	return !isNull(string);
    }

    /**
     * Returns true if the string is <code>null</code> or {@link String#isEmpty()} returns true
     * 
     * @param string
     * @return
     */
    public static boolean isEmpty(String string) {

	return string == null || string.isEmpty();
    }

    /**
     * Avoid the use of the '!' operator to negate the {@link #isEmpty(String)} method
     * 
     * @param string
     * @return
     */
    public static boolean isNotEmpty(String string) {

	return !isEmpty(string);
    }

    /**
     * Avoid the use of the '!' operator to negate the {@link String#isBlank()} method
     * 
     * @param string
     * @return
     */
    public static boolean isNotBlank(String string) {

	return !string.isBlank();
    }

    /**
     * Both methods {@link #isNotEmpty(String)} and {@link #isNotNull(String)} are applied
     * 
     * @param string
     * @return
     */
    public static boolean isNotEmptyAndNotNull(String string) {

	return isNotNull(string) && isNotEmpty(string);
    }

    /**
     * Both methods {@link #isNotBlank(String)} and {@link #isNotNull(String)} are applied
     * 
     * @param string
     * @return
     */
    public static boolean isNotBlankAndNotNull(String string) {

	return isNotNull(string) && isNotBlank(string);
    }

    /**
     * Returns <code>true</code> if the given <code>string</code> {@link #isNotNull(String)},
     * {@link #isNotEmpty(String)} and {@link #isNotBlank(String)}
     * 
     * @param string
     * @return
     */
    public static boolean isReadable(String string) {

	return isNotNull(string) && isNotEmpty(string) && isNotBlank(string);
    }

    /**
     * @param string
     * @return
     */
    public static boolean hasOnlyLettersNumbersAndUnderscores(String string) {

	Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]*$");

	Matcher matcher = pattern.matcher(string);
	return matcher.find();
    }

    /**
     * Regular expression:<br>
     * ^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
     * 
     * @param value
     * @return
     */
    public static boolean isUUID(String value) {

	try {
	    UUID.fromString(value);
	    return true;
	} catch (IllegalArgumentException e) {
	}

	return false;
    }
}
