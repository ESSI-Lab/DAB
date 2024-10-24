/**
 * 
 */
package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

/**
 * @author Fabrizio
 */
public class IOStreamUtils {

	/**
	 * Default value is 2048.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 2048;

	/**
	 * As from http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html
	 */
	private static final int[] PNG_START_BYTES_AS_DECIMAL = new int[] { 137, 80, 78, 71, 13, 10, 26, 10 };
	/**
	 * As from http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html
	 */
	private static int[] JPEG_START_BYTES_AS_DECIMAL = new int[] { Integer.parseInt("FF", 16),
			Integer.parseInt("D8", 16) };
	/**
	 * As from http://www.libpng.org/pub/png/spec/1.2/PNG-Structure.html
	 */
	private static final int[] TIF_START_BYTES_AS_DECIMAL = new int[] { Integer.parseInt("4d", 16),
			Integer.parseInt("4d", 16), Integer.parseInt("00", 16), Integer.parseInt("2a", 16) };

	/**
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static String asUTF8String(InputStream stream) throws IOException {

		byte[] bytes = getBytes(stream);

		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * @return a byte[] containing the information contained in the specified
	 *         InputStream.
	 * @throws java.io.IOException
	 */
	public static byte[] getBytes(InputStream input) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		copy(input, result);
		result.close();
		return result.toByteArray();
	}

	/**
	 * Copies information from the input stream to the output stream using a default
	 * buffer size of 2048 bytes.
	 * 
	 * @throws java.io.IOException
	 */
	public static void copy(InputStream input, OutputStream output) throws IOException {
		copy(input, output, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies information from the input stream to the output stream using the
	 * specified buffer size
	 * 
	 * @throws java.io.IOException
	 */
	public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int bytesRead = input.read(buf);
		while (bytesRead != -1) {
			output.write(buf, 0, bytesRead);
			bytesRead = input.read(buf);
		}
		output.flush();
	}

	/**
	 * @param string
	 * @return
	 */
	public static InputStream asStream(String string) {

		return new ByteArrayInputStream(string.getBytes(Charsets.UTF_8));
	}

	/**
	 * @param stream
	 * @return
	 */
	public static boolean isPNG(InputStream stream) {

		return checkStreamFirstBytes(stream, PNG_START_BYTES_AS_DECIMAL);
	}

	/**
	 * @param stream
	 * @return
	 */
	public static boolean isJPEG(InputStream stream) {

		return checkStreamFirstBytes(stream, JPEG_START_BYTES_AS_DECIMAL);
	}

	/**
	 * @param stream
	 * @return
	 */
	public static boolean isTIF(InputStream stream) {

		return checkStreamFirstBytes(stream, TIF_START_BYTES_AS_DECIMAL);
	}

	/**
	 * 
	 * @return
	 */
	public static File getUserTempDirectory() {

		return new File(System.getProperty("java.io.tmpdir"));
	}

	/**
	 * @param stream
	 * @param prefix
	 * @param postfix
	 * @return
	 * @throws IOException
	 */
	public static File tempFilefromStream(InputStream stream, String prefix, String postfix) throws IOException {

		File tmpFile = File.createTempFile(prefix, postfix);
		tmpFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(stream, fos);
		stream.close();
		fos.close();
		return tmpFile;
	}

	/**
	 * @param is
	 * @param array
	 * @return
	 */
	private static boolean checkStreamFirstBytes(InputStream is, int[] array) {

		byte[] b = new byte[array.length];

		try {

			is.read(b);

			for (int i = 0; i < array.length; i++) {

				if ((b[i] & 0xff) != array[i]) {
					return false;
				}
			}
		} catch (Exception e) {
			GSLoggerFactory.getLogger(IOStreamUtils.class).error(e.getMessage(), e);
			return false;
		}

		return true;
	}

}
