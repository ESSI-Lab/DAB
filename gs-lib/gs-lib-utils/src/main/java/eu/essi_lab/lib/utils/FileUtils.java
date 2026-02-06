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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
