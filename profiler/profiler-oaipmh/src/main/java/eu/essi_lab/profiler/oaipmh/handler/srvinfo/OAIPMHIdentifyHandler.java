package eu.essi_lab.profiler.oaipmh.handler.srvinfo;

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

import eu.essi_lab.jaxb.oaipmh.DeletedRecordType;
import eu.essi_lab.jaxb.oaipmh.GranularityType;
import eu.essi_lab.jaxb.oaipmh.IdentifyType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHRequestTransformer;

/**
 * Specific handler for the OAIPMH Identify request; it is selected by the {@link OAIPMHIdentifyFilter}. Example URL:
 * <a href=
 * "http://localhost:8085/gs-service/services/essi/oaipmh?verb=Identify">http://localhost:8085/gs-service/services/essi/
 * oaipmh?verb=Identify</a>
 * 
 * @author Fabrizio
 */
public class OAIPMHIdentifyHandler extends OAIPMHServiceInfoHandler {

    @Override
    protected OAIPMHtype createResponseElement(WebRequest webRequest) {

	IdentifyType type = new IdentifyType();
	type.setRepositoryName("DAB OAI-PMH 2.0 interface");
	type.setDeletedRecord(DeletedRecordType.TRANSIENT);
	type.setGranularity(GranularityType.YYYY_MM_DD_THH_MM_SS_Z);
	type.setProtocolVersion("2.0");
	type.getAdminEmail().add("test@my-org.com");
	String dateStamp = null;
	try {
	    dateStamp = OAIPMHRequestTransformer.getMinMaxDateStamp(webRequest.getRequestId(), BondOperator.MIN);
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to compute min/max date stamp");
	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}
	if (dateStamp != null) {
	    type.setEarliestDatestamp(dateStamp);
	}
	type.setBaseURL(webRequest.getUriInfo().getAbsolutePath().toString());

	OAIPMHtype oai = new OAIPMHtype();
	oai.setIdentify(type);

	return oai;
    }

    @Override
    protected VerbType getVerbType() {

	return VerbType.IDENTIFY;
    }

}
