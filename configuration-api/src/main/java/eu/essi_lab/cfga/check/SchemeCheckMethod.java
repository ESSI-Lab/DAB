/**
 * 
 */
package eu.essi_lab.cfga.check;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.scheme.Scheme;
import eu.essi_lab.cfga.scheme.SchemeItem;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SchemeCheckMethod implements CheckMethod {

    /**
     * @author Fabrizio
     */
    public enum CheckMode {

	/**
	 * This check fails if the given {@link Configuration} do not contains one or more required
	 * {@link SchemeItem}.<br>
	 * If this check fail, the settings returned in the {@link CheckResponse#getSettings()} must be added to the
	 * configuration
	 */
	MISSING_SETTINGS,

	/**
	 * This check fails if the given {@link Configuration} contains one or more redundant {@link SchemeItem}.<br>
	 * If this check fail, the settings returned in the {@link CheckResponse#getSettings()} must be removed from the
	 * configuration
	 */
	REDUNDANT_SETTINGS,
    }

    private CheckMode checkMode;
    private List<? extends SchemeItem> descriptors;

    /**
     * 
     */
    public SchemeCheckMethod() {

	setCheckMode(CheckMode.MISSING_SETTINGS);
    }

    /**
     * @param items
     */
    public void setItems(List<? extends SchemeItem> items) {

	this.descriptors = items;
    }

    /**
     * @param scheme
     */
    public void setScheme(Scheme scheme) {

	this.descriptors = scheme.getItems();
    }

    /**
     * @param checkMode
     */
    public void setCheckMode(CheckMode checkMode) {

	this.checkMode = checkMode;
    }

    @Override
    public CheckResponse check(Configuration configuration) {

	GSLoggerFactory.getLogger(getClass()).info("Scheme check method {} STARTED", checkMode);

	CheckResponse response = new CheckResponse(getName());

	List<Setting> outSettings = new ArrayList<Setting>();

	List<SchemeItem> required = descriptors.//
		stream().//
		filter(d -> d.required() && !d.getDescriptors().isEmpty()).//
		collect(Collectors.toList());

	switch (checkMode) {

	case MISSING_SETTINGS -> {

	    for (SchemeItem item : required) {

		List<Function<Setting, Boolean>> functions = item.getDescriptors().//
			stream().//
			map(d -> d.describe()).//
			collect(Collectors.toList());

		boolean missing = configuration.list().//
			stream().//
			filter(s -> functions.stream().noneMatch(f -> f.apply(s))).//
			count() <= configuration.size();

		if (missing) {

		    item.getDescriptors().stream().//
			    filter(d -> configuration.list().stream().noneMatch(s -> d.describe().apply(s))).//
			    forEach(d -> {

				Setting setting = d.create().get().get();

				outSettings.add(setting);
				response.getMessages().add("Detected missing setting: " + setting.getName());
			    });
		}
	    }
	}

	case REDUNDANT_SETTINGS -> {

	    for (Setting setting : configuration.list()) {

		boolean optionalSetting = descriptors.//
			stream().//
			filter(d -> !d.required() && !d.getDescriptors().isEmpty()).//
			filter(i -> i.getDescriptors().stream().anyMatch(d -> d.describe().apply(setting))).//
			findFirst().//
			isPresent();

		if (!optionalSetting) {

		    boolean redundant = required.stream().//
			    filter(i -> i.getDescriptors().stream().noneMatch(d -> d.describe().apply(setting))).//
			    count() == required.size();

		    if (redundant) {

			outSettings.add(setting);
			response.getMessages().add("Detected redundant setting: " + setting.getName());
		    }
		}
	    }
	}
	}

	if (!outSettings.isEmpty()) {

	    response.getSettings().addAll(outSettings);
	    response.setCheckResult(CheckResult.CHECK_FAILED);
	}

	GSLoggerFactory.getLogger(getClass()).info("Scheme check method {} ENDED", checkMode);

	return response;
    }
}
