/**
 *
 */
package eu.essi_lab.demo.accessor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * Connectors have the responsibility to connect to the remote service to harvest in order to generate a list of
 * {@link OriginalMetadata} which will be mapped, during the harvesting phase, by the proper {@link IResourceMapper}.<br>
 * 
 * 
 * @author Fabrizio
 */
public class DemoConnector extends HarvestedQueryConnector<DemoConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "DemoConnector";
    private static final String DEMO_CONNECTOR_LIST_RECORDS_ERROR = "DEMO_CONNECTOR_LIST_RECORDS_ERROR";

    /**
    * 
    */
    public DemoConnector() {

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	try {

	    //
	    // - 1 -
	    //
	    // Extracts the "href" attributes value from the <a> elements found in the HTML page
	    // at the source URL, and keeps only the references which ends with the ".json" extension
	    //

	    HREFGrabberClient client = new HREFGrabberClient(new URL(getSourceURL()));

	    List<String> hrefLinks = client.grabLinks().//
		    stream().//
		    filter(link -> link.endsWith(".json")).//
		    collect(Collectors.toList());

	    //
	    // - 2 -
	    //
	    // Creates a new ListRecordsResponse of OriginalMetadata.
	    // The OriginalMetadata represents a single metadata in the original format
	    // as provided by the accessed remote service.
	    // This original metadata will be later mapped to a GSResource by the DemoMapper
	    //

	    ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	    //
	    // - 3 -
	    //
	    // Downloads all the referenced json files, and for each of them creates an OriginalMetadata
	    // to add to the ListRecordsResponse.
	    // Each OriginalMetadata has a scheme URI which is used, during the harvesting phase, to select the proper
	    // mapper
	    // ( in this case the DemoMapper )
	    //

	    for (String href : hrefLinks) {

		Downloader downloader = new Downloader();
		Optional<String> jsonMetadata = downloader.downloadString(href);

		if (jsonMetadata.isPresent()) {

		    OriginalMetadata original = new OriginalMetadata();

		    original.setMetadata(jsonMetadata.get());

		    original.setSchemeURI(DemoMapper.DEMO_METADATA_SCHEMA);

		    response.addRecord(original);
		}
	    }

	    //
	    // - 4 -
	    //
	    // Returns the ListRecordsResponse
	    //

	    return response;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(this.getClass(), DEMO_CONNECTOR_LIST_RECORDS_ERROR, e);
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(DemoMapper.DEMO_METADATA_SCHEMA);

	return ret;
    }

    @Override

    public boolean supports(GSSource source) {

	return source.getEndpoint().equals("https://essilab.eu/demo/accessor");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected DemoConnectorSetting initSetting() {

	return new DemoConnectorSetting();
    }
}
