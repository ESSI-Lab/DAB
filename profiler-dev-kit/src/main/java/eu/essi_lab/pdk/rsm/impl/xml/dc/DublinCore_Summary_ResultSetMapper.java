package eu.essi_lab.pdk.rsm.impl.xml.dc;

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

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.SummaryRecordType;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

public class DublinCore_Summary_ResultSetMapper extends DublinCore_Full_ResultSetMapper {

    private static final String SUMMARY_RES_SET_MAPPPER_AS_DOCUMENT_ERROR = "SUMMARY_RES_SET_MAPPPER_AS_DOCUMENT_ERROR";

    @Override
    public Element map(DiscoveryMessage message, GSResource resource) throws GSException {

	String schemeURI = resource.getOriginalMetadata().getSchemeURI();
	String original = resource.getOriginalMetadata().getMetadata();

	if (strategy == MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA && //
		schemeURI.equals(CommonNameSpaceContext.CSW_NS_URI) && //
		isSummaryRecord(original)) {

	    try {
		return CommonContext.asDocument(original, true).getDocumentElement();

	    } catch (Exception e) {

		e.printStackTrace();
		throw GSException.createException( //
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			SUMMARY_RES_SET_MAPPPER_AS_DOCUMENT_ERROR);
	    }
	}

	SummaryRecordType summary = new SummaryRecordType();

	try {

	    addIdentifier(resource, null, null, summary);
	    addTitle(resource, null, null, summary);
	    addType(resource, null, null, summary);
	    addSubject(resource, null, summary);
	    addFormat(resource, null, summary);
	    addRelation(resource, null, summary);
	    addDate(resource, null, summary);
	    addDescription(resource, null, summary);
	    addBoundingBox(resource, null, null, summary);

	    return CommonContext.asDocument(summary, true).getDocumentElement();

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SUMMARY_RES_SET_MAPPPER_AS_DOCUMENT_ERROR);
	}
    }
}
