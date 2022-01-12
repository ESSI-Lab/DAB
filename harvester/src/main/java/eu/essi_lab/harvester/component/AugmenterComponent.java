package eu.essi_lab.harvester.component;

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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.harvester.HarvestingComponent;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
public class AugmenterComponent extends HarvestingComponent {

    /**
     * 
     */
    private static final long serialVersionUID = 3159688783069311163L;
    private static final String AUGMENTER_KEY = "AUGMENTER_KEY";

    public AugmenterComponent() {
	/**
	 * Empty constructor. Mandatory due serialization.
	 */
    }

    public AugmenterComponent(Augmenter augmenter) {
	getConfigurableComponents().put(AUGMENTER_KEY, augmenter);
    }

    @JsonIgnore
    public Augmenter getAugmenter() {

	return (Augmenter) getConfigurableComponents().get(AUGMENTER_KEY);
    }

    @Override
    public void apply(GSResource resource) throws HarvesterComponentException {

	Augmenter augmenter = (Augmenter) getConfigurableComponents().get(AUGMENTER_KEY);

	try {
	    augmenter.augment(resource);

	} catch (GSException e) {

	    throw new HarvesterComponentException(e);
	}
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return new HashMap<>();
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
    }

    @Override
    public void onFlush() throws GSException {
    }

}
