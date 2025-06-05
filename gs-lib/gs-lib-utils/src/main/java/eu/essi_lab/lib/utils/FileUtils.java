/**
 * 
 */
package eu.essi_lab.lib.utils;

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
    public static File createTempDir(String child, boolean makeDir) {

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
    public static File createTempDir(boolean makeDir) {

	File file = new File(System.getProperty("java.io.tmpdir"));

	if (!file.exists() && makeDir) {

	    boolean mkdirs = file.mkdirs();

	    System.out.println(mkdirs);
	}

	return file;
    }
}
