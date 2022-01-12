package eu.essi_lab.profiler.oaipmh.handler.srvinfo;

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

import java.util.List;

import eu.essi_lab.jaxb.oaipmh.ListMetadataFormatsType;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.oaipmh.profile.OAIPMHProfile;
public class OAIPMHListMetadataFormatsHandler extends OAIPMHServiceInfoHandler {

    public static final String ISO19139_METADATA_PREFIX = "ISO19139";
    public static final String OAI_DC_METADATA_PREFIX = "oai_dc";

    /**
     * Here the optional identifier parameter is ignored, since the available formats can be applied to all the
     * resources. Furthermore there is no check on the identifier, if it's not valid no error is returned
     */
    @Override
    protected OAIPMHtype createResponseElement(WebRequest webRequest) {

	OAIPMHtype oai = new OAIPMHtype();

	List<MetadataFormatType> formats = OAIPMHProfile.getAllSupportedMetadataFormats();

	ListMetadataFormatsType mft = new ListMetadataFormatsType();
	formats.forEach(mft.getMetadataFormat()::add);

	oai.setListMetadataFormats(mft);

	return oai;
    }

    @Override
    protected VerbType getVerbType() {

	return VerbType.LIST_METADATA_FORMATS;
    }

}
