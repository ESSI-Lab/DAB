package eu.essi_lab.augmenter;

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

import java.util.Optional;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;

/**
 * It checks variable units name and abbreviation to find out a correspondent concept URI in WMO units ontology. If
 * found, it adds it in a specific metadata field (attribu
 * 
 * @author boldrini
 */
public class WHOSUnitsAugmenter extends ResourceAugmenter<AugmenterSetting> {

    public WHOSUnitsAugmenter() {

    }

    /**
     * @param setting
     */
    public WHOSUnitsAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("WHOS Units augmentation of current resource STARTED");

	ExtensionHandler extensionHandler = resource.getExtensionHandler();
	Optional<String> uri = extensionHandler.getAttributeUnitsURI();
	if (uri.isPresent()) {
	    GSLoggerFactory.getLogger(getClass()).info("Units URI already present in original metadata");
	    return Optional.of(resource);
	}
	String units = null;

	Optional<String> attributeUnits = extensionHandler.getAttributeUnits();
	if (attributeUnits.isPresent()) {
	    units = attributeUnits.get();
	} else {
	    Optional<String> attributeUnitsAbbreviation = extensionHandler.getAttributeUnitsAbbreviation();
	    if (attributeUnitsAbbreviation.isPresent()) {
		units = attributeUnitsAbbreviation.get();
	    }
	}

	if (units == null) {
	    GSLoggerFactory.getLogger(getClass()).info("Unable to unit augment this resource.. no unit information in original metadata");
	    return Optional.of(resource);
	}

	try {
	    WMOUnit unit = WMOOntology.decodeUnit(units);

	    if (unit != null) {
		extensionHandler.setAttributeUnitsURI(unit.getURI());
		GSLoggerFactory.getLogger(getClass()).info("WHOS units augmenter success");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).info("Exception occurred unit augmenting this resource");
	    return Optional.of(resource);
	}

	GSLoggerFactory.getLogger(getClass()).warn("WHOS unit augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    protected String initName() {

	return "WHOS Units augmenter";
    }

    @Override
    public String getType() {

	return "WHOSUnitsAugmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }
}
