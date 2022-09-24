package eu.essi_lab.augmenter.reindexer;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class ResourceReindexerSetting extends AugmenterSetting {

    /**
     * 
     */
    private static final String RESOURCE_REINDEXER_TARGETS_OPTION_KEY = "resourceReindexerTargets";

    /**
     * 
     */
    public ResourceReindexerSetting() {

	Option<MetadataElement> option = OptionBuilder.get(MetadataElement.class).//
		withKey(RESOURCE_REINDEXER_TARGETS_OPTION_KEY).//
		withLabel("Metadata element to reindex").//
		withMultiSelection().//
		withValues(Arrays.asList(MetadataElement.values())).//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param object
     */
    public ResourceReindexerSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ResourceReindexerSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<MetadataElement> getSelectedElements() {

	return getOption(RESOURCE_REINDEXER_TARGETS_OPTION_KEY, MetadataElement.class).get().getSelectedValues();
    }

    /**
     * @param elements
     */
    public void setSelectedElements(List<MetadataElement> elements) {

	getOption(RESOURCE_REINDEXER_TARGETS_OPTION_KEY, MetadataElement.class).get().select(e -> elements.contains(e));
    }

}
