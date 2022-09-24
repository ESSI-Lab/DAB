package eu.essi_lab.accessor.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * CSW service endpoint: https://www.nodc.noaa.gov/archivesearch/csw?
 * This service has big metadata (some reaching 20MB xml files). It will hang with HTTP code 500 when the response
 * become big. E.g.
 * https://www.nodc.noaa.gov/archivesearch/csw?service=CSW&request=GetRecords&version=2.0.2&outputFormat=application/xml&outputSchema=http://www.isotc211.org/2005/gmd&ElementSetName=full&resultType=results&typeNames=gmd:MD_Metadata&startPosition=1821&maxRecords=20
 * In this case record 1830 was about 20MB and requesting it along with other caused HTTP code 500. The workaround here
 * is to reduce page size to 1 when errors due to big records are detected and restoring the user set page size
 * afterwards (the strategy has been implemented at upper levels of the hierarchy ({@link CSWConnector}) as it
 * seems to
 * be useful in general).
 * 
 * @author boldrini
 */
public class CSWNODCConnector extends CSWGetConnector {

    /**
     * 
     */
    public CSWNODCConnector() {

	getSetting().selectPageSize(20);
    }

    /**
     * 
     */
    public static final String TYPE = "CSW NODC Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * The CSW NODC always returns GMI Metadata according to the NODC profile (even if GMD Metadata is asked)
     */

    @Override
    protected String getReturnedMetadataSchema() {

	return CommonNameSpaceContext.NODC_NS_URI;
    }

    @Override
    protected String getRequestedMetadataSchema() throws GSException {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    @Override
    protected String getConstraintLanguageParameter() {
	return "";
    }

    /**
     * The CSW NODC connector applies only to the NODC catalogue
     */
    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("nodc")) {
	    return super.supports(source);
	} else {
	    return false;
	}

    }

}
