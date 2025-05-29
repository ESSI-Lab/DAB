package eu.essi_lab.accessor.copernicus.dataspace.harvested;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.accessor.satellite.common.SatelliteCollectionMapper;
import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.GMDResourceMapper;

/**
 * @author Roberto
 */
public class CopernicusDataspaceConnector extends HarvestedQueryConnector<CopernicusDataspaceConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CopernicusDataspaceConnector";

    private static final String COPERNICUS_DATASPACE_CONNECTOR_COLLECTION_ADDING_ERROR = "COPERNICUS_DATASPACE_CONNECTOR_COLLECTION_ADDING_ERROR";

    private final InputStream SENTINEL_1 = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("collections/sentinel1.xml");
    private final InputStream SENTINEL_2 = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("collections/sentinel2.xml");
    private final InputStream SENTINEL_3 = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("collections/sentinel3.xml");
    private final InputStream SENTINEL = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("collections/sentinel.xml");

    private ClonableInputStream s1Stream;
    private ClonableInputStream s2Stream;
    private ClonableInputStream s3Stream;
    private ClonableInputStream sStream;

    protected Downloader downloader;

    protected Logger logger = GSLoggerFactory.getLogger(this.getClass());

    /**
     * This is the cached set of package identifiers, used during subsequent
     * list records.
     */

    private Integer cachedRecordsCount = null;

    public CopernicusDataspaceConnector() {

	try {
	    sStream = new ClonableInputStream(SENTINEL);
	    s1Stream = new ClonableInputStream(SENTINEL_1);
	    s2Stream = new ClonableInputStream(SENTINEL_2);
	    s3Stream = new ClonableInputStream(SENTINEL_3);
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(SatelliteUtils.class).error(e.getMessage(), e);
	}

    }

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().toLowerCase().contains("catalogue.dataspace.copernicus.eu");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	GSLoggerFactory.getLogger(getClass()).info("Storing Sentinel collections STARTED");
	addCollections(ret);
	GSLoggerFactory.getLogger(getClass()).info("Storing Sentinel collections ENDED");

	return ret;

    }

    /**
     * @param response
     * @throws GSException
     */
    protected void addCollections(ListRecordsResponse<OriginalMetadata> response) throws GSException {

	try {

	    List<GSResource> collections = getCollections();

	    for (GSResource collection : collections) {

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(SatelliteCollectionMapper.SATELLITE_COLLECTION_SCHEME_URI);

		//
		// this is required to avoid problems with the CDATA. see GIP-152
		//
		collection.getOriginalMetadata().setMetadata("");

		metadata.setMetadata(collection.asString(true));

		response.addRecord(metadata);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    COPERNICUS_DATASPACE_CONNECTOR_COLLECTION_ADDING_ERROR, //
		    e);
	}

    }

    private List<GSResource> getCollections() throws Exception {
	return getSentinelCollections(new GSSource(), false);
    }

    public List<GSResource> getSentinelCollections(GSSource sentinelSource, boolean setIndexes) throws Exception {

//	//
//	// root collection with highest ranking
//	//
//	GSResource sentinelCollection = SatelliteUtils.mapCollection(sStream.clone(), sentinelSource, true, setIndexes);
//	sentinelCollection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
//	sentinelCollection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);

	//
	// other collections with lowest ranking
	//
	GSResource sentinel1Collection = mapCollection(s1Stream.clone(), sentinelSource, true, setIndexes);
//	sentinel1Collection.getPropertyHandler().setLowestRanking();
	
	sentinel1Collection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	sentinel1Collection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);

	GSResource sentinel2Collection = mapCollection(s2Stream.clone(), sentinelSource, true, setIndexes);
//	sentinel2Collection.getPropertyHandler().setLowestRanking();
	
	sentinel2Collection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	sentinel2Collection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);

	GSResource sentinel3Collection = mapCollection(s3Stream.clone(), sentinelSource, true, setIndexes);
//	sentinel3Collection.getPropertyHandler().setLowestRanking();
	
	sentinel3Collection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	sentinel3Collection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);

	return Arrays.asList(//
		//sentinelCollection, //
		sentinel1Collection, //
		sentinel2Collection, //
		sentinel3Collection //
	);
    }
    
    
    
    /**
     * @param collection
     * @param source
     * @param gdc
     * @param setIndexes
     * @return
     * @throws Exception
     */
    public GSResource mapCollection(InputStream collection, GSSource source, boolean gdc, boolean setIndexes) throws Exception {

	GMDResourceMapper resourceMapper = new GMDResourceMapper();

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(IOStreamUtils.asUTF8String(collection));
	originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);

	GSResource gsCollection = resourceMapper.map(originalMetadata, source);

	if (gdc) {
	    gsCollection.getPropertyHandler().setIsGDC(true);
	}

	String queryables = "prodType";
	String origin = "sentinel";

	String publicId = gsCollection.getPublicId();

	if (publicId.equals("USGS_LANDSAT_8")) {

	    queryables += ",cloudcp,";
	    queryables += "pubDatefrom,";
	    queryables += "pubDateuntil,";
	    queryables += "row,";
	    queryables += "path";

	    origin = "landsat";

	} else if (publicId.contains("SENTINEL")) {

	    queryables += ",pubDatefrom,";
	    queryables += "pubDateuntil,";

	    if (publicId.contains("1") || publicId.contains("1")) {

		queryables += "sensorOpMode,";
		queryables += "sensorSwath,";
		queryables += "relOrbit,";
		queryables += "sarPolCh";

	    } else if (publicId.contains("2") || publicId.contains("2")) {

		//queryables += "prodType,";
		queryables += "sensorOpMode,";
		queryables += "relOrbit,";
		queryables += "cloudcp";

	    } else { // 3A

		//queryables += "prodType,";
		queryables += "s3InstrumentIdx,";
		queryables += "s3ProductLevel,";
		queryables += "s3Timeliness,";
		queryables += "relOrbit";
		queryables += "cloudcp";
	    }

	}

	SatelliteScene satelliteScene = new SatelliteScene();
	satelliteScene.setOrigin(origin);
	satelliteScene.setCollectionQueryables(queryables);

	gsCollection.getExtensionHandler().setSatelliteScene(satelliteScene);

	if (setIndexes) {
	    //
	    // writes indexes
	    //
	    IndexedElementsWriter.write(gsCollection);
	}

	//
	// set the fileIdentifer as private id
	//
	gsCollection.setPrivateId(publicId);

	return gsCollection;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return Arrays.asList(SatelliteCollectionMapper.SATELLITE_COLLECTION_SCHEME_URI);
    }


    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CopernicusDataspaceConnectorSetting initSetting() {

	return new CopernicusDataspaceConnectorSetting();
    }

}
