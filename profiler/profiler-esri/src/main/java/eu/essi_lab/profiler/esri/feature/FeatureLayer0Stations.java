package eu.essi_lab.profiler.esri.feature;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

/**
 * @author boldrini
 */
public class FeatureLayer0Stations extends FeatureLayer {

    private static HashMap<String, HashMap<String, Long>> identifiers = new HashMap<>();

    @Override
    public synchronized Long getResourceIdentifier(String view, XMLDocumentReader reader) {

	String platformId;
	try {
	    platformId = reader.evaluateString("//*:extension/*:uniquePlatformId");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    return null;
	}

	HashMap<String, Long> map = identifiers.get(view);
	if (map == null) {
	    map = new HashMap<String, Long>();
	    identifiers.put(view, map);
	}

	Long ret = map.get(platformId);
	if (ret == null) {
	    ret = (long) map.size();
	    map.put(platformId, ret);
	}
	return ret;
    }

    @Override
    public String getId() {
	return "0";
    }

    @Override
    public List<Field> getFields() {
	List<Field> ret = new ArrayList<Field>();
	ret.add(new Field("stationId", ESRIFieldType.OID, "Station ID", null, MetadataElement.STATION_IDENTIFIER));
	ret.add(new Field("datetimeStart", ESRIFieldType.START_DATE, "Date Start", null, MetadataElement.TEMP_EXTENT_BEGIN));
	ret.add(new Field("datetimeEnd", ESRIFieldType.END_DATE, "Date End", null, MetadataElement.TEMP_EXTENT_END));
	ret.add(new Field("latitude", ESRIFieldType.LATITUDE, "Latitude", null, MetadataElement.LATITUDE));
	ret.add(new Field("longitude", ESRIFieldType.LONGITUDE, "Longitude", null, MetadataElement.LONGITUDE));
	ret.add(new Field("name", ESRIFieldType.ISO_TITLE, "Name", null, MetadataElement.TITLE));
	return ret;
    }

    @Override
    public String getName() {
	return "Stations";
    }

    @Override
    public String getDescription() {
	return "Stations from the current DAB view";
    }

    @Override
    protected StatisticsMessage getStatisticsMessage(WebRequest webRequest) throws GSException {
	StatisticsMessage statisticsMessage = new StatisticsMessage();

	List<GSSource> allSources = ConfigurationWrapper.getAllSources();

	// set the required properties
	statisticsMessage.setSources(allSources);

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();
	GSLoggerFactory.getLogger(getClass()).info("Searching view in repository {}", databaseURI);

	statisticsMessage.setDataBaseURI(databaseURI);
	// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
	statisticsMessage.setWebRequest(webRequest);

	Optional<String> viewId = webRequest.extractViewId();

	// set the view
	if (viewId.isPresent()) {

	    WebRequestTransformer.setView(//
		    viewId.get(), //
		    statisticsMessage.getDataBaseURI(), //
		    statisticsMessage);
	}

	// set the user bond
	// statisticsMessage.setUserBond(andBond);

	// groups by source id
//	statisticsMessage.groupBy(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

	// pagination works with grouped results. in this case there is one result item for each source.
	// in order to be sure to get all the items in the same statistics response,
	// we set the count equals to number of sources
	Page page = new Page();
	page.setStart(1);
	page.setSize(allSources.size());

	statisticsMessage.setPage(page);

	// computes union of bboxes
	statisticsMessage.computeBboxUnion();

	statisticsMessage.computeTempExtentUnion();

	// computes count distinct of 2 queryables
	// statisticsMessage.countDistinct(//
	// Arrays.asList(//
	// MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
	// MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

	statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));
	return statisticsMessage;
    }

}
