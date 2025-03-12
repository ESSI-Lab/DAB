package csw.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;

public class TestDiffDownload {

    public static void main(String[] args) throws Exception {
	String filePath = "/home/boldrini/diffs/wa-requests";
	String folderPath = "/home/boldrini/diffs/";
	File folder = new File(folderPath);
	try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	    String line;
	    while ((line = reader.readLine()) != null) {
		if (line.isEmpty()) {
		    continue;
		}
		line = line.trim();
		File file1 = null;
		try {
		    file1 = downloadURL(line, folder, "ID" + line.hashCode() + "-orig");
		} catch (Exception e) {
		    System.out.println("error downloading " + line);
		    System.exit(1);
		}
		String line2 = line.replace("http://whos.geodab.eu", "http://localhost:9090");
		File file2 = null;
		try {
		    file2 = downloadURL(line2, folder, "ID" + line.hashCode() + "-local");
		} catch (Exception e) {
		    System.out.println("error downloading " + line2);
		    System.exit(1);
		}

		if (file1.length() > 0 && file1.length() == file2.length()) {
		    file1.delete();
		    file2.delete();
		} else {
		    System.out.println("SIZE MISMATCH "+file1.getName());
		    System.out.println(line);
		    System.out.println(line2);
		}

	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static File downloadURL(String line, File folder, String name) throws Exception {
	System.out.println("Downloading " + line + " to " + name);
	Downloader d = new Downloader();
	Optional<InputStream> stream = d.downloadOptionalStream(line);
	File file = new File(folder, name);
	FileOutputStream fos = new FileOutputStream(file);
	InputStream s = stream.get();
	IOUtils.copy(s, fos);
	s.close();
	fos.close();
	return file;

    }
}
