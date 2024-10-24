package eu.essi_lab.gssrv.health;

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

import java.util.Iterator;
import java.util.ServiceLoader;

import com.indeed.status.core.AbstractDependencyManager;
import com.indeed.status.core.ImmutableDependencyManagerParams;
import com.indeed.status.core.SimplePingableDependency;

import eu.essi_lab.configuration.ExecutionMode;

/**
 * @author Fabrizio
 */
public class GSDependencyManager extends AbstractDependencyManager {

    private static final GSDependencyManager INSTANCE = new GSDependencyManager();

    /**
     * 
     */
    private GSDependencyManager() {
	
	super(ImmutableDependencyManagerParams.builder().appName("ESSI Lab GS-Service").build());

	ExecutionMode mode = ExecutionMode.get();

	Iterator<GSPingMethod> methods = ServiceLoader.load(GSPingMethod.class).iterator();

	methods.forEachRemaining(method -> {

	    if (method.applicableTo(mode)) {

		SimplePingableDependency d1 = SimplePingableDependency.newBuilder()//
			.setId(method.getId())//
			.setDescription(method.getDescription())//
			.setPingMethod(method)//
			.setUrgency(method.getUrgency())//
			.build();

		addDependency(d1);
	    }
	});
    }

    /**
     * @return
     */
    public static GSDependencyManager getInstance() {

	return INSTANCE;
    }
}
