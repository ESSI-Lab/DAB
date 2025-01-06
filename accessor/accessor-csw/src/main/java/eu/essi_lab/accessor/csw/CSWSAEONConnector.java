package eu.essi_lab.accessor.csw;

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

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordsResponse;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.GSException;

public class CSWSAEONConnector extends CSWGetConnector {

    /**
     * 
     */
    public static final String TYPE = "CSW SAEON Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * The CSW SAEON connector rewrites the unmarshal procedure, to insert "not jaxbizzed dublin core elements",
     * containing all the extended metadata not defined by the default CSW Dublin Core schema
     */
    protected GetRecordsResponse unmarshal(byte[] byteArray) throws JAXBException {
	try {

	    XMLDocumentReader reader = new XMLDocumentReader(new ByteArrayInputStream(byteArray));
	    Node[] nodes = reader.evaluateNodes("//*:Record");

	    JAXBContext jaxbContext = JAXBContext.newInstance(GetRecordsResponse.class);
	    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	    GetRecordsResponse ret = (GetRecordsResponse) unmarshaller.unmarshal(new ByteArrayInputStream(byteArray));

	    ret.getSearchResults().getAbstractRecords().clear();
	    ret.getSearchResults().getAnies().clear();
	    ret.getSearchResults().getAnies().addAll(Arrays.asList(nodes));

	    return ret;
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(CommonContext.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(CommonContext.class).error(e.getMessage(), e);
	}
	return null;
    }

    @Override
    protected String getReturnedMetadataSchema() throws GSException {
	return CommonNameSpaceContext.CSW_SAEON_NS_URI;
    }

}
