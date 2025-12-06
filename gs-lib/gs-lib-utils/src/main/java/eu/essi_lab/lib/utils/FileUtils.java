/**
 *
 */
package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Fabrizio
 */
public class FileUtils {

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
