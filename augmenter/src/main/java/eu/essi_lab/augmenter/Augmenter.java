package eu.essi_lab.augmenter;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;
public abstract class Augmenter extends AbstractGSconfigurable {

    private static final String AUGMENTER_PRIORITY_KEY = "AUGMENTER_PRIORITY_KEY";

    /**
     * 
     */
    private static final long serialVersionUID = -5889610869314270715L;

    protected HashMap<String, GSConfOption<?>> options = new HashMap<>();

    public Augmenter() {

	setKey(getClass().getCanonicalName());
    }

    @JsonIgnore
    public int getPriority() {

	GSConfOption<?> option = getSupportedOptions().get(AUGMENTER_PRIORITY_KEY);

	Object value = option != null ? option.getValue() : null;
	if (value != null) {

	    return Integer.valueOf(value.toString());
	}

	return 1;
    }

    /**
     * Creates and put a String option with a range of integer values starting from 1
     * to the number of all the available augmenters. This initialization CANNOT be done in the constructor
     * since using the ServiceLoader on this class in that moment causes an initialization error
     */
    public void initPriorityOption() {

	ServiceLoader<Augmenter> loader = ServiceLoader.load(Augmenter.class);
	int count = (int) StreamUtils.iteratorToStream(loader.iterator()).count();

	List<String> values = IntStream.rangeClosed(1, count).//
		boxed().//
		map(i -> String.valueOf(i)).//
		collect(Collectors.toList());

	GSConfOptionString option = new GSConfOptionString();
	option.setLabel("Select priority of " + getLabel());
	option.setKey(AUGMENTER_PRIORITY_KEY);
	option.setAllowedValues(values);
	option.setValue(values.get(0));

	getSupportedOptions().put(AUGMENTER_PRIORITY_KEY, option);
    }

    /**
     * Augments the given resource (if possible) and returns the related {@link Optional}
     * 
     * @param resource resource to augment
     * @return
     * @throws GSException
     */
    public abstract Optional<GSResource> augment(GSResource resource) throws GSException;

    /**
     * Generates a list (possible empty) of {@link GSKnowledgeResourceDescription}s from the supplied
     * <code>resource</code>
     * 
     * @param resource
     * @return
     * @throws GSException
     */
    public abstract List<GSKnowledgeResourceDescription> generate(GSResource resource) throws GSException;

    /**
     * Returns all supported options for this GI-suite component.
     *
     * @return all supported {@link GSConfOption}s for this GI-suite component.
     */
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return options;
    }

}
