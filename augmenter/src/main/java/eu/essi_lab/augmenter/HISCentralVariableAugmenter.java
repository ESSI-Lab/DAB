package eu.essi_lab.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;

/**
 * It checks variable name to find out a correspondent concept URI in Hydro ontology. If
 * found, it adds it in a specific metadata field (attribute URI)
 * 
 * @author boldrini
 */
public class HISCentralVariableAugmenter extends ResourceAugmenter<AugmenterSetting> {

    public HISCentralVariableAugmenter() {

    }

    /**
     * @param setting
     */
    public HISCentralVariableAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("HIS-Central Variable augmentation of current resource STARTED");

	ExtensionHandler extensionHandler = resource.getExtensionHandler();
	Optional<String> uri = extensionHandler.getObservedPropertyURI();
	if (uri.isPresent()) {
	    GSLoggerFactory.getLogger(getClass()).info("Variable URI already present in original metadata");
	    return Optional.of(resource);
	}
	CoverageDescription coverageDescription = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.getCoverageDescription();
	String variable = null;
	if (coverageDescription != null) {
	    variable = coverageDescription.getAttributeTitle();
	}

	if (variable == null) {
	    GSLoggerFactory.getLogger(getClass())
		    .info("Unable to variable augment this resource.. no variable information in original metadata");
	    return Optional.of(resource);
	}
	try {

	    List<SKOSConcept> concepts = getConcepts(variable);
	    if (concepts == null || concepts.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("No concept found in hydro ontology for this variable name");
	    } else if (concepts.size() > 1) {
		String value = "";
		for (SKOSConcept concept : concepts) {
		    value += concept.getURI() + " (" + concept.getPreferredLabel() + ") ";
		}
		GSLoggerFactory.getLogger(getClass()).warn("More than one concept found in hydro ontology! ", value);

	    } else {
		SKOSConcept concept = concepts.get(0);
		extensionHandler.setObservedPropertyURI(concept.getURI());
		GSLoggerFactory.getLogger(getClass()).info("HIS-Central variable augmenter success");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).warn("Exception occurred unit augmenting this resource");
	    return Optional.of(resource);
	}

	GSLoggerFactory.getLogger(getClass()).warn("HIS-Central variable augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    public List<SKOSConcept> getConcepts(String variable) {
	HydroOntology ontology = new HISCentralOntology();
	List<SKOSConcept> ret = ontology.findConcepts(variable, false,true);
	return ret;
    }

    @Override
    protected String initName() {

	return "HIS-Central Variable augmenter";
    }

    @Override
    public String getType() {

	return "HISCentralVariableAugmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }
}
