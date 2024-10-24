package eu.essi_lab.accessor.emodnet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;

public class EMODNETPhysicsClientExternalTestIT {

    @Test
    public void test() throws Exception {
	String endpoint = "https://data-erddap.emodnet-physics.eu/erddap";
	EMODNETPhysicsClient client = new EMODNETPhysicsClient(endpoint);
	List<String> identifiers = client.getIdentifiers();
	System.out.println(identifiers.size());
	EMODNETPhysicsMapper mapper = new EMODNETPhysicsMapper();
	// identifiers.clear();
	// identifiers.add("HFR_WHUB_Total_NRT");
	for (String id : identifiers) {
	    System.out.println(id);
	    JSONObject metadata = client.getMetadata(id);
	    System.out.println(metadata);
	    OriginalMetadata originalMD = new OriginalMetadata();
	    originalMD.setMetadata(metadata.toString());
	    GSResource mapped = mapper.execMapping(originalMD, null);
	    HarmonizedMetadata md = mapped.getHarmonizedMetadata();

	    List<String> realKeywords = new ArrayList<String>();
	    Iterator<Keywords> kIterator = md.getCoreMetadata().getDataIdentification().getKeywords();
	    while (kIterator.hasNext()) {
		Keywords k = (Keywords) kIterator.next();
		Iterator<String> kkIterator = k.getKeywords();
		String type = k.getTypeCode();
		while (kkIterator.hasNext()) {
		    String kk = (String) kkIterator.next();
		    if (type != null) {

		    } else {
			realKeywords.add(kk);
		    }
		}
	    }

	    System.out.println("\n\n");
	    System.out.println("KEYWORDS:");
	    for (String realKeyword : realKeywords) {
		System.out.println(realKeyword);
	    }
	    System.out.println("PARAMETERS:");
	    Iterator<CoverageDescription> coverageIterator = md.getCoreMetadata().getMIMetadata().getCoverageDescriptions();
	    while (coverageIterator.hasNext()) {
		CoverageDescription cd = (CoverageDescription) coverageIterator.next();
		String parameter = cd.getAttributeTitle();
		System.out.println(parameter);
		String parameterId = cd.getAttributeIdentifier();
		System.out.println(parameterId);
	    }
	    System.out.println("INSTRUMENT:");
	    System.out.println("PLATFORM:");
	    System.out.println("ORGANIZATION:");
	    List<ResponsibleParty> parties = md.getCoreMetadata().getDataIdentification().getPointOfContactParty();
	    for (ResponsibleParty party : parties) {
		String org = party.getOrganisationName();
		String orgURI = party.getOrganisationURI();
		System.out.println(org + " " + orgURI);
	    }
	    System.out.println("CRUISE:");
	    System.out.println("PROJECT:");
	    break;
	}
    }

}
