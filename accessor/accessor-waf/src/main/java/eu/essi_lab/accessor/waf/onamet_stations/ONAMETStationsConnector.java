package eu.essi_lab.accessor.waf.onamet_stations;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.waf.onamet.ONAMETConnector;
import eu.essi_lab.accessor.waf.onamet_stations.ONAMETParameter.ONAMETParameterId;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.csv.CSVReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class ONAMETStationsConnector extends HarvestedQueryConnector<ONAMETStationsConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ONAMETStationsConnector";
    /**
     * 
     */
    static final String ROOT_PATH = "ema";
    /**
     * 
     */
    private static final String PARAMETERS_CSV_PATH = ROOT_PATH + "/metadatos/paramters.csv";
    /**
     * 
     */
    private static final String STATIONS_CSV_PATH = ROOT_PATH + "/metadatos/stations.csv";
    /**
     * 
     */
    private static final String STATIONS_CSV_FILE_DOWNLOAD_ERROR = "STATIONS_CSV_FILE_DOWNLOAD_ERROR";
    /**
     * 
     */
    private static final String PARAMETERS_CSV_FILE_DOWNLOAD_ERROR = "PARAMETERS_CSV_FILE_DOWNLOAD_ERROR";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	//
	// metadata creation
	//
	GSLoggerFactory.getLogger(getClass()).debug("Downloading stations CSV STARTED");
	List<String[]> stations = readCsv(STATIONS_CSV_PATH, STATIONS_CSV_FILE_DOWNLOAD_ERROR);
	GSLoggerFactory.getLogger(getClass()).debug("Downloading stations CSV ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Downloading parameters CSV STARTED");
	List<String[]> parameters = readCsv(PARAMETERS_CSV_PATH, PARAMETERS_CSV_FILE_DOWNLOAD_ERROR);
	GSLoggerFactory.getLogger(getClass()).debug("Downloading parameters CSV ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Creating original metadata STARTED");

	for (String[] stationArray : stations) {

	    ONAMETStation station = new ONAMETStation(stationArray);

	    for (String parameterId : station.getParameters()) {

		ONAMETParameter parameter = ONAMETParameter.find(parameters, parameterId);

		if (ONAMETParameterId.valueOf(parameterId) != ONAMETParameterId.RS) {
		    
		    response.addRecord(ONAMETStationsMapper.create(station, parameter));
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating original metadata ENDED");

	return response;
    }

    /**
     * @param filePath
     * @param errorId
     * @return
     * @throws GSException
     */
    private List<String[]> readCsv(String filePath, String errorId) throws GSException {

	String csvURL = getSourceURL().endsWith("/") ? getSourceURL() + filePath : getSourceURL() + "/" + filePath;

	Downloader downloader = new Downloader();
	Optional<InputStream> csvStream = downloader.downloadOptionalStream(csvURL);

	if (!csvStream.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to download " + filePath);

	    throw GSException.createException(//
		    ONAMETConnector.class, //
		    "Unable to download " + filePath, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    STATIONS_CSV_FILE_DOWNLOAD_ERROR //
	    );
	}

	try (CSVReader reader = new CSVReader(new InputStreamReader(csvStream.get(), "Cp1251"), ',')) {

	    List<String[]> list = reader.readAll();
	    list.remove(0);

	    return list;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    ONAMETConnector.class, //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    errorId //
	    );
	}
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://onamet.gov.do/ema");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return null;
    }

    @Override
    protected ONAMETStationsConnectorSetting initSetting() {

	return new ONAMETStationsConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }

    public static void main(String[] args) throws Exception {

	ONAMETStationsConnector connector = new ONAMETStationsConnector();

	connector.setSourceURL("https://onamet.gov.do/");

	ListRecordsResponse<OriginalMetadata> listRecords = connector.listRecords(new ListRecordsRequest());

	System.out.println(listRecords);

    }

}
