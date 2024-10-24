package eu.essi_lab.accessor.egaskro;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.csv.CSVReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class EGASKROConnector extends HarvestedQueryConnector<EGASKROConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "EGASKROConnector";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	OriginalMetadata collection1 = createCollectionMetadata("Departmental/Roshydromet/Exposure dose", "egaskro-1");
	response.addRecord(collection1);

	OriginalMetadata collection2 = createCollectionMetadata("Departmental/Roshydromet/Average daily activity of radionuclides",
		"egaskro-2");
	response.addRecord(collection2);

	OriginalMetadata collection3 = createCollectionMetadata("Departmental/Roshydromet/Volumetric activity of radionuclides",
		"egaskro-3");
	response.addRecord(collection3);

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving exposure dose data STARTED");

	List<String[]> exposureDose = getExposureDose();
	
	if(exposureDose.isEmpty()){
	    
	    GSLoggerFactory.getLogger(getClass()).warn("No 'Exposure dose' metadata found");
	}
	
	for (String[] line : exposureDose) {
	    response.addRecord(createDatasetMetadata(line, "egaskro-1", "Exposure dose"));
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving exposure dose data ENDED");
	GSLoggerFactory.getLogger(getClass()).debug("Retrieving radionuclides average data STARTED");

	List<String[]> radioNuclidesAverage = getRadioNuclidesAverage();
	
	if(radioNuclidesAverage.isEmpty()){
	    
	    GSLoggerFactory.getLogger(getClass()).warn("No 'Average daily activity of radionuclides' metadata found");
	}
	
	for (String[] line : radioNuclidesAverage) {
	    response.addRecord(createDatasetMetadata(line, "egaskro-2", "Average daily activity of radionuclides"));
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving radionuclides average data ENDED");
	GSLoggerFactory.getLogger(getClass()).debug("Retrieving radionuclides volumetric data STARTED");

	List<String[]> radioNuclidesVolumetric = getRadioNuclidesVolumetric();
	
	if(radioNuclidesVolumetric.isEmpty()){
	    
	    GSLoggerFactory.getLogger(getClass()).warn("No 'Volumetric activity of radionuclides' metadata found");
	}
	
	for (String[] line : radioNuclidesVolumetric) {
	    response.addRecord(createDatasetMetadata(line, "egaskro-3", "Volumetric activity of radionuclides"));
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving radionuclides volumetric data ENDED");

	return response;
    }

    private OriginalMetadata createDatasetMetadata(String[] line, String parentId, String titlePrefix) {

	OriginalMetadata metadata = new OriginalMetadata();
	metadata.setSchemeURI(EGASKROResourceMapper.EGASKRO_SCHEME_URI);

	JSONObject object = new JSONObject();

	object.put("date", line[0]);
	object.put("time", line[1]);
	object.put("name", line[2]);
	object.put("localIndex", line[3]);
	object.put("longitude", line[4]);
	object.put("latitude", line[5]);
	object.put("quantity", line[6]);

	object.put("type", "dataset");
	object.put("titlePrefix", titlePrefix);
	object.put("parentId", parentId);

	metadata.setMetadata(object.toString());

	return metadata;
    }

    private OriginalMetadata createCollectionMetadata(String title, String id) {

	OriginalMetadata metadata = new OriginalMetadata();
	metadata.setSchemeURI(EGASKROResourceMapper.EGASKRO_SCHEME_URI);
	JSONObject object = new JSONObject();

	object.put("type", "collection");
	object.put("title", title);
	object.put("id", id);

	metadata.setMetadata(object.toString());

	return metadata;
    }

    private List<String[]> getExposureDose() {

	return getData(1);
    }

    private List<String[]> getRadioNuclidesAverage() {

	return getData(2);
    }

    private List<String[]> getRadioNuclidesVolumetric() {

	return getData(3);
    }

    private List<String[]> getData(int parameter) {

	try {

	    Downloader executor = new Downloader();
	    String sourceURL = getSourceURL();
	    if (sourceURL.endsWith("/")) {
		sourceURL = sourceURL.substring(0, sourceURL.length() - 1);
	    }

//	    HttpPost httpPost = new HttpPost(sourceURL + "/data");
//	    httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
//
//	    ByteArrayEntity entity = new ByteArrayEntity(
//		    ("system=1&subsystem=1&param=" + parameter + "&datarel=-1&outputfor=2&request=Request")
//			    .getBytes(StandardCharsets.UTF_8));
//	    httpPost.setEntity(entity);
//	    
	    String body = "system=1&subsystem=1&param=" + parameter + "&datarel=-1&outputfor=2&request=Request";
	    
	    HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, sourceURL + "/data", body,
		    HttpHeaderUtils.build("Content-Type", "application/x-www-form-urlencoded"));

	    HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

	    String html = IOStreamUtils.asUTF8String(response.body());

	    HREFGrabberClient grabberClient = new HREFGrabberClient(html);
	    
	    List<String> links = grabberClient.grabLinks("Download file");
	    if(links.isEmpty()){
		
		GSLoggerFactory.getLogger(getClass()).warn("No 'Download files' links found");
		
		return new ArrayList<>();
	    }
	    
	    String file = links.get(0);

	    Downloader downloader = new Downloader();
	    Optional<InputStream> fileStream = downloader.downloadOptionalStream(getSourceURL() + "/" + file);

	    List<String[]> ret = new ArrayList<>();

	    if (fileStream.isPresent()) {

		InputStream is = fileStream.get();

		CSVReader reader = new CSVReader(new InputStreamReader(is, "Cp1251"), ';', '\"');

		ret = reader.readAll();
		ret.remove(0);// headers

		reader.close();
	    }

	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error getting data for parameter {}", parameter, e);

	    return new ArrayList<>();
	}
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://www.feerc.ru/geoss/egaskro");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(EGASKROResourceMapper.EGASKRO_SCHEME_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected EGASKROConnectorSetting initSetting() {

	return new EGASKROConnectorSetting();
    }
}
