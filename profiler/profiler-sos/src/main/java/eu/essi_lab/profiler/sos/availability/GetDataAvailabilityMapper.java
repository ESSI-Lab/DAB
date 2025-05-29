package eu.essi_lab.profiler.sos.availability;

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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.sos._2_0.gda.DataAvailabilityMemberType;
import eu.essi_lab.jaxb.sos._2_0.gda.GetDataAvailabilityResponseType;
import eu.essi_lab.jaxb.sos._2_0.gda.TimeObjectPropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ObjectFactory;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimeIndeterminateValueType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.sos.SOSRequest.Parameter;

public class GetDataAvailabilityMapper extends DiscoveryResultSetMapper<String> {

    private static final String SOS_DATA_AVAILABILITY_MAPPER_ERROR = "SOS_DATA_AVAILABILITY_MAPPER_ERROR";

    public GetDataAvailabilityMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema SOS_DATA_AVAILABILITY_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return SOS_DATA_AVAILABILITY_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(message.getWebRequest());

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    // MDMetadata metadata = coreMetadata.getMDMetadata();
	    MIMetadata metadata = coreMetadata.getMIMetadata();

	    String platformCode = "";
	    String platformName = "";
	    try {
		platformCode = res.getExtensionHandler().getUniquePlatformIdentifier().get();
		platformName = platformCode;
		platformName = metadata.getMIPlatform().getCitation().getTitle();
	    } catch (Exception e) {
	    }

	    String attributeCode = "";
	    String attributeName = "";
	    try {
		attributeCode = res.getExtensionHandler().getUniquePlatformIdentifier().get();
		attributeName = attributeCode;
		attributeName = metadata.getCoverageDescription().getAttributeTitle();
	    } catch (Exception e) {
	    }

	    // Double latitude = 0.;// 41.5724
	    // try {
	    // latitude =
	    // Double.parseDouble(metadata.getDataIdentification().getGeographicBoundingBox().getNorth().toString());
	    // } catch (Exception e) {
	    // }
	    // Double longitude = 0.;// -111.8551
	    // try {
	    // longitude =
	    // Double.parseDouble(metadata.getDataIdentification().getGeographicBoundingBox().getEast().toString());
	    // } catch (Exception e) {
	    // }

	    TimePositionType tp1 = new TimePositionType();

	    TimePositionType tp2 = new TimePositionType();

	    tp2.setIndeterminatePosition(TimeIndeterminateValueType.NOW);

	    String begin = null;
	    try {
		begin = metadata.getDataIdentification().getTemporalExtent().getBeginPosition();
		tp1.getValue().add(begin);
	    } catch (Exception e) {
		// TODO: handle exception
	    }
	    String end = null;
	    try {
		end = metadata.getDataIdentification().getTemporalExtent().getEndPosition();
		tp2.getValue().add(end);
	    } catch (Exception e) {
		// TODO: handle exception
	    }
	    try {
		net.opengis.gml.v_3_2_0.TimeIndeterminateValueType beginIndeterminate = metadata.getDataIdentification().getTemporalExtent()
			.getIndeterminateBeginPosition();
		if (beginIndeterminate.equals(net.opengis.gml.v_3_2_0.TimeIndeterminateValueType.NOW)) {
		    tp1.setIndeterminatePosition(TimeIndeterminateValueType.NOW);
		}
	    } catch (Exception e) {

	    }
	    try {
		net.opengis.gml.v_3_2_0.TimeIndeterminateValueType endIndeterminate = metadata.getDataIdentification().getTemporalExtent()
			.getIndeterminateEndPosition();
		if (endIndeterminate.equals(net.opengis.gml.v_3_2_0.TimeIndeterminateValueType.NOW)) {
		    tp2.setIndeterminatePosition(TimeIndeterminateValueType.NOW);
		}
	    } catch (Exception e) {

	    }

	    GetDataAvailabilityResponseType gdaResponse = new GetDataAvailabilityResponseType();

	    DataAvailabilityMemberType dataAvailabilityMember = new DataAvailabilityMemberType();
	    ReferenceType foiReference = new ReferenceType();
	    foiReference.setTitle(platformName);
	    foiReference.setHref(platformCode);
	    dataAvailabilityMember.setFeatureOfInterest(foiReference);

	    ReferenceType propertyReference = new ReferenceType();
	    propertyReference.setTitle(attributeName);
	    propertyReference.setHref(attributeCode);
	    dataAvailabilityMember.setObservedProperty(propertyReference);

	    ReferenceType procedureReference = new ReferenceType();
	    String procedure = request.getParameterValue(Parameter.PROCEDURE);
	    procedureReference.setTitle(procedure);
	    procedureReference.setHref(procedure);
	    dataAvailabilityMember.setProcedure(procedureReference);

	    TimeObjectPropertyType time = new TimeObjectPropertyType();
	    TimePeriodType timePeriod = new TimePeriodType();
	    timePeriod.setId("Time-ID");

	    timePeriod.setBeginPosition(tp1);

	    timePeriod.setEndPosition(tp2);
	    ObjectFactory gmlFactory = new ObjectFactory();
	    JAXBElement<? extends AbstractTimeObjectType> jaxbTime = gmlFactory.createTimePeriod(timePeriod);
	    time.setAbstractTimeObject(jaxbTime);
	    dataAvailabilityMember.setPhenomenonTime(time);

	    gdaResponse.getDataAvailabilityMember().add(dataAvailabilityMember);
	    eu.essi_lab.jaxb.sos._2_0.gda.ObjectFactory gdaFactory = new eu.essi_lab.jaxb.sos._2_0.gda.ObjectFactory();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBElement<GetDataAvailabilityResponseType> jaxbElement = gdaFactory.createGetDataAvailabilityResponse(gdaResponse);
	    JAXBSOS.getInstance().marshal(jaxbElement, baos);
	    String ret = new String(baos.toByteArray(), StandardCharsets.UTF_8);
	    baos.close();
	    return ret;

	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_DATA_AVAILABILITY_MAPPER_ERROR);
	}

    }
}
