
package eu.essi_lab.accessor.wof.client;

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

import java.io.InputStream;

import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest;
import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest_1_1;

public class CUAHSIHISServerClient1_1 extends CUAHSIHISServerClient {

    public CUAHSIHISServerClient1_1(String endpoint) {
	super(endpoint);
    }

    /*
     * SOAP Actions
     */

    @Override
    protected String getGetSitesSOAPAction() {

	return "http://www.cuahsi.org/his/1.1/ws/GetSites";
    }

    @Override
    protected String getGetSitesObjectSOAPAction() {

	return "http://www.cuahsi.org/his/1.1/ws/GetSitesObject";
    }

    @Override
    protected String getGetVariableInfoSOAPAction() {

	return "http://www.cuahsi.org/his/1.1/ws/GetVariableInfoObject";
    }

    @Override
    protected String getGetSiteInfoSOAPAction() {

	return "http://www.cuahsi.org/his/1.1/ws/GetSiteInfo";
    }

    protected String getGetValuesSOAPAction() {

	return "http://www.cuahsi.org/his/1.1/ws/GetValuesObject";
    }

    /*
     * TEMPLATES
     */

    @Override
    protected InputStream loadGetSitesTemplate() throws Exception {

	return CUAHSIHISServerClient1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.1/GetSitesRequest.xml");
    }

    @Override
    protected InputStream loadGetSitesObjectTemplate() throws Exception {

	return CUAHSIHISServerClient1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.1/GetSitesObjectRequest.xml");
    }

    @Override
    protected InputStream loadGetSiteInfoTemplate() throws Exception {

	return CUAHSIHISServerClient1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.1/GetSiteInfoRequest.xml");
    }

    @Override
    protected InputStream loadGetSiteInfoObjectTemplate() throws Exception {

	return CUAHSIHISServerClient1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.1/GetSiteInfoObjectRequest.xml");
    }

    @Override
    protected GetValuesRequest getGetValuesRequest() {
	return new GetValuesRequest_1_1();
    }

}
