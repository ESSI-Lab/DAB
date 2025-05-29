package eu.essi_lab.accessor.csw.mapper;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ComparableEntry;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Mapper from EEA SDI to set IN situ data
 *
 * @author roncella
 */
public class EEAMapper extends FileIdentifierMapper {

    private static final String EEA_MAPPER_ERROR = "EEA_MAPPER_ERROR";
    String HREF_ATTRIBUTE = "@*:href";

    public EEAMapper() {
	// empty constructor needed for service loader
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.EEA_NS_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

	try {
	    InputStream stream = new ByteArrayInputStream(originalMetadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(stream);

	    // extracting information from the keywords
	    // PARAMETER IDENTIFIERS (map of identifier;label couples)
	    Set<ComparableEntry<String, String>> parameters = new HashSet<>();
	    Node[] identifierNodes = reader
		    .evaluateNodes("//*:identificationInfo/*:MD_DataIdentification/*:descriptiveKeywords/*:MD_Keywords/*:keyword");
	    for (Node idNode : identifierNodes) {
		Node anchor = reader.evaluateNode(idNode, "*:Anchor");
		if (anchor != null) {
		    String keyword = anchor.getTextContent();
		    String keywordAttr = anchor.getAttributes().getNamedItem("xlink:href").getTextContent();
		    //String codeIdentifier = reader.evaluateString(anchor, "*:CharacterString");
		    if ((keywordAttr != null && !"".equals(keywordAttr) && keywordAttr.toLowerCase().contains("gemet/concept/4359")) || (keyword!= null && keyword.toLowerCase().contains("in situ"))) {
			extendedMetadataHandler.setIsInSitu();
		    }
		}

	    }

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    MDMetadata metadata = new MDMetadata(reader.asStream());
	    MIMetadata miMetadata = new MIMetadata(metadata.getElementType());
	    coreMetadata.setMIMetadata(miMetadata);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    EEA_MAPPER_ERROR, //
		    e);
	}
    }

}
