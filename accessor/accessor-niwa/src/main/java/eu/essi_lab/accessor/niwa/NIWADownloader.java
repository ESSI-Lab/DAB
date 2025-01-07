package eu.essi_lab.accessor.niwa;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author Fabrizio
 */
public class NIWADownloader extends WMLDataDownloader {

    /**
     * 
     */
    private static final String NIWA_DOWNLOADER_NO_TEMPORAL_EXTENT_ERROR = "NIWA_DOWNLOADER_NO_TEMPORAL_EXTENT_ERROR";
    /**
     * 
     */
    private static final String NIWA_DOWNLOADER_ERROR = "NIWA_DOWNLOADER_ERROR";

    @Override
    public boolean canConnect() {
	try {
	    online.getLinkage();

	    return true;

	} catch (Exception e) {
	}
	return false;
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.NIWA.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	String name = online.getName();

	NIWAClient client = new NIWAClient();

	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();

	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	//
	//
	//

	NIWAIdentifierMangler mangler = new NIWAIdentifierMangler();
	mangler.setMangling(name);

	String datasetIdentifier = mangler.getDatasetIdentifier();

	Optional<TemporalExtent> temporalExtent = client.getTemporalExtent(datasetIdentifier);

	if (!temporalExtent.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find temporal extent of dataset identifier: " + datasetIdentifier, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NIWA_DOWNLOADER_NO_TEMPORAL_EXTENT_ERROR);
	}

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.get().getBeginPosition()).get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.get().getEndPosition()).get();

	descriptor.setTemporalDimension(begin, end);

	//
	//
	//

	DataDimension temporalDimension = descriptor.getTemporalDimension();

	long oneHourInMilliseconds = 1000 * 60 * 60l;
	Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;

	temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);

	ret.add(descriptor);
	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {

	    String name = online.getName();

	    NIWAIdentifierMangler mangler = new NIWAIdentifierMangler();
	    mangler.setMangling(name);

	    String datasetIdentifier = mangler.getDatasetIdentifier();

	    Date begin = null;
	    Date end = null;

	    DataDimension dimension = descriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    }

	    ObjectFactory factory = new ObjectFactory();

	    ArrayList<SimpleEntry<String, String>> list = NIWAClient.download(datasetIdentifier, begin, end);

	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	    for (SimpleEntry<String, String> entry : list) {

		ValueSingleVariable v = new ValueSingleVariable();

		try {
		    v.setValue(new BigDecimal(entry.getValue()));

		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(ISO8601DateTimeUtils.parseISO8601ToDate(entry.getKey()).get());

		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);

		    addValue(tsrt, v);

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    continue;
		}
	    }

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");

	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);

	    return tmpFile;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NIWA_DOWNLOADER_ERROR, //
		    e);
	}
    }

    public static void main(String[] args) throws GSException {

	NIWADownloader niwaDownloader = new NIWADownloader();

	Dataset dataset = new Dataset();
	
	GSSource gsSource = new GSSource();
	gsSource.setUniqueIdentifier("niwa");
	dataset.setSource(gsSource);

	dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
		"parameter;Discharge.Master;platform;15341", //
		"https://hydrowebportal.niwa.co.nz", //
		NetProtocols.NIWA.getCommonURN(), //
		"download");

	String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, "parameter;Discharge.Master;platform;15341");

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().setResourceIdentifier(resourceIdentifier);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline()
		.setIdentifier(resourceIdentifier);

	niwaDownloader.setOnlineResource(dataset, resourceIdentifier);

	List<DataDescriptor> remoteDescriptors = niwaDownloader.getRemoteDescriptors();

	File download = niwaDownloader.download(remoteDescriptors.get(0));
	
	System.out.println(download);
    }

}
