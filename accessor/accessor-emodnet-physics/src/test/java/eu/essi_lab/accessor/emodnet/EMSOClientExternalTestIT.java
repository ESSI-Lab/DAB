package eu.essi_lab.accessor.emodnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;

public class EMSOClientExternalTestIT {

	@Test
	public void test() throws Exception {
		String endpoint = "https://erddap.emso.eu/erddap";
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
			Iterator<CoverageDescription> coverageIterator = md.getCoreMetadata().getMIMetadata()
					.getCoverageDescriptions();
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

	@Test
	public void testBBOX() throws Exception {
		String endpoint = "https://erddap.emso.eu/erddap";
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
			GeographicBoundingBox bbox = md.getCoreMetadata().getDataIdentification().getGeographicBoundingBox();
			if (bbox == null) {
				GSLoggerFactory.getLogger(getClass()).error("missing bbox for {}", id);
			}
		}

	}

	@Test
	public void testOrganizations() throws Exception {
		String endpoint = "https://erddap.emso.eu/erddap";
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
			List<ResponsibleParty> list = md.getCoreMetadata().getDataIdentification().getPointOfContactParty();
			for (ResponsibleParty org : list) {
				String uri = org.getOrganisationURI();
				System.out.println(id + " = " + uri);
			}
		}
	}


	@Test
	public void testInstruments() throws Exception {
		String endpoint = "https://erddap.emso.eu/erddap";
		EMODNETPhysicsClient client = new EMODNETPhysicsClient(endpoint);
		List<String> identifiers = client.getIdentifiers();
		System.out.println(identifiers.size());
		EMODNETPhysicsMapper mapper = new EMODNETPhysicsMapper();
		HashSet<String>titles = new HashSet<String>();
		HashSet<String>uris = new HashSet<String>();
		for (String id : identifiers) {
			System.out.println(id);
			JSONObject metadata = client.getMetadata(id);
			System.out.println(metadata);
			OriginalMetadata originalMD = new OriginalMetadata();
			originalMD.setMetadata(metadata.toString());
			GSResource mapped = mapper.execMapping(originalMD, null);
			HarmonizedMetadata md = mapped.getHarmonizedMetadata();
			Iterator<MIInstrument> iterator = md.getCoreMetadata().getMIMetadata().getMIInstruments();
			while (iterator.hasNext()) {
				MIInstrument miInstrument = (MIInstrument) iterator.next();
				String title = miInstrument.getTitle();
				System.out.println(title);
				titles.add(title);
				String uri= miInstrument.getMDIdentifierCode();
				System.out.println(uri);
				uris.add(uri);
			}
		}
		for (String title : titles) {
			System.out.println(title);
		}
		System.out.println();
		for (String title : uris) {
			System.out.println(title);
		}
	}
	
	@Test
	public void testConventions() throws Exception {
		String endpoint = "https://erddap.emso.eu/erddap";
		EMODNETPhysicsClient client = new EMODNETPhysicsClient(endpoint);
		List<String> identifiers = client.getIdentifiers();
		System.out.println(identifiers.size());
		EMODNETPhysicsMapper mapper = new EMODNETPhysicsMapper();
		// identifiers.clear();
		// identifiers.add("HFR_WHUB_Total_NRT");
		HashSet<String> conventions = new HashSet<>();
		for (String id : identifiers) {
			System.out.println(id);
			JSONObject metadata = client.getMetadata(id);
			JSONArray rows = metadata.getJSONObject("table").getJSONArray("rows");
			for (int i = 0; i < rows.length(); i++) {
				JSONArray array = rows.getJSONArray(i);
				String type = array.getString(0);
				String name = array.getString(2);
				String value = array.getString(4);
				if (type.equals("attribute") && name.equals("source")) {
					String[] split = value.split(",");
					for (String s : split) {
						conventions.add(s);
						System.out.println(s);
					}
				}
			}
		}
		for (String c : conventions) {
			System.out.println(c);
		}
	}

}
