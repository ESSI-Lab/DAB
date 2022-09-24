package eu.essi_lab.demo.accessor;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cdk.query.DistributedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Mappers have the responsibility to map an {@link OriginalMetadata} provided by the {@link HarvestedQueryConnector}s
 * or by the {@link DistributedQueryConnector}s to a {@link GSResource}
 * 
 * @author Fabrizio
 */
public class DemoMapper extends FileIdentifierMapper {

    /**
     * 
     */
    static final String DEMO_METADATA_SCHEMA = "DEMO_METADATA_SCHEMA";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return DEMO_METADATA_SCHEMA;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	//
	// - 1 -
	//
	// Creates a Dataset used to map the original metadata properties, according to the GI-project
	// internal data model
	//

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	//
	// - 2 -
	//
	// Reads the original metadata properties
	//

	String originalMetadata = originalMD.getMetadata();

	JSONObject object = new JSONObject(originalMetadata);

	String identifier = getString(object, "identifier");

	String title = getString(object, "title");

	String abstract_ = getString(object, "abstract");

	String dateStamp = getString(object, "dateStamp");

	JSONArray keywords = getJSONArray(object, "keywords");

	String organization = getString(object, "organization");

	String owner = getString(object, "owner");

	JSONArray spatial = getJSONArray(object, "spatialExtent");

	JSONArray temporal = getJSONArray(object, "tempExtent");

	//
	// - 3 -
	//
	// Sets the original metadata properties to the dataset
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	//
	// identifier
	//
	coreMetadata.setIdentifier(identifier);

	//
	// title
	//

	coreMetadata.setTitle(title);

	//
	// abstract
	//

	coreMetadata.setAbstract(abstract_);

	//
	// date stamp
	//

	coreMetadata.getMIMetadata().setDateStampAsDate(dateStamp);

	//
	// keywords
	//

	for (int i = 0; i < keywords.length(); i++) {

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(keywords.getString(i));
	}

	//
	// organization
	//

	ResponsibleParty orgContact = new ResponsibleParty();
	orgContact.setIndividualName(organization);
	orgContact.setRoleCode("author");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(orgContact);

	//
	// owner
	//

	ResponsibleParty ownerContact = new ResponsibleParty();
	ownerContact.setOrganisationName(owner);
	ownerContact.setRoleCode("owner");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(ownerContact);

	//
	// spatial extent
	//

	Double minLat = spatial.getDouble(0);
	Double minLon = spatial.getDouble(1);
	Double maxLat = spatial.getDouble(2);
	Double maxLon = spatial.getDouble(3);

	coreMetadata.addBoundingBox(maxLat, minLon, minLat, maxLon);

	//
	// temporal extent
	//

	String startTime = temporal.getString(0);
	String endTime = temporal.getString(1);

	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startTime, endTime);

	//
	// - 4 -
	//
	// Returns the mapped dataset
	//

	return dataset;
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private String getString(JSONObject object, String key) {

	return object.getString(key);
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private JSONArray getJSONArray(JSONObject object, String key) {

	return object.getJSONArray(key);
    }
}
