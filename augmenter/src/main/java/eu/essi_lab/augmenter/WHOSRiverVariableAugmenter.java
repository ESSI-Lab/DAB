package eu.essi_lab.augmenter;

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

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;

/**
 * This augmenter will call the parent augmenter, and will replace found general concepts with concepts related to
 * rivers. Moreover temperature is changed into air temperature, as intended for data providers in the La Plata river basin.
 * 
 * @author boldrini
 */
public class WHOSRiverVariableAugmenter extends WHOSVariableAugmenter {

    public WHOSRiverVariableAugmenter() {

	getSetting().setName("WHOS River Variable augmenter");
    }

    /**
     * @param setting
     */
    public WHOSRiverVariableAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public List<SKOSConcept> getConcepts(String variable) {
	List<SKOSConcept> ret = new ArrayList<SKOSConcept>();
	switch (variable.toLowerCase()) {
	case "gage height": // gage height workaround: as there is no "Gage Height" concept in the ontology yet
	    ret.add(new SKOSConcept("http://hydro.geodab.eu/hydro-ontology/concept/12"));
	    return ret;
	default:
	    break;
	}
	ret = super.getConcepts(variable);

	List<SKOSConcept> toRemove = new ArrayList<SKOSConcept>();
	List<SKOSConcept> toAdd = new ArrayList<SKOSConcept>();
	for (SKOSConcept concept : ret) {
	    switch (concept.getURI()) {
	    case "http://hydro.geodab.eu/hydro-ontology/concept/76": // discharge
		toRemove.add(concept);
		toAdd.add(new SKOSConcept("http://hydro.geodab.eu/hydro-ontology/concept/78")); // discharge, stream
		break;
	    case "http://hydro.geodab.eu/hydro-ontology/concept/3": // level
		toRemove.add(concept);
		toAdd.add(new SKOSConcept("http://hydro.geodab.eu/hydro-ontology/concept/11")); // level, stream 
		break;
	    case "http://hydro.geodab.eu/hydro-ontology/concept/40": // temperature
		toRemove.add(concept);
		toAdd.add(new SKOSConcept("http://hydro.geodab.eu/hydro-ontology/concept/49")); // temperature, air 
		break;
	    default:
		break;
	    }
	}
	ret.removeAll(toRemove);
	ret.addAll(toAdd);
	return ret;
    }
    
    @Override
    public String getType() {

	return "WHOSRiverVariableAugmenter";
    }

}
