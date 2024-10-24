package eu.essi_lab.profiler.sos.foi;

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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeWithAuthorityType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.sos._2_0.sams._2_0.ObjectFactory;
import eu.essi_lab.jaxb.sos._2_0.sams._2_0.SFSpatialSamplingFeatureType;
import eu.essi_lab.jaxb.sos._2_0.sams._2_0.ShapeType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

public class GetFeatureOfInterestMapper extends DiscoveryResultSetMapper<String> {

    private static final String SOS_FOI_MAPPER_ERROR = "SOS_FOI_MAPPER_ERROR";

    public GetFeatureOfInterestMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema SOS_FOI_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return SOS_FOI_MAPPING_SCHEMA;
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

	    GSSource source = res.getSource();

	    FeaturePropertyType feature = new FeaturePropertyType();

	    ObjectFactory sfactory = new ObjectFactory();
	    SFSpatialSamplingFeatureType sfft = new SFSpatialSamplingFeatureType();
	    CodeWithAuthorityType codeId = new CodeWithAuthorityType();
	    codeId.setValue(platformCode);
	    sfft.setIdentifier(codeId);
	    // eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType codeName = new
	    // eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType();
	    // List<CodeType> names = new ArrayList<>();
	    // CodeType codeName = new CodeType();
	    eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType codeName = new eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType();
	    codeName.setValue(platformName);
	    Collection<eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.CodeType> names = new ArrayList<>();

	    names.add(codeName);
	    sfft.getName().addAll(names);
	    ReferenceType referenceType = new ReferenceType();
	    referenceType.setHref("http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint");
	    sfft.setType(referenceType);

	    ShapeType shape = new ShapeType();
	    eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ObjectFactory gmlFactory = new eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.ObjectFactory();
	    PointType geometry = new PointType();
	    DirectPositionType pos = new DirectPositionType();
	    pos.setSrsName("http://www.opengis.net/def/crs/EPSG/0/4326");
	    pos.getValue().addAll(Arrays.asList(new Double[] { latitude, longitude }));
	    geometry.setPos(pos);
	    JAXBElement<PointType> jaxbGeometry = gmlFactory.createPoint(geometry);
	    shape.setAbstractGeometry(jaxbGeometry);
	    sfft.setShape(shape);

	    // JAXBElement<? extends AbstractFeatureType> abstractFeature =
	    // sfactory.createSFSpatialSamplingFeature(sfft);

	    eu.essi_lab.jaxb.sos._2_0.sf._2_0.ObjectFactory sffactory = new eu.essi_lab.jaxb.sos._2_0.sf._2_0.ObjectFactory();

	    JAXBElement<? extends AbstractFeatureType> abstractFeature = sffactory.createSFSamplingFeature(sfft);

	    feature.setAbstractFeature(abstractFeature);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBElement<FeaturePropertyType> jaxbFeature = gmlFactory.createFeatureProperty(feature);
	    JAXBSOS.getInstance().marshal(jaxbFeature, baos);
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
		    SOS_FOI_MAPPER_ERROR);
	}

    }
}
