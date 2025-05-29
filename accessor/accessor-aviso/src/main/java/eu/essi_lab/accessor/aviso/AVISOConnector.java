package eu.essi_lab.accessor.aviso;

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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * 
 */

/**
 * @author Fabrizio
 */
public class AVISOConnector extends HarvestedQueryConnector<AVISOConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "AVISOConnector";

    /**
     * 
     */
    public AVISOConnector() {
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	FTPDownloader ftpDownloader = new FTPDownloader();
	List<String> names = ftpDownloader.//
		downloadFileNames(getSourceURL()).//
		stream().filter(n -> n.endsWith("nc")).//
		collect(Collectors.toList());

	int start = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    start = Integer.valueOf(resumptionToken);
	}

	int offset = 10;
	int end = 0;

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (start + offset < names.size()) {
	    end = start + offset;
	    response.setResumptionToken(String.valueOf(end));
	} else {
	    end = names.size();
	}

	for (int i = start; i < end; i++) {

	    String fileName = names.get(i);

	    String storedFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + fileName;

	    GSLoggerFactory.getLogger(getClass()).info("Handling file [" + i + "/" + names.size() + "] " + fileName + " STARTED");

	    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + storedFileName);

	    file = ftpDownloader.downloadToFile(getSourceURL(), fileName, file, false);

	    try {

		NetcdfDataset dataset = NetcdfDataset.openDataset(file.toString());

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(AVISOMapper.METADATA_SCHEMA);
		metadata.setMetadata(dataset.toString());

		try {
		    dataset.release();
		    dataset.close();
		} catch (IOException e) {
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}

		GSPropertyHandler propertyHandler = new GSPropertyHandler();
		propertyHandler.add(new GSProperty<File>(AVISOMapper.NET_CDF_FILE_PROPERTY, file));

		metadata.setAdditionalInfo(propertyHandler);

		response.addRecord(metadata);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Handling file [" + i + "/" + names.size() + "] " + fileName + " ENDED");
	}

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(AVISOMapper.METADATA_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://ftp.aviso.altimetry.fr/pub/oceano/AVISO/indicators/msl/");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected AVISOConnectorSetting initSetting() {

	return new AVISOConnectorSetting();
    }
}
