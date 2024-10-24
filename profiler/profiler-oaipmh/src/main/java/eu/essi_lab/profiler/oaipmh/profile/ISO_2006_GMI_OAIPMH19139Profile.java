package eu.essi_lab.profiler.oaipmh.profile;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMI_ResultSetMapper;

/**
 * @author Fabrizio
 */
public class ISO_2006_GMI_OAIPMH19139Profile extends OAIPMHProfile {

    @Override
    public MetadataFormatType getSupportedMetadataFormat() {

	MetadataFormatType type = new MetadataFormatType();
	type.setMetadataNamespace(CommonNameSpaceContext.GMI_NS_URI);
	type.setSchema("http://www.isotc211.org/2005/gmi/gmi.xsd");
	type.setMetadataPrefix("ISO19139-2006-GMI");

	return type;
    }

    @Override
    public DiscoveryResultSetMapper<Element> getResultSetMapper() {

	return new GMI_ResultSetMapper();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
