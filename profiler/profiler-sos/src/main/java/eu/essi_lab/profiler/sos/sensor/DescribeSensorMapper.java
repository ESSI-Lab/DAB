package eu.essi_lab.profiler.sos.sensor;

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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Document;

import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractTimeGeometricPrimitiveType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType.Description;
import eu.essi_lab.jaxb.sos._2_0.swes_2.SensorDescriptionType;
import eu.essi_lab.jaxb.sos._2_0.swes_2.SensorDescriptionType.Data;
import eu.essi_lab.jaxb.sos._2_0.swes_2.SensorDescriptionType.ValidTime;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.jaxb.sos.factory.JAXBSOSPrefixMapper;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.sos.SOSUtils;
import net.opengis.gml.v_3_2_0.AbstractTimePrimitiveType;
import net.opengis.sensorml.v_2_0.AbstractProcessType.FeaturesOfInterest;
import net.opengis.sensorml.v_2_0.CapabilityListType;
import net.opengis.sensorml.v_2_0.CapabilityListType.Capability;
import net.opengis.sensorml.v_2_0.DescribedObjectType.Capabilities;
import net.opengis.sensorml.v_2_0.FeatureListType;
import net.opengis.sensorml.v_2_0.IdentifierListPropertyType;
import net.opengis.sensorml.v_2_0.IdentifierListType;
import net.opengis.sensorml.v_2_0.IdentifierListType.Identifier;
import net.opengis.sensorml.v_2_0.KeywordListPropertyType;
import net.opengis.sensorml.v_2_0.KeywordListType;
import net.opengis.sensorml.v_2_0.PhysicalSystemType;
import net.opengis.sensorml.v_2_0.TermType;
import net.opengis.swecommon.v_2_0.AbstractDataComponentType;
import net.opengis.swecommon.v_2_0.TextType;

public class DescribeSensorMapper extends DiscoveryResultSetMapper<String> {

    private static final String SOS_DESCRIBE_SENSOR_MAPPER_ERROR = "SOS_DESCRIBE_SENSOR_MAPPER_ERROR";

    public DescribeSensorMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema SOS_DESCRIBE_SENSOR_MAPPING_SCHEMA = new MappingSchema();
    
