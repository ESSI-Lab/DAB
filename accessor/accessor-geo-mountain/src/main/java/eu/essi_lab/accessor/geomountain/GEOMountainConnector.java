/**
 *
 */
package eu.essi_lab.accessor.geomountain;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;

import com.github.jsonldjava.shaded.com.google.common.base.Charsets;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.WebConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GZIPUnzipper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class GEOMountainConnector extends HarvestedQueryConnector<GEOMountainConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "GEOMountainConnector";

    private static final String GEOMOUNTAIN_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR = "GEOMOUNTAIN_CONNECTOR_SCENE_LIST_DOWNLOAD_ERROR";
    private static final String GEOMOUNTAIN_CONNECTOR_IO_ERROR = "GEOMOUNTAIN_CONNECTOR_IO_ERROR";

    private static final int STEP = 100;

    // private Map<String, Map<String, List<String>>> prismaMap;
    private Downloader downloader;

    private int totalResults;
    private List<String> csvRecords;
    private Logger logger;
    private int partialNumbers;

    public GEOMountainConnector() {

	this.downloader = new Downloader();
	this.csvRecords = new ArrayList<String>();
	this.logger = GSLoggerFactory.getLogger(this.getClass());

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int count = 0;

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();
	if (pageSize == 0)
	    pageSize = 20;

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	GSLoggerFactory.getLogger(getClass()).info("Handling CSV File STARTED");

	if (csvRecords.isEmpty()) {
	    getCSVRecords();
	}

	GSLoggerFactory.getLogger(getClass()).info("Handling CSV File ENDED");

	int rowSize = csvRecords.size();
	logger.info("TOTAL NUMBER OF DATASET: " + rowSize);
	int maxSize = start + pageSize;
	boolean isLast = false;
	if(maxNumberReached) {
	    ret.setResumptionToken(null);
	    logger.info("ADDED ALL RECORDS {}", partialNumbers);
	    logger.info("GEO Mountain Harvesting is completed.");
	    return ret;
	}
	if (rowSize < maxSize) {
	    maxSize = rowSize - 1;
	    isLast = true;
	}
	for (int i = start; i < maxSize; i++) {
	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setSchemeURI(CommonNameSpaceContext.GEOMOUNTAIN);
	    originalMetadata.setMetadata(csvRecords.get(i));
	    ret.addRecord(originalMetadata);
	    partialNumbers++;
	}
	logger.debug("ADDED {}/{} records. Number of analyzed GEO Mountain row: {}", partialNumbers, rowSize, String.valueOf(start + pageSize));
	if (isLast) {
	    ret.setResumptionToken(null);
	    logger.info("ADDED ALL RECORDS {}", partialNumbers);
	    logger.info("GEO Mountain Harvesting is completed.");
	} else {
	    ret.setResumptionToken(String.valueOf(maxSize));

	}

	return ret;
    }

    private void getCSVRecords() {

	Optional<InputStream> stream = downloader.downloadOptionalStream(getSourceURL());

	try {
	    if (stream.isPresent()) {
		CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
		try (CSVParser parser = new CSVParser(new InputStreamReader(stream.get(), Charsets.ISO_8859_1), format)) {

		    for (CSVRecord record : parser) {
			if (record.size() != 20) {
			    logger.info("NOT VALID!!");
			    continue;
			}
			String s = csvRecordToString(record);
			csvRecords.add(s);
		    }

		}

	    }
	} catch (Exception e) {
	    logger.error(GEOMOUNTAIN_CONNECTOR_IO_ERROR + ": Error to read CSV file");
	}

    }

    private String csvRecordToString(CSVRecord record) {
	int fieldCount = record.size();
	StringJoiner joiner = new StringJoiner("|");
	for (int i = 0; i < fieldCount; i++) {
	    String field = record.get(i);
	    field = field.contains("|") ? field.replace("|", "/") : field;
	    joiner.add(field == null || field.isEmpty() ? null : field);
	}
	return joiner.toString();
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://s3.amazonaws.com/geomountain");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GEOMOUNTAIN);
	return ret;
    }

    @Override
    protected GEOMountainConnectorSetting initSetting() {
	return new GEOMountainConnectorSetting();
    }

    public static void main(String[] args) throws IOException {

	int step = 10;

    }

}
