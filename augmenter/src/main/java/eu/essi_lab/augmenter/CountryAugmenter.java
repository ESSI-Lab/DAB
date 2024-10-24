package eu.essi_lab.augmenter;

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

import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.GSResource;

/**
 * sets the country metadata element
 * 
 * @author boldrini
 */
public class CountryAugmenter extends ResourceAugmenter<CountryNameSetting> {

    /**
     * 
     */
    public CountryAugmenter() {
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource STARTED");

	String country = getSetting().getCountryName().orElse(null);

	if (country != null) {

	    resource.getExtensionHandler().setCountry(Country.decode(country).getShortName());
	}
	
	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    public String getType() {

	return "CountryAugmenter";
    }

    @Override
    protected CountryNameSetting initSetting() {

	return new CountryNameSetting();
    }

    @Override
    protected String initName() {

	return "Country augmenter";
    }
}
