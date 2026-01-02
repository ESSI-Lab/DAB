/**
 * 
 */
package eu.essi_lab.profiler.csw;

import java.util.Optional;

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

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class CSWRequestUtils {

    /**
     * @param request
     * @return
     */
    public static boolean isGetRecordsFromGET(WebRequest request) {

	if (request.isGetRequest()) {

	    Optional<String> queryString = request.getURLDecodedQueryString();

	    if (queryString.isPresent() && !queryString.get().isBlank()) {

		return queryString.get().toLowerCase().contains("request=getrecords");
	    }
	}

	return false;
    }

    /**
     * @param webRequest
     * @return
     * @throws Exception
     */
    public static boolean isGetRecordsFromPOST(WebRequest webRequest) throws GSException {

	return getGetRecordFromPOST(webRequest) != null;
    }

    /**
     * @param request
     * @return
     */
    public static boolean isGetRecordByIdFromGET(WebRequest request) {

	if (request.isGetRequest()) {

	    Optional<String> queryString = request.getURLDecodedQueryString();

	    if (queryString.isPresent() && !queryString.get().isBlank()) {

		return queryString.get().toLowerCase().contains("request=getrecordbyid");
	    }
	}

	return false;
    }

    /**
     * @param webRequest
     * @return
     * @throws GSException
     * @throws Exception
     */
    public static boolean isGetRecordByIdFromPOST(WebRequest webRequest) throws GSException {

	try {

	    if (webRequest.isPostRequest()) {

		XMLDocumentReader reader = new XMLDocumentReader(webRequest.getBodyStream().clone());
		reader.setNamespaceContext(new CommonNameSpaceContext());

		return reader.evaluateBoolean("exists(//csw:GetRecordById)");
	    }
	} catch (Exception ex) {

	    throw GSException.createException(CSWRequestUtils.class, "CSWRequestUtilsIsGetRecordByIdFromPOSTError", ex);
	}

	return false;
    }

    /**
     * @param webRequest
     * @return
     * @throws Exception
     */
    public static GetRecords getGetRecordFromPOST(WebRequest webRequest) throws GSException {

	try {

	    if (webRequest.isPostRequest()) {

		XMLDocumentReader reader = new XMLDocumentReader(webRequest.getBodyStream().clone());
		reader.setNamespaceContext(new CommonNameSpaceContext());

		if (reader.evaluateBoolean("exists(//csw:GetRecords)")) {

		    GetRecords getRecords = CommonContext.unmarshal(webRequest.getBodyStream().clone(), GetRecords.class);
		    return getRecords;
		}
	    }
	} catch (Exception ex) {

	    throw GSException.createException(CSWRequestUtils.class, "CSWRequestUtilsIsGetRecordByIdFromPOSTError", ex);

	}

	return null;
    }
}
