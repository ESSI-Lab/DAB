package eu.essi_lab.profiler.cdi;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.profiler.csw.profile.StandardCSWISOProfile;

/**
 * @author Fabrizio
 */
public class CSWCDIProfile extends StandardCSWISOProfile {

    @Override
    public List<String> getSupportedOutputSchemas() {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.SDN_NS_URI);
	return ret;
    }

    @Override
    public DiscoveryResultSetMapper<Element> getResultSetMapper(String outputSchema, ElementSetType setType, List<QName> elementNames) {
	switch (outputSchema) {

	case CommonNameSpaceContext.SDN_NS_URI:

	    return new CDIResultSetMapper();
	}

	return null;
    }
}
