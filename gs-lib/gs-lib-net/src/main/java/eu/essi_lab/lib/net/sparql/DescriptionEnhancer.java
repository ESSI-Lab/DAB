/**
 * 
 */
package eu.essi_lab.lib.net.sparql;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openrdf.model.impl.SimpleValueFactory;

import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;

/**
 * @author Fabrizio
 */
public abstract class DescriptionEnhancer {

    protected SimpleValueFactory factory;
    protected boolean collectLabels;
    protected List<String> labels;

    public DescriptionEnhancer() {

	factory = SimpleValueFactory.getInstance();
	labels = new ArrayList<>();
    }

    /**
     * 
     */
    public void collectLabels() {

	this.collectLabels = true;
    }

    /**
     * @return
     */
    public List<String> getLabels() {

	return labels;
    }

    /**
     * @param description
     * @param objectValue
     * @param predValue
     * @param objLan
     */
    public abstract void enhanceDescription(//
	    GSKnowledgeResourceDescription description, //
	    String objectValue, //
	    String predValue, //
	    Optional<String> objLan);
}
