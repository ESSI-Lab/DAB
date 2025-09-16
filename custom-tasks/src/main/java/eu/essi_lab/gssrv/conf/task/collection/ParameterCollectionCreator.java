package eu.essi_lab.gssrv.conf.task.collection;

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

import java.util.HashSet;

import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.whos.MQTTUtils;
import eu.essi_lab.lib.whos.WIS2Level10Topic;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.MetadataElement;

public class ParameterCollectionCreator extends SourceCollectionCreator {

    protected Queryable getGroupByQueryable() {
	return MetadataElement.OBSERVED_PROPERTY_URI;
    }

    protected String getMetadataIdentifier(String sourceIdentifier, String parameterURI) {
	String parameter = "";
	if (parameterURI != null && parameterURI.contains("/")) {
	    parameter = ":" + parameterURI.substring(parameterURI.lastIndexOf("/") + 1);
	}
	return "urn:wmo:md:" + sourceIdentifier + parameter;
    }

    protected String getAdditionalLevels(String parameterURI) {
	WHOSOntology ontology = new WHOSOntology();
	SKOSConcept concept = ontology.getConcept(parameterURI);
	WIS2Level10Topic wisLevel10 = WIS2Level10Topic.UNHARMONIZED;
	String level11 = parameterURI;
	if (concept != null) {
	    wisLevel10 = WIS2Level10Topic.decode(parameterURI);
	    level11 = concept.getPreferredLabel().getKey();
	}
	String level10 = wisLevel10.getId();
	String level9 = wisLevel10.getBroaderLevel().getId();
	return "/" + MQTTUtils.harmonizeTopicName(level9)//
		+ "/" + MQTTUtils.harmonizeTopicName(level10) //
		+ "/" + MQTTUtils.harmonizeTopicName(level11);
    }

    protected void addAdditionalElements(DatasetCollection dataset, String sourceId, String parameterURI) throws GSException {

	HashSet<PropertyResult> observedProperties = new HashSet<SourceCollectionCreator.PropertyResult>();
	PropertyResult property = new PropertyResult();
	property.setUri(parameterURI);
	observedProperties.add(property);
	addProperties(dataset, observedProperties);

    }

    protected String getTitle(GSSource source, String parameterURI) {
	WHOSOntology ontology = new WHOSOntology();
	SKOSConcept concept = ontology.getConcept(parameterURI);
	return "Observations from data provider " + source.getLabel() + ": " + concept.getPreferredLabel().getKey();
    }
}
