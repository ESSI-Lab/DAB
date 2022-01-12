package eu.essi_lab.gssrv.health;

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

import java.util.Iterator;
import java.util.ServiceLoader;

import com.indeed.status.core.AbstractDependencyManager;
import com.indeed.status.core.SimplePingableDependency;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.gssrv.health.components.IGSHealthCheckPingableComponent;
public class GSDependencyManager extends AbstractDependencyManager {

    private static GSDependencyManager instance;

    /**
     * 
     */
    private GSDependencyManager() {

	super("ESSI Lab GSService");

	ExecutionMode mode = new GIProjectExecutionMode().getMode();

	Iterator<IGSHealthCheckPingableComponent> iterator = ServiceLoader.load(IGSHealthCheckPingableComponent.class).iterator();

	iterator.forEachRemaining(igsComponentPingable -> {

	    if (igsComponentPingable.applicableTo(mode)) {

		SimplePingableDependency d1 = SimplePingableDependency.newBuilder()//
			.setId(igsComponentPingable.getId())//
			.setDescription(igsComponentPingable.getDescription())//
			.setPingMethod(igsComponentPingable)//
			.setUrgency(igsComponentPingable.getUrgency())//
			.build();

		addDependency(d1);
	    }
	});
    }

    /**
     * @return
     */
    public static GSDependencyManager getInstance() {

	if (instance == null) {
	    
	    instance = new GSDependencyManager();
	}

	return instance;
    }
}
