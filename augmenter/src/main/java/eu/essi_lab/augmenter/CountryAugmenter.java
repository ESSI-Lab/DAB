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

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.GSResource;
public class CountryAugmenter extends ResourceAugmenter {

    /**
     * 
     */
    private static final long serialVersionUID = 7412866302816222900L;

    @JsonIgnore
    private static final String COUNTRY_AUGMENTER_TARGET_KEY = "COUNTRY_AUGMENTER_TARGET_KEY";

    public CountryAugmenter() {

	setLabel("Country augmenter");

	GSConfOptionString option = new GSConfOptionString();
	option.setLabel("Organization Name");
	option.setKey(COUNTRY_AUGMENTER_TARGET_KEY);
	getSupportedOptions().put(COUNTRY_AUGMENTER_TARGET_KEY, option);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource STARTED");

	String country = getSupportedOptions().get(COUNTRY_AUGMENTER_TARGET_KEY).getValue().toString();

	resource.getExtensionHandler().setCountry(Country.decode(country).getShortName());

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    public void onOptionSet(GSConfOption<?> option) throws GSException {

    }

    @Override
    public void onFlush() throws GSException {
    }
}
