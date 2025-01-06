package eu.essi_lab.accessor.sos._1_0_0;

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

import eu.essi_lab.accessor.sos.SOSIdentifierMangler;
import eu.essi_lab.accessor.sos.SOSProperties;
import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class SOSMapper extends eu.essi_lab.accessor.sos.SOSMapper {

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.SOS_1_0_0;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	return super.execMapping(originalMD, source);
    }
    
    /**
     * @return
     */
    protected String getProtocol() {

	return NetProtocols.SOS_1_0_0.getCommonURN();
    }

    /**
     * @param properties
     * @return
     */
    @Override
    protected String getTitle(SOSProperties properties) {

	String propertyName = properties.getProperty(SOSProperty.OBSERVED_PROPERTY_NAME);

	String foiName = properties.getProperty(SOSProperty.FOI_NAME);

	return propertyName + " at " + foiName;
    }

    /**
     * @param properties
     * @return
     */
    @Override
    protected SOSIdentifierMangler getMangler(SOSProperties properties) {

	SOSIdentifierMangler mangler = new SOSIdentifierMangler();

	// offering=
	mangler.setFeature(properties.getProperty(SOSProperty.OFFERING));

	// procedure=
	mangler.setProcedure(properties.getProperty(SOSProperty.PROCEDURE_HREF));

	// observedProperty=
	mangler.setObservedProperty(properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID));

	return mangler;
    }
    
    /**
     * @return
     */
    protected String getSOSVersion() {

	return "v.1.0.0";
    }
}