    private static Unmarshaller UNMARSHALLER ;

    
    static{
	try {
	    UNMARSHALLER = JAXBContext.newInstance(net.opengis.sensorml.v_2_0.ObjectFactory.class).createUnmarshaller();
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }
    
    

    @Override
    public MappingSchema getMappingSchema() {

	return SOS_DESCRIBE_SENSOR_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    // MDMetadata metadata = coreMetadata.getMDMetadata();
	    MIMetadata metadata = coreMetadata.getMIMetadata();

	    String platformCode = "";
	    String platformName = "";
	    try {
		platformCode = res.getExtensionHandler().getUniquePlatformIdentifier().get();
		platformName = metadata.getMIPlatform().getCitation().getTitle();
	    } catch (Exception e) {
	    }

	    Double latitude = 0.;// 41.5724
	    try {
		latitude = Double.parseDouble(metadata.getDataIdentification().getGeographicBoundingBox().getNorth().toString());
	    } catch (Exception e) {
	    }
	    Double longitude = 0.;// -111.8551
	    try {
		longitude = Double.parseDouble(metadata.getDataIdentification().getGeographicBoundingBox().getEast().toString());
	    } catch (Exception e) {
	    }

	    ExtensionHandler handler = res.getExtensionHandler();

	    Optional<String> uniqueAttributeIdentifier = handler.getUniqueAttributeIdentifier();

	    String procedureId = null;

	    String variableTitle = null;

	    try {
		variableTitle = metadata.getCoverageDescription().getAttributeTitle();
	    } catch (Exception e) {
	    }

	    String variableDescription = null;

	    try {
		variableDescription = metadata.getCoverageDescription().getAttributeDescription();
	    } catch (Exception e) {
	    }

	    String offeringId = null;
	    String foiID = handler.getUniquePlatformIdentifier().isPresent() ? handler.getUniquePlatformIdentifier().get() : null;

	    if (uniqueAttributeIdentifier.isPresent()) {
		procedureId = SOSUtils.createProcedureId(uniqueAttributeIdentifier.get());
		offeringId = SOSUtils.createOfferingId(uniqueAttributeIdentifier.get());
	    }

	    GSSource source = res.getSource();

	    DescribeSensorResponseType describeSensorResponseType = new DescribeSensorResponseType();
	    describeSensorResponseType.setProcedureDescriptionFormat("http://www.opengis.net/sensorml/2.0");
	    Description description = new Description();
	    describeSensorResponseType.getDescription().add(description);

	    SensorDescriptionType sensorDescription = new SensorDescriptionType();
	    description.setSensorDescription(sensorDescription);

	    ValidTime validTime = getValidTime(metadata.getDataIdentification().getTemporalExtent());
	    sensorDescription.setValidTime(validTime);

	    Data data = new Data();
	    sensorDescription.setData(data);

	    net.opengis.sensorml.v_2_0.ObjectFactory sensorFactory = new net.opengis.sensorml.v_2_0.ObjectFactory();

	    PhysicalSystemType pst = new PhysicalSystemType();
	    net.opengis.gml.v_3_2_1.CodeWithAuthorityType psIdentifier = new net.opengis.gml.v_3_2_1.CodeWithAuthorityType();
	    psIdentifier.setValue(procedureId);
	    pst.setIdentifier(psIdentifier);

	    JAXBElement<PhysicalSystemType> ps = sensorFactory.createPhysicalSystem(pst);

	    try {
		Iterator<Keywords> ki = metadata.getDataIdentification().getKeywords();
		while (ki.hasNext()) {
		    KeywordListPropertyType klpt = new KeywordListPropertyType();
		    KeywordListType klt = new KeywordListType();

		    Keywords keyword = (Keywords) ki.next();

		    Iterator<String> childKi = keyword.getKeywords();
		    while (childKi.hasNext()) {
			String key = (String) childKi.next();
			klt.getKeyword().add(key);
		    }
		    klpt.setKeywordList(klt);

		    pst.getKeywords().add(klpt);

		}

	    } catch (Exception e) {
	    }

	    IdentifierListPropertyType ilpt = new IdentifierListPropertyType();
	    pst.getIdentification().add(ilpt);

	    IdentifierListType ilt = new IdentifierListType();
	    ilpt.setIdentifierList(ilt);

	    addIdentifier(ilt, "urn:ogc:def:identifier:OGC:1.0:uniqueID", "uniqueID", procedureId);
	    if (variableDescription != null) {
		addIdentifier(ilt, "urn:ogc:def:identifier:OGC:1.0:longName", "longName", variableDescription);
	    }
	    if (variableTitle != null) {
		addIdentifier(ilt, "urn:ogc:def:identifier:OGC:1.0:shortName", "shortName", variableTitle);
	    }

	    net.opengis.sensorml.v_2_0.DescribedObjectType.ValidTime validTime2 = getValidTime2(
		    metadata.getDataIdentification().getTemporalExtent());

	    pst.getValidTime().add(validTime2);

	    Capabilities cap = new Capabilities();
	    cap.setName("offerings");
	    CapabilityListType clt = new CapabilityListType();
	    Capability c = new Capability();
	    c.setName("Offering for sensor " + variableTitle);
	    net.opengis.swecommon.v_2_0.ObjectFactory sFactory = new net.opengis.swecommon.v_2_0.ObjectFactory();
	    TextType tt = new TextType();
	    tt.setDefinition("http://www.opengis.net/def/offering/identifier");
	    tt.setLabel("Offering for sensor " + variableTitle);
	    tt.setValue(offeringId);
	    JAXBElement<? extends AbstractDataComponentType> jaxbData = sFactory.createText(tt);
	    c.setAbstractDataComponent(jaxbData);
	    clt.getCapability().add(c);
	    cap.setCapabilityList(clt);
	    pst.getCapabilities().add(cap);

	    FeaturesOfInterest foi = new FeaturesOfInterest();
	    FeatureListType flt = new FeatureListType();

	    flt.setDefinition("http://www.opengis.net/def/featureOfInterest/identifier");
	    flt.setLabel("featuresOfInterest");

	    net.opengis.gml.v_3_2_1.FeaturePropertyType fpt = new net.opengis.gml.v_3_2_1.FeaturePropertyType();
	    fpt.setHref(platformCode);
	    fpt.setTitle(platformName);
	    flt.getFeature().add(fpt);
	    foi.setFeatureList(flt);
	    pst.setFeaturesOfInterest(foi);

	    Marshaller marshaller = JAXBContext.newInstance(net.opengis.sensorml.v_2_0.ObjectFactory.class).createMarshaller();
	    marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new JAXBSOSPrefixMapper());
	    Document doc = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder().newDocument();
	    marshaller.marshal(ps, doc);

	    data.setAny(doc.getDocumentElement());

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBElement<DescribeSensorResponseType> jaxbElement = JAXBSOS.getInstance().getSWESFactory()
		    .createDescribeSensorResponse(describeSensorResponseType);
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
		    SOS_DESCRIBE_SENSOR_MAPPER_ERROR);
	}

    }

    private void addIdentifier(IdentifierListType list, String definition, String label, String value) {
	Identifier identifier = new Identifier();
	TermType term = new TermType();
	term.setDefinition(definition);
	term.setLabel(label);
	term.setValue(value);
	identifier.setTerm(term);
	list.getSMLIdentifier().add(identifier);

    }

    private net.opengis.sensorml.v_2_0.DescribedObjectType.ValidTime getValidTime2(TemporalExtent temporalExtent) {
	net.opengis.sensorml.v_2_0.DescribedObjectType.ValidTime validTime = new net.opengis.sensorml.v_2_0.DescribedObjectType.ValidTime();

	try {
	    JAXBElement<AbstractTimePrimitiveType> extent = temporalExtent.getElementType().getExtent().getAbstractTimePrimitive();

	    Marshaller marshaller = CommonContext.createMarshaller(true); // JAXBContext.newInstance(AbstractTimePrimitiveType.class).createMarshaller();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    marshaller.marshal(extent, baos);

	    String str = new String(baos.toByteArray());

	    str = str.replace("http://www.opengis.net/gml", "http://www.opengis.net/gml/3.2");

	    

	    StringReader reader = new StringReader(str);

	    Object object = UNMARSHALLER.unmarshal(reader);

	    if (object instanceof JAXBElement) {
		JAXBElement<?> jaxb = (JAXBElement) object;
		if (jaxb.getValue() instanceof net.opengis.gml.v_3_2_1.TimePeriodType) {
		    net.opengis.gml.v_3_2_1.TimePeriodType gtpt = (net.opengis.gml.v_3_2_1.TimePeriodType) jaxb.getValue();
		    validTime.setTimePeriod(gtpt);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    // TODO: handle exception
	}

	return validTime;
    }

    private ValidTime getValidTime(TemporalExtent temporalExtent) {
	ValidTime validTime = new ValidTime();

	try {
	    JAXBElement<AbstractTimePrimitiveType> extent = temporalExtent.getElementType().getExtent().getAbstractTimePrimitive();

	    Marshaller marshaller = CommonContext.createMarshaller(true); // .newInstance(AbstractTimePrimitiveType.class).createMarshaller();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    marshaller.marshal(extent, baos);

	    String str = new String(baos.toByteArray());

	    str = str.replace("http://www.opengis.net/gml", "http://www.opengis.net/gml/3.2");

	    Object object = JAXBSOS.getInstance().unmarshal(str);

	    if (object instanceof JAXBElement) {
		JAXBElement<?> jaxb = (JAXBElement) object;
		if (jaxb.getValue() instanceof AbstractTimeGeometricPrimitiveType) {
		    JAXBElement<AbstractTimeGeometricPrimitiveType> abstractTime = (JAXBElement<AbstractTimeGeometricPrimitiveType>) jaxb;
		    validTime.setAbstractTimeGeometricPrimitive(abstractTime);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    // TODO: handle exception
	}

	return validTime;
    }
}
