package eu.essi_lab.gssrv.conf.task.collection;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.lib.net.utils.whos.*;
import eu.essi_lab.cfga.gs.setting.WIS2Setting.RecordGranularity;
import eu.essi_lab.lib.whos.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.profiler.wis.WISUtils;

import java.util.*;

/**
 * Creates a collection for each harmonized observed property of a source, used with the
 * {@link RecordGranularity#OBSERVED_PROPERTY} granularity. Datasets without a harmonized observed property are not
 * described
 *
 * @author boldrini
 */
class ParameterCollectionCreator extends SourceCollectionCreator {

    /**
     * @param centreId
     */
    public ParameterCollectionCreator(String centreId) {
	super(centreId);
    }

    protected Queryable getGroupByQueryable() {
	return MetadataElement.OBSERVED_PROPERTY_URI;
    }

    protected String getMetadataIdentifier(String sourceIdentifier, String parameterURI) {
	return WISUtils.getMetadataIdentifier(centreId, sourceIdentifier, parameterURI);
    }

    protected void addAdditionalElements(DatasetCollection dataset, String sourceId, String parameterURI) throws GSException {

	HashSet<PropertyResult> observedProperties = new HashSet<SourceCollectionCreator.PropertyResult>();
	PropertyResult property = new PropertyResult();
	property.setUri(parameterURI);
	property.setName(getParameterLabel(parameterURI));
	observedProperties.add(property);
	addProperties(dataset, observedProperties);

    }

    protected String getTitle(GSSource source, String parameterURI) {
	return getParameterLabel(parameterURI) + " observations from " + normalize(source.getLabel());
    }

    /**
     * @param parameterURI
     * @return the english preferred label of the parameter concept or, if not found in the ontology, the parameter URI
     */
    private String getParameterLabel(String parameterURI) {
	SKOSConcept concept = new WHOSOntology().getConcept(parameterURI);
	if (concept != null) {
	    String label = concept.getPreferredLabel("en");
	    if (label != null && !label.isBlank()) {
		return label;
	    }
	}
	return parameterURI;
    }
}
