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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.augmenter.AugmentersOptionHelper;
import eu.essi_lab.harvester.HarvestingComponent;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.exceptions.SkipProcessStepException;
import eu.essi_lab.model.resource.GSResource;
public class HarvesterPlan extends HarvestingComponent {

    private static final long serialVersionUID = 2317319735583254303L;

    private Map<String, GSConfOption<?>> supportedOptions = new HashMap<>();

    public HarvesterPlan() {

	AugmentersOptionHelper augmentersHelper = new AugmentersOptionHelper(this) {

	    protected AbstractGSconfigurable getComponent(Augmenter augmenter) {

		return new AugmenterComponent(augmenter);
	    }
	};

	augmentersHelper.putAugmenterOption();
    }

    @Override
    public String getLabel() {

	return "Default harvesting plan";
    }

    @Override
    @JsonIgnore
    public void apply(GSResource resource) throws HarvesterComponentException, DuplicatedResourceException, ConflictingResourceException {

	List<HarvestingComponent> harvestingComponents = getHarvestingComponents();

	HarvestingComponent current = null;

	try {
	    for (HarvestingComponent component : harvestingComponents) {

		current = component;

		GSLoggerFactory.getLogger(getClass()).trace(//
			"Application of {} on resource {} STARTED", //
			component.getClass().getSimpleName(), //
			resource);

		component.setAccessor(getAccessor());
		component.setResumptionToken(getResumptionToken());
		component.setSourceStorage(getSourceStorage());
		component.setHarvestingProperties(getHarvestingProperties());
		component.setIsRecovering(isRecovering());
		component.setIsFirstHarvesting(isFirstHarvesting());
		component.setIsIncrementalHarvesting(isIncrementalHarvesting());

		component.apply(resource);
	    }
	} catch (SkipProcessStepException spse) {
	    //
	    // next harvesting component application is skipped and Harvester
	    // continues with the next resource
	    //
	    throw spse;

	} finally {

	    GSLoggerFactory.getLogger(getClass()).trace( //
		    "Application of {} on resource {} ENDED", //
		    current.getClass().getSimpleName(), //
		    resource);
	}
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return supportedOptions;
    }

    /**
     * Makes a cast of the config components in to harvesting components, and if present more than 1 AugmenterComponent,
     * sort them according
     * to their Augmenter priority
     */
    @JsonIgnore
    private List<HarvestingComponent> getHarvestingComponents() {

	HarvestingComponent[] components = getConfigurableComponents().//
		values().//
		stream().//
		map(c -> (HarvestingComponent) c).//
		collect(Collectors.toList()).toArray(new HarvestingComponent[] {});

	List<AugmenterComponent> augmenters = getConfigurableComponents().//
		values().//
		stream().//
		filter(v -> v instanceof AugmenterComponent).//
		map(c -> (AugmenterComponent) c).//
		sorted((c1, c2) -> Integer.compare(c1.getAugmenter().getPriority(), c2.getAugmenter().getPriority())).//
		collect(Collectors.toList());

	if (augmenters.size() >= 2) {

	    int startIndex = 0;

	    // finds the first augmenter component and keeps the index
	    for (int i = 0; i < components.length; i++) {

		HarvestingComponent comp = components[i];
		if (comp instanceof AugmenterComponent) {

		    startIndex = i;
		    break;
		}
	    }

	    // replaces the augmenter components according to the augmenters
	    // priority. if the plan contains more than 1 augmenter components they are contiguous
	    // so this is safe
	    for (AugmenterComponent comp : augmenters) {

		components[startIndex] = comp;
		startIndex++;
	    }
	}

	return Arrays.asList(components);
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (AugmentersOptionHelper.isAugmentersOptionSet(opt)) {

	    AugmentersOptionHelper augmentersHelper = new AugmentersOptionHelper(this) {

		protected AbstractGSconfigurable getComponent(Augmenter augmenter) {

		    return new AugmenterComponent(augmenter);
		}
	    };

	    augmentersHelper.handleOnOptionSet(opt);
	    return;
	}
    }

    @Override
    public void onFlush() throws GSException {
    }
}
