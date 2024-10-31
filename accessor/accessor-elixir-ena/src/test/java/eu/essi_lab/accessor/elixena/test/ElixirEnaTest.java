package eu.essi_lab.accessor.elixena.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.accessor.elixena.ElixirENAConnector;
import eu.essi_lab.lib.net.downloader.Downloader;

public class ElixirEnaTest {

    @Test
    public void test() throws IOException {
	System.out.println("Starting5");
	// DAB-LOW
	String url = "https://www.ebi.ac.uk/ena/portal/api/search?result=read_study&fields=study_accession&format=tsv&query="+ElixirENAConnector.FIRST_STEP_LOW_QUERY_STRING;
	// HIGH
	url = "https://www.ebi.ac.uk/ena/portal/api/search?result=read_study&fields=study_accession&format=tsv&query="+ElixirENAConnector.FIRST_STEP_HIGH_QUERY_STRING;
	// MEDIUM
	url = "https://www.ebi.ac.uk/ena/portal/api/search?result=read_study&fields=study_accession&format=tsv&query="+ElixirENAConnector.FIRST_STEP_MEDIUM_QUERY_STRING;
	// COMBINED
	url = "https://www.ebi.ac.uk/ena/portal/api/search?result=read_study&fields=study_accession&format=tsv&query=(%20tag%3D%22env_tax%3Amarine%22%20AND%20tag!%3D%22env_tax%3Abrackish%22%20AND%20tag!%3D%22env_tax%3Afreshwater%22%20AND%20tag!%3D%22env_tax%3Aterrestrial%22%20AND%20tag!%3D%22env_geo%3Amarine%22%20AND%20tag!%3D%22env_geo%3Acoastal%22%20AND%20tag!%3D%22env_geo%3Afreshwater%22%20AND%20tag!%3D%22env_geo%3Aterrestrial%22%20)%20OR%20(%20tag%3D%22env_tax%3Amarine%22%20AND%20(%20tag%3D%22env_tax%3Abrackish%22%20OR%20tag%3D%22env_tax%3Afreshwater%22%20OR%20tag%3D%22env_tax%3Aterrestrial%22%20)%20AND%20tag%3D%22env_geo%3Amarine%22%20AND%20(%20tag%3D%22env_geo%3Acoastal%22%20OR%20tag%3D%22env_geo%3Afreshwater%22%20OR%20tag%3D%22env_geo%3Aterrestrial%22%20)%20)%20OR%20(%20tag%3D%22env_geo%3Amarine%22%20AND%20tag!%3D%22env_geo%3Acoastal%22%20AND%20tag!%3D%22env_geo%3Afreshwater%22%20AND%20tag!%3D%22env_geo%3Aterrestrial%22%20AND%20tag!%3D%22env_tax%3Amarine%22%20AND%20tag!%3D%22env_tax%3Abrackish%22%20AND%20tag!%3D%22env_tax%3Afreshwater%22%20AND%20tag!%3D%22env_tax%3Aterrestrial%22%20)%20OR%20(%20tag%3D%22env_tax%3Amarine%22%20AND%20tag%3D%22env_geo%3Amarine%22%20)%20AND%20(%20(%20tag!%3D%22env_tax%3Abrackish%22%20AND%20tag!%3D%22env_tax%3Afreshwater%22%20AND%20tag!%3D%22env_tax%3Aterrestrial%22%20)%20OR%20(%20tag!%3D%22env_geo%3Acoastal%22%20AND%20tag!%3D%22env_geo%3Afreshwater%22%20AND%20tag!%3D%22env_geo%3Aterrestrial%22%20)%20)";
	Downloader d = new Downloader();
	Optional<InputStream> stream = d.downloadOptionalStream(url);
	InputStream s = stream.get();
	File tmpFile = File.createTempFile("elixir-ena", ".tsv");
	tmpFile.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(s, fos);
	s.close();
	String filePath = tmpFile.getAbsolutePath();
	HashSet<String> projects = new HashSet<String>();
	try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	    String line;
	    while ((line = br.readLine()) != null) {
		String[] split = line.split("\t");
		projects.add(split[0]);
	    }
	    System.out.println(projects.size());
	    tmpFile.delete();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

}
