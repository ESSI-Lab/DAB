package eu.essi_lab.accessor.sos.downloader;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.sos.SOSConnector;
import eu.essi_lab.accessor.sos.SOSIdentifierMangler;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType;
import eu.essi_lab.jaxb.sos._2_0.ObservationOfferingType.PhenomenonTime;
import eu.essi_lab.jaxb.sos._2_0.gda.DataAvailabilityMemberType;
import eu.essi_lab.jaxb.sos._2_0.gda.GetDataAvailabilityResponseType;
import eu.essi_lab.jaxb.sos._2_0.gda.TimeObjectPropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.StringOrRefType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractOfferingType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class SOSDownloader extends DataDownloader {

    private static final String SOS_DOWNLOADER_ERROR = "SOS_DOWNLOADER_ERROR";
    protected String linkage;

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canConnect() throws GSException {
	GSSource source = new GSSource();
	source.setEndpoint(linkage);
	return getConnector().supports(source);
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(getSupportedProtocol()));

    }

    public String getSupportedProtocol() {
	return NetProtocols.SOS_2_0_0.getCommonURN();
    }

    @Override
    public boolean canSubset(String dimensionName) {

	return true;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();
	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_2_0());
	    descriptor.setCRS(CRS.EPSG_4326());
	    String name = online.getName();
	    // we expect a name encoded by the SavaHISIdentifierMangler
	    if (name != null) {
		SOSIdentifierMangler mangler = new SOSIdentifierMangler();
		mangler.setMangling(name);
		String site = mangler.getFeature();
		String variable = mangler.getObservedProperty();
		String procedure = mangler.getProcedure();

		SOSConnector connector = getConnector();
		connector.setSourceURL(linkage);

		Date beginDate = null;
		Date endDate = null;

		if (connector.hasGetDataAvailabilityOperation()) {

		    GetDataAvailabilityResponseType dataAvailability = connector.retrieveDataAvailability(procedure, site, variable);

		    List<DataAvailabilityMemberType> availabilities = dataAvailability.getDataAvailabilityMember();
		    // may be more than one availability member, correspondent to more than one observation or campaign

		    for (DataAvailabilityMemberType availability : availabilities) {
			TimeObjectPropertyType phenomenonTime = availability.getPhenomenonTime();
			if (phenomenonTime != null) {
			    JAXBElement<? extends AbstractTimeObjectType> abstractTimeObject = phenomenonTime.getAbstractTimeObject();
			    if (abstractTimeObject != null) {
				AbstractTimeObjectType abstractTime = abstractTimeObject.getValue();
				if (abstractTime instanceof TimePeriodType) {
				    TimePeriodType tpt = (TimePeriodType) abstractTime;
				    String begin = tpt.getBeginPosition().getValue().get(0);
				    String end = tpt.getEndPosition().getValue().get(0);
				    Date tmpBeginDate = ISO8601DateTimeUtils.parseISO8601ToDate(begin).get();
				    Date tmpEndDate = ISO8601DateTimeUtils.parseISO8601ToDate(end).get();
				    if (beginDate == null || beginDate.after(tmpBeginDate)) {
					beginDate = tmpBeginDate;
				    }
				    if (endDate == null || endDate.after(tmpEndDate)) {
					endDate = tmpEndDate;
				    }
				} else {
				    GSLoggerFactory.getLogger(getClass()).warn("Null abstract time");
				}
			    } else {
				GSLoggerFactory.getLogger(getClass()).warn("Null abstract time object");
			    }
			} else {
			    GSLoggerFactory.getLogger(getClass()).warn("Null phenomenon time");
			}
		    }
		} else {

		    AbstractOfferingType abstractOffering = connector.getOffering(procedure);
		    if (abstractOffering instanceof ObservationOfferingType) {
			ObservationOfferingType observationOffering = (ObservationOfferingType) abstractOffering;
			PhenomenonTime phenomenonTime = observationOffering.getPhenomenonTime();
			if (phenomenonTime != null) {
			    TimePeriodType timePeriod = phenomenonTime.getTimePeriod();
			    String begin = timePeriod.getBeginPosition().getValue().get(0);
			    String end = timePeriod.getEndPosition().getValue().get(0);
			    Optional<Date> beginParsed = ISO8601DateTimeUtils.parseISO8601ToDate(begin);
			    Optional<Date> endParsed = ISO8601DateTimeUtils.parseISO8601ToDate(end);
			    if (beginParsed.isPresent() && endParsed.isPresent()) {
				beginDate = beginParsed.get();
				endDate = endParsed.get();
			    }
			} else {
			    GSLoggerFactory.getLogger(getClass()).warn("empty phenomenon time");
			}

		    }

		}

		if (beginDate != null && endDate != null) {
		    descriptor.setTemporalDimension(beginDate, endDate);
		}
		ret.add(descriptor);

	    }

	    return ret;

	} catch (GSException gse) {

	    throw gse;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_DOWNLOADER_ERROR, //
		    e);
	}

    }

    public SOSConnector getConnector() {
	return new SOSConnector();
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	String name = online.getName();
	// we expect a SavaHIS online resource name in the form as encoded by SavaHISIdentifierMangler
	if (name != null) {
	    try {
		SOSIdentifierMangler mangler = new SOSIdentifierMangler();
		mangler.setMangling(name);
		String featureIdentifier = mangler.getFeature();
		String property = mangler.getObservedProperty();
		String procedure = mangler.getProcedure();

		SOSConnector connector = getConnector();
		connector.setSourceURL(linkage);

		Date begin = null;
		Date end = null;

		DataDimension temporalDimension = descriptor.getTemporalDimension();
		if (temporalDimension != null) {
		    try {
			ContinueDimension dimension = temporalDimension.getContinueDimension();
			Number lower = dimension.getLower();
			Number upper = dimension.getUpper();
			begin = new Date(lower.longValue());
			end = new Date(upper.longValue());
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
		eu.essi_lab.jaxb.sos._2_0.wml.ObjectFactory factory = new eu.essi_lab.jaxb.sos._2_0.wml.ObjectFactory();

		eu.essi_lab.jaxb.sos._2_0.wml.CollectionType collection = new eu.essi_lab.jaxb.sos._2_0.wml.CollectionType();
		List<OMObservationType> observations = connector.retrieveData(procedure, featureIdentifier, property, begin, end);
		for (OMObservationType observation : observations) {
		    eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationPropertyType observationProperty = new eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationPropertyType();
		    observationProperty.setOMObservation(observation);
		    collection.getObservationMember().add(observationProperty);
		}

		StringOrRefType desc = new StringOrRefType();
		desc.setValue("Downloaded by GI-suite broker");
		collection.setDescription(desc);
		JAXBElement<eu.essi_lab.jaxb.sos._2_0.wml.CollectionType> jaxb = factory.createCollection(collection);

		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
		tmpFile.deleteOnExit();

		JAXBSOS.getInstance().marshal(jaxb, tmpFile);

		return tmpFile;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SOS_DOWNLOADER_ERROR, //
			e);
	    }

	}

	throw GSException.createException(//
		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		SOS_DOWNLOADER_ERROR);

    }

}
