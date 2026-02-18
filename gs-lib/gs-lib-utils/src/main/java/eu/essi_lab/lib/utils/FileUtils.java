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

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Fabrizio
 */
public class FileUtils {

    private static final Set<String> RESERVED_NAMES = Set.of(//
	    "CON",//
	    "PRN",//
	    "AUX",//
	    "NUL",//
	    "COM1",//
	    "COM2",//
	    "COM3",//
	    "COM4",//
	    "COM5",//
	    "COM6",//
	    "COM7",//
	    "COM8",//
	    "COM9",//
	    "LPT1",//
	    "LPT2",//
	    "LPT3",//
	    "LPT4",//
	    "LPT5",//
	    "LPT6",//
	    "LPT7",//
	    "LPT8",//
	    "LPT9");

    /**
     * @param trustStoreFile
     * @param trustStorePwd
     */
    public static void printTrustStoreCertificates(File trustStoreFile, String trustStorePwd) {

	try {
	    KeyStore ks = KeyStore.getInstance("PKCS12");

	    GSLoggerFactory.getLogger(FileUtils.class).trace("Reading certificates");

	    try (FileInputStream fis = new FileInputStream(trustStoreFile)) {

		ks.load(fis, trustStorePwd.toCharArray());
	    }

	    List<String> aliases = StreamUtils.iteratorToStream(ks.aliases().asIterator()).toList();
	    int i = 0;

	    GSLoggerFactory.getLogger(FileUtils.class).trace("Found {} certificates", aliases.size());

	    for (String alias : aliases) {

		String type = (ks.isCertificateEntry(alias) ? "trustedCertEntry" : "keyEntry");
		GSLoggerFactory.getLogger(FileUtils.class).info("{}: {} {}", ++i, alias, type);

		Certificate cert = ks.getCertificate(alias);

		if (cert instanceof X509Certificate x) {

		    GSLoggerFactory.getLogger(FileUtils.class).trace("{} {}", x.getSubjectX500Principal(), x.getIssuerX500Principal());

		    byte[] encoded = x.getEncoded();
		    MessageDigest md = MessageDigest.getInstance("SHA-256");

		    byte[] digest = md.digest(encoded);

		    // convert to hex with colon separators
		    StringBuilder hex = new StringBuilder();

		    for (int h = 0; h < digest.length; h++) {
			hex.append(String.format("%02X", digest[h]));
			if (h < digest.length - 1)
			    hex.append(":");
		    }

		    GSLoggerFactory.getLogger(FileUtils.class).trace("SHA-256 Fingerprint: " + hex.toString());

		    GSLoggerFactory.getLogger(FileUtils.class).trace("-----");
		}
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(FileUtils.class).error(e);
	}
    }

    /**
     * @param path
     * @return
     */
    public static String sanitizeForNtfs(String value) {

	String sanitized = value.replaceAll("[\\\\/:*?\"<>|]", "_");

	sanitized = sanitized.replaceAll("[\\. ]+$", "");

	String baseName = sanitized.contains(".") ? sanitized.substring(0, sanitized.indexOf('.')) : sanitized;

	if (RESERVED_NAMES.contains(baseName.toUpperCase())) {

	    sanitized = "_" + sanitized;
	}

	return sanitized;
    }

    /**
     * @param folder
     * @param includeRoot
     * @throws IOException
     */
    public static void clearFolder(File folder, boolean includeRoot) throws IOException {

	Predicate<Path> p = includeRoot ? f -> true : f -> !f.toFile().getAbsolutePath().equals(folder.getAbsolutePath());

	try (Stream<Path> paths = Files.walk(folder.toPath())) {
	    paths.sorted(Comparator.reverseOrder()).//
		    filter(p).//
		    map(Path::toFile).//
		    forEach(File::delete);
	}
    }

    /**
     * @param child
     * @param makeDir
     * @return
     */
    public static File getTempDir(String child, boolean makeDir) {

	File file = new File(System.getProperty("java.io.tmpdir"), child);

	if (!file.exists() && makeDir) {

	    file.mkdirs();
	}

	return file;
    }

    /**
     * @param makeDir
     * @return
     */
    public static File getTempDir() {

	return new File(System.getProperty("java.io.tmpdir"));
    }
}
