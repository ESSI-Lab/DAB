package eu.essi_lab.accessor.oaipmh;

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

import java.util.Optional;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import eu.essi_lab.jaxb.oaipmh.MetadataType;
import eu.essi_lab.jaxb.oaipmh.RecordType;
import eu.essi_lab.jaxb.oaipmh.StatusType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.DublinCoreResourceMapper;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import eu.essi_lab.ommdk.GMDResourceMapper;
import eu.essi_lab.ommdk.GMIResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;

/**
 * @author Fabrizio
 */
public class OAIPMHResourceMapper extends FileIdentifierMapper {

    private static final String OAI_PMH_RES_MAPPER_GET_RECORD_ERROR = "OAI_PMH_RES_MAPPER_GET_RECORD_ERROR";
    private static final String OAI_PMH_RES_MAPPER_ERROR = "OAI_PMH_RES_MAPPER_ERROR";

    public OAIPMHResourceMapper() {
	// nothing to do here
    }
    //

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Optional<GSResource> optResource = Optional.empty();

	RecordType record = getRecordType(originalMD.getMetadata());

	boolean deleted = record.getHeader().getStatus() == StatusType.DELETED;

	if (deleted) {

	    String identifier = record.getHeader().getIdentifier();

	    Dataset dataset = new Dataset();
	    dataset.setPublicId(identifier);
	    dataset.setSource(source);
	    
	    optResource = Optional.of(dataset);

	} else {

	    try {

		optResource = mapRecord(originalMD, record, source);

	    } catch (Exception ex) {

		throw GSException.createException( //
			getClass(), //
			ex.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			OAI_PMH_RES_MAPPER_ERROR, //
			ex);
	    }
	}

	if (optResource.isPresent()) {
	    // ----------------------------------------
	    //
	    // add the header identifier to the indexes
	    //
	    String identifier = record.getHeader().getIdentifier();

	    optResource.get().getPropertyHandler().setOAIPMHHeaderIdentifier(identifier);

	    // ----------------------------------------
	    //
	    // add the deleted tag if necessary
	    //
	    if (deleted) {
		optResource.get().getPropertyHandler().setIsDeleted(true);
	    }
	}

	return optResource.get();
    }

    /**
     * @param originalMD
     * @param record
     * @param dataset
     * @param source
     */
    protected Optional<GSResource> mapRecord(OriginalMetadata originalMD, RecordType record, GSSource source) throws Exception {

	DublinCoreResourceMapper dcMapper = new DublinCoreResourceMapper();
	GMDResourceMapper gmdMapper = new GMDResourceMapper();
	GMIResourceMapper gmiMapper = new GMIResourceMapper();

	Object any = record.getMetadata().getAny();
	if (any instanceof Element) {

	    Element el = (Element) any;
	    String metadata = new XMLDocumentReader(el.getOwnerDocument()).asString();
	    originalMD.setMetadata(metadata);

	    return Optional.of(dcMapper.map(originalMD, source));

	} else {

	    @SuppressWarnings("unchecked")
	    JAXBElement<MetadataType> jaxb = (JAXBElement<MetadataType>) record.getMetadata().getAny();

	    if (jaxb != null) {

		Object metadataType = jaxb.getValue();

		if (metadataType instanceof MDMetadataType) {

		    MDMetadataType type = (MDMetadataType) metadataType;
		    MDMetadata mdMetadata = new MDMetadata(type);

		    String metadata = mdMetadata.asString(true);
		    originalMD.setMetadata(metadata);

		    return Optional.of(gmdMapper.map(originalMD, source));

		} else if (metadataType instanceof MIMetadataType) {

		    MIMetadataType type = (MIMetadataType) metadataType;
		    MIMetadata miMetadata = new MIMetadata(type);

		    String metadata = miMetadata.asString(true);
		    originalMD.setMetadata(metadata);

		    return Optional.of(gmiMapper.map(originalMD, source));

		} else if (metadataType instanceof eu.essi_lab.jaxb.csw._2_0_2.RecordType) {

		    eu.essi_lab.jaxb.csw._2_0_2.RecordType type = (eu.essi_lab.jaxb.csw._2_0_2.RecordType) metadataType;
		    String metadata = CommonContext.asString(type, false);
		    originalMD.setMetadata(metadata);

		    return Optional.of(dcMapper.map(originalMD, source));
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve metadata from: " + originalMD.getMetadata());
	    }
	}
	
	return Optional.empty();
    }

    private RecordType getRecordType(String metadata) throws GSException {

	metadata = fixMetadata(metadata);
	
	try {
	    return CommonContext.unmarshal(metadata, RecordType.class);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    OAI_PMH_RES_MAPPER_GET_RECORD_ERROR, //
		    e);
	}
    }

    public String fixMetadata(String metadata) {
	return metadata;
    }

    @Override

    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.OAI_NS_URI;
    }

}
