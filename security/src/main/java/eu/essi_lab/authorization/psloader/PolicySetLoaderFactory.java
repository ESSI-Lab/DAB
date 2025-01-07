/**
 * 
 */
package eu.essi_lab.authorization.psloader;

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

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Fabrizio
 */
public class PolicySetLoaderFactory {
    
    private static PolicySetLoader defaultLoader;

    /**
     * 
     * @param loader
     */
    public static void setDefaultLoader(PolicySetLoader loader) {
	
	PolicySetLoaderFactory.defaultLoader = loader;
    }

    /**
     * @return
     */
    public static PolicySetLoader createPolicySetLoader() {
	
	if(defaultLoader != null) {
	    
	    return defaultLoader;
	}

	ServiceLoader<PolicySetLoader> loader = ServiceLoader.load(PolicySetLoader.class);

	Iterator<PolicySetLoader> iterator = loader.iterator();

	while (iterator.hasNext()) {

	    return iterator.next();
	}

	throw new RuntimeException("Unable to find PolicySetLoader implementation");
    }
}
