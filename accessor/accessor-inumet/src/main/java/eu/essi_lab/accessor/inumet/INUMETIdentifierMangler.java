package eu.essi_lab.accessor.inumet;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * 
 * @author boldrini
 */
public class INUMETIdentifierMangler extends KVPMangler {
    private static final String PLATFORM_KEY = "platform";
    private static final String PARAMETER_KEY = "parameter";

    public INUMETIdentifierMangler() {
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

}
