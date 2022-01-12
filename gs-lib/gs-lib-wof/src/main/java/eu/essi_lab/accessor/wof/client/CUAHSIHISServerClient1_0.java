
package eu.essi_lab.accessor.wof.client;

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

import java.io.InputStream;

import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest;
import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest_1_0;

public class CUAHSIHISServerClient1_0 extends CUAHSIHISServerClient {
    public CUAHSIHISServerClient1_0(String endpoint) {
	super(endpoint);
    }

    protected String getGetValuesSOAPAction() {

	return "http://www.cuahsi.org/his/1.0/ws/GetValuesObject";
    }

    @Override
    protected String getGetSitesSOAPAction() {

	return "http://www.cuahsi.org/his/1.0/ws/GetSites";
    }

    @Override
    protected String getGetSitesObjectSOAPAction() {

	return "http://www.cuahsi.org/his/1.0/ws/GetSitesObject";
    }

    @Override
    protected String getGetVariableInfoSOAPAction() {

	return "http://www.cuahsi.org/his/1.0/ws/GetVariableInfoObject";
    }

    @Override
    protected String getGetSiteInfoSOAPAction() {

	return "http://www.cuahsi.org/his/1.0/ws/GetSiteInfo";
    }

    /*
     * TEMPLATES
     */
    
    @Override
    protected InputStream loadGetSitesTemplate() throws Exception {

	return CUAHSIHISServerClient1_0.class.getClassLoader().getResourceAsStream("cuahsi/1.0/GetSitesRequest.xml");
    }

    @Override
    protected InputStream loadGetSitesObjectTemplate() throws Exception {

	return CUAHSIHISServerClient1_0.class.getClassLoader().getResourceAsStream("cuahsi/1.0/GetSitesObjectRequest.xml");
    }

    @Override
    protected InputStream loadGetSiteInfoTemplate() throws Exception {

	return CUAHSIHISServerClient1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.0/GetSiteInfoRequest.xml");
    }

    @Override
    protected InputStream loadGetSiteInfoObjectTemplate() throws Exception {

	return CUAHSIHISServerClient1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.0/GetSiteInfoObjectRequest.xml");
    }

    @Override
    protected GetValuesRequest getGetValuesRequest() {
	return new GetValuesRequest_1_0();
    }

    // @Override
    // protected XMLDocument loadGetVariableInfoTemplate() throws Exception {
    //
    // return new
    // XMLDocument(HydroServerClient1_0.class.getClassLoader().getResourceAsStream("cuahsi/1.0/GetVariableInfoRequest.xml"));
    // }
    //
    // @Override
    // protected XMLDocument loadGetSitesInfoTemplate() throws Exception {
    // return new
    // XMLDocument(HydroServerClient1_0.class.getClassLoader().getResourceAsStream("cuahsi/1.0/GetSitesInfoRequest.xml"));
    // }
    //
    // @Override
    // protected GetValuesRequest getGetValuesRequest() throws SAXException, IOException, ParserConfigurationException {
    // return new GetValuesRequest_1_0();
    // }
    //
    // @Override
    // public String getGetVariableInfoXPath() {
    // return "//*[local-name(.)='GetVariableInfoObjectResponse']";
    // }
    //

}
