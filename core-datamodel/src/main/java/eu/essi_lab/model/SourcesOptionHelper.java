package eu.essi_lab.model;

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

import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.Subcomponent;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponentList;
import eu.essi_lab.model.configuration.option.SubComponentList;
public class SourcesOptionHelper {

    public static final String SOURCES_OPTION_KEY = "SOURCES_OPTION_KEY";

    private AbstractGSconfigurableComposed configurable;

    /**
     * @param configurable
     */
    public SourcesOptionHelper(AbstractGSconfigurableComposed configurable) {

	this.configurable = configurable;
    }

    /**
     * Put a {@link GSConfOptionSubcomponentList} having as allowed value and as value a
     * {@link SubComponentList} where each component is given by a source label/source id (all the
     * sources are pre-selected) of the supplied <code>sources</code>
     * 
     * @param sources the list selected {@link GSSource}s
     */
    public void putSourcesOption(List<GSSource> sources) {

	GSConfOptionSubcomponentList sourceOption = new GSConfOptionSubcomponentList();

	sourceOption.setLabel("Select sources");
	sourceOption.setKey(SOURCES_OPTION_KEY);

	SubComponentList sourcesList = new SubComponentList();

	sources.stream().filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.HARVESTED).//
		forEach(s -> {
		    Subcomponent source = new Subcomponent();
		    source.setLabel(s.getLabel());
		    source.setValue(s.getUniqueIdentifier());

		    sourcesList.add(source);
		});

	sourceOption.setValue(sourcesList); // set the value
	sourceOption.getAllowedValues().add(sourcesList);// set the allowed value

	configurable.getSupportedOptions().put(SOURCES_OPTION_KEY, sourceOption);
    }

    /**
     * @return
     */
    public List<Subcomponent> getSelectedSources() {

	return ((GSConfOptionSubcomponentList) configurable.getSupportedOptions().//
		get(SOURCES_OPTION_KEY)).//
			getValue().//
			getList().//
			stream().//
			collect(Collectors.toList());
    }

    /**
     * @return
     */
    public List<String> getSelectedSourcesIdentifiers() {

	return getSelectedSources().//
		stream().//
		map(c -> c.getValue()).//
		collect(Collectors.toList());
    }

    /**
     * 
     */
    public void clear() {

	configurable.getSupportedOptions().remove(SOURCES_OPTION_KEY);
    }
}
