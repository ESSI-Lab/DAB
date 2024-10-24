package eu.essi_lab.accessor.mgnify;

import java.util.List;

import org.junit.Test;

public class MGnifyClientExternalTestIT {

    @Test
    public void test() throws Exception {
	String endpoint = "https://www.ebi.ac.uk/metagenomics/api/v1/";
	MGnifyClient client = new MGnifyClient(endpoint);
	Pages<SuperStudy> superStudies = client.getSelectedSuperStudies();
	while (superStudies != null) {
	    List<SuperStudy> studies = superStudies.getObjects();
	    for (SuperStudy study : studies) {
		System.out.println("SS: " + study.getUrlSlug() + " - " + study.getTitle());
		Pages<Study> flagships = client.getStudies(study.getFlagshipStudiesLink());
		while (flagships != null) {
		    List<Study> fships = flagships.getObjects();
		    for (Study fship : fships) {
			System.out.println(fship.getName());
//			System.out.println(fship.getCentreName());
//			System.out.println(fship.getJSON().toString());
//			System.out.println(fship.getAccession()+" "+fship.getSecondaryAccession());
			Pages<Sample> samplePages = client.getSamples(fship.getSamplesLink());
			while (samplePages!=null) {
			    List<Sample> samples = samplePages.getObjects();
			    for (Sample sample : samples) {
				System.out.println(sample.getLatitude()+" "+sample.getLongitude());
			    }
			    samplePages = samplePages.getNext();
			}
			Pages<Analysis> analysisPages = client.getAnalyses(fship.getAnalysisLink());
			while (analysisPages!=null) {
			    List<Analysis> analyses = analysisPages.getObjects();
			    for (Analysis ana : analyses) {
				String instrumentPlatform = ana.getInstrumentPlatform();
				String instrumentModel = ana.getInstrumentModel();
				System.out.println(instrumentModel);
				Pages<Download> downloadPages = client.getDownloads(ana.getDownloadsLink());
				while (downloadPages!=null) {
				    List<Download> downloads = downloadPages.getObjects();
				    for (Download download : downloads) {
					System.out.println(download.getAlias()+" "+download.getDescription()+" "+download.getFileFormat()+" "+download.getDownloadLink());
				    }
				    downloadPages = downloadPages.getNext();
				}
			    }
			    analysisPages = analysisPages.getNext();
			}
		    }
		    flagships = flagships.getNext();
		}

	    }
	    superStudies = superStudies.getNext();
	}
	// Pages<Biome> biomes = client.getBiomes();
	// while (biomes != null) {
	// for (Biome biome : biomes.getObjects()) {
	// System.out.println(biome.getId());
	// }
	// biomes = biomes.getNext();
	// }
    }

}
