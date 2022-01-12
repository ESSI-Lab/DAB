package eu.essi_lab.pdk.rsm.impl.xml.iso19139;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
public class GMD_ResultSetMapper extends DiscoveryResultSetMapper<Element> {

    /**
     * The schema uri of {@link #GMD_MAPPING_SCHEMA}
     */
    public static final String GMD_SCHEMA_URI = CommonNameSpaceContext.GMD_NS_URI;

    /**
     * The schema name of {@link #GMD_MAPPING_SCHEMA}
     */
    public static final String GMD_SCHEMA_NAME = "GMD";

    /**
     * The schema version of {@link #GMD_MAPPING_SCHEMA}
     */
    public static final String GMD_SCHEMA_VERSION = "1.0";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema GMD_MAPPING_SCHEMA = new MappingSchema();

    static {
	GMD_MAPPING_SCHEMA.setUri(GMD_SCHEMA_URI);
	GMD_MAPPING_SCHEMA.setName(GMD_SCHEMA_NAME);
	GMD_MAPPING_SCHEMA.setVersion(GMD_SCHEMA_VERSION);
    }

    private static final String ISO_19139_RES_SET_MAPPER_ERROR = "ISO_19139_RES_SET_MAPPER_ERROR";

    protected ElementSetType setType = null;

    protected List<QName> elementNames = null;

    public GMD_ResultSetMapper() {
    }

    public GMD_ResultSetMapper(ElementSetType setType) {
	this.setType = setType;
    }

    public GMD_ResultSetMapper(List<QName> elementNames) {
	this.elementNames = elementNames;
    }

    @Override
    public Element map(DiscoveryMessage message, GSResource res) throws GSException {

	Element originalElement = null;
	Element coreElement = null;

	try {

	    String originalSchemeURI = res.getOriginalMetadata().getSchemeURI();

	    String originalMd = res.getOriginalMetadata().getMetadata();
	    CoreMetadata coreMetadata = res.getHarmonizedMetadata().getCoreMetadata();

	    String targetNamespace = getTargetNamespace();
	    switch (targetNamespace) {
	    case CommonNameSpaceContext.GMD_NS_URI:

		if (originalSchemeURI.equals(CommonNameSpaceContext.GMD_NS_URI)) {
		    MDMetadata mdMetadata = new MDMetadata(originalMd);
		    originalElement = mdMetadata.asDocument(true).getDocumentElement();
		}

		coreElement = coreMetadata.getReadOnlyMDMetadata().asDocument(true).getDocumentElement();

		break;

	    case CommonNameSpaceContext.GMI_NS_URI:

		if (originalSchemeURI.equals(CommonNameSpaceContext.GMI_NS_URI)) {

		    MIMetadata miMetadata = new MIMetadata(originalMd);
		    originalElement = miMetadata.asDocument(true).getDocumentElement();
		}

		coreElement = coreMetadata.getMIMetadata().asDocument(true).getDocumentElement();

		break;
	    default:
		if (targetNamespace.equals(originalSchemeURI)) {
		    originalElement = new XMLDocumentReader(originalMd).getDocument().getDocumentElement();
		}
	    }

	    // get the subset if needed
	    coreElement = getSubset(coreElement);

	    if (targetNamespace.equals(CommonNameSpaceContext.GMD_NS_URI) && getMappingSchema().getVersion().contains("2007")) {
		coreElement = decorateElement(coreElement);
		originalElement = decorateElement(originalElement);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ISO_19139_RES_SET_MAPPER_ERROR);
	}

	switch (strategy) {
	case PRIORITY_TO_ORIGINAL_METADATA:
	    if (originalElement != null) {
		return originalElement;
	    }
	case PRIORITY_TO_CORE_METADATA:
	default:
	    return coreElement;
	}
    }

    public Element decorateElement(Element element) {
	return element;
    }

    private Element getSubset(Element coreElement) throws Exception {
	if (setType != null) {
	    coreElement = GMD_ElementName.subset(coreElement.getOwnerDocument(), setType).getDocumentElement();
	} else if (elementNames != null) {
	    // get only the specified elements
	    ArrayList<GMD_ElementName> list = new ArrayList<>();
	    for (QName qName : elementNames) {
		String localPart = qName.getLocalPart();
		GMD_ElementName decode = GMD_ElementName.decode(localPart);
		if (decode != null) {
		    list.add(decode);
		}
	    }
	    if (!list.isEmpty()) {
		coreElement = GMD_ElementName.subset(coreElement.getOwnerDocument(), list.toArray(new GMD_ElementName[] {}))
			.getDocumentElement();
	    }
	}
	return coreElement;
    }

    /**
     * @return
     */
    protected String getTargetNamespace() {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    @Override
    public MappingSchema getMappingSchema() {

	return GMD_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

}
