package eu.essi_lab.model.ontology;

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
import java.util.stream.Collectors;

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.d2k.D2KGSOntologyLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * @author ilsanto
 */
public abstract class GSPredicate implements IRI {

    /**
     * 
     */
    private static final long serialVersionUID = 8900303746298607721L;
    private transient SimpleValueFactory factory = SimpleValueFactory.getInstance();

    /**
     * Creates a new {@link GSPredicate} according to the provided <code>scheme</code> and <code>stringValue</code>.<br>
     * The <code>stringValue</code> must be the string representation of a predicate, according to the
     * {@link #stringValue()} method
     * which concatenates {@link #getNamespace()} and {@link #getLocalName()}, consequently the created predicate local
     * name is built by removing the {@link GSKnowledgeScheme#getNamespace()} of <code>scheme</code> from the provided
     * <code>stringValue</code>
     * 
     * @param scheme
     * @param stringValue
     * @return
     */
    public static GSPredicate create(GSKnowledgeScheme scheme, String stringValue) {

	String localName = stringValue.replace(scheme.getNamespace(), "");

	GSPredicate gsPredicate = new GSPredicate() {

	    private static final long serialVersionUID = -4190349436844045365L;

	    @Override
	    public String getNamespace() {

		return scheme.getNamespace();
	    }

	    @Override
	    public String getLocalName() {

		return localName;
	    }
	};

	return gsPredicate;
    }

    public List<IRI> inverseOf() {

	return getRelated(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_INVERSE_OF_LOCALNAME);
    }

    public List<IRI> subPropertyOf() {

	return getRelated(OntologyURIs.RDFS_NAMESPACE + OntologyURIs.RDFS_SUBPROPERTY_OF);
    }

    public List<IRI> equivalentOf() {

	return getRelated(OntologyURIs.OWL_NAMESPACE + OntologyURIs.OWL_EQUIVALENT_OF_LOCALNAME);
    }

    private List<IRI> getRelated(String related) {
	try {

	    return new D2KGSOntologyLoader()
		    .getLinkedPredicates(//
			    factory.createIRI(stringValue()), //
			    factory.createIRI(related))
		    .stream().//
		    map(s -> factory.createIRI(s)).collect(Collectors.toList());

	} catch (GSException e) {
	    e.log();
	}

	return new ArrayList<>();
    }

    @Override
    public String getNamespace() {
	return OntologyURIs.ESSI_D2K_NAMESPACE;
    }

    @Override
    public String stringValue() {
	return getNamespace() + getLocalName();
    }

    @Override
    public boolean equals(Object o) {

	if (!(o instanceof GSPredicate)) {
	    return false;
	}

	return ((GSPredicate) o).stringValue().equals(stringValue());
    }

    @Override
    public int hashCode() {
	return stringValue().hashCode();
    }

    @Override
    public String toString() {
	return stringValue();
    }
}
