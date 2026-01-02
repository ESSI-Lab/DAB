package eu.essi_lab.accessor.waf.onamet_stations;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.validator.netcdf.classic.NetCDF3TimeSeriesValidator;

/**
 * @author Fabrizio
 */
public class ONAMETStationsDownloader extends DataDownloader {

    private static final String GET_REMOTE_DESC_ERROR = "GET_REMOTE_DESC_ERROR";
    private static final String DOWNLOAD_ERROR = "DOWNLOAD_ERROR";

    @Override
    public boolean canDownload() {

	String linkage = online.getLinkage();

	return linkage.contains("thredds-data.s3.amazonaws.com");
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	try {

	    String linkage = online.getLinkage();

	    Downloader downloader = new Downloader();
	    InputStream inputStream = downloader.downloadOptionalStream(linkage).get();

	    File tempFile = File.createTempFile(getClass().getSimpleName(), ".nc");

	    FileOutputStream outputStream = new FileOutputStream(tempFile);

	    IOUtils.copy(inputStream, outputStream);

	    DataObject dataObject = new DataObject();
	    dataObject.setFile(tempFile);

	    NetCDF3TimeSeriesValidator validator = new NetCDF3TimeSeriesValidator();

	    DataDescriptor dataAttributes = validator.readDataAttributes(dataObject);

	    tempFile.delete();
	    tempFile.deleteOnExit();

	    return Arrays.asList(dataAttributes);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GET_REMOTE_DESC_ERROR); //
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	try {

	    String linkage = online.getLinkage();

	    Downloader downloader = new Downloader();
	    InputStream inputStream = downloader.downloadOptionalStream(linkage).get();

	    File tempFile = File.createTempFile(getClass().getSimpleName(), ".nc");

	    FileOutputStream outputStream = new FileOutputStream(tempFile);

	    IOUtils.copy(inputStream, outputStream);

	    tempFile.deleteOnExit();

	    return tempFile;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DOWNLOAD_ERROR); //
	}
    }
}
