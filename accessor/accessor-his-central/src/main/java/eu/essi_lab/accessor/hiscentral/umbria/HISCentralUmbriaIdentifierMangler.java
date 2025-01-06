package eu.essi_lab.accessor.hiscentral.umbria;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * The WOF identifier mangler is primarily used by the WML mapper to create the file identifier and the distribution
 * online resource name for WOF resources.
 * 
 * @author boldrini
 */
public class HISCentralUmbriaIdentifierMangler extends KVPMangler {
    private static final String PLATFORM_KEY = "platform";
    private static final String PARAMETER_KEY = "parameter";
    private static final String RESOURCE_KEY = "resource";
    private static final String SOURCE_KEY = "source";
    //private static final String QUALITY_KEY = "quality";
    

    public HISCentralUmbriaIdentifierMangler() {
	super(";");
    }

    public void setPlatformIdentifier(String platformIdentifier) {
	setParameter(PLATFORM_KEY, platformIdentifier);
    }

    public String getPlatformIdentifier() {
	return getParameterValue(PLATFORM_KEY);
    }

    public void setParameterIdentifier(String parameterIdentifier) {
	setParameter(PARAMETER_KEY, parameterIdentifier);
    }

    public String getParameterIdentifier() {
	return getParameterValue(PARAMETER_KEY);
    }

    public void setResourceIdentifier(String methodIdentifier) {
	setParameter(RESOURCE_KEY, methodIdentifier);
    }

    public String getResourceIdentifier() {
	return getParameterValue(RESOURCE_KEY);
    }

    public void setSourceIdentifier(String sourceIdentifier) {
	setParameter(SOURCE_KEY, sourceIdentifier);
    }

    public String getSourceIdentifier() {
	return getParameterValue(SOURCE_KEY);
    }

//    public void setQualityIdentifier(String qualityIdentifier) {
//	setParameter(QUALITY_KEY, qualityIdentifier);
//    }
//
//    public String getQualityIdentifier() {
//	return getParameterValue(QUALITY_KEY);
//    }

}
