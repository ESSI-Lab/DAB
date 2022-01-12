package eu.essi_lab.model.ontology.d2k.serialization;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.helpers.ContextStatementCollector;

import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.GSKnowledgeResourceLoader;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
public class GSStatementHandler extends ContextStatementCollector {

    private final List<GSKnowledgeResourceDescription> instances;
    private final Set<String> descriptionURIs;

    public GSStatementHandler() {

	super(SimpleValueFactory.getInstance());

	instances = new ArrayList<>();

	descriptionURIs = new HashSet<>();

    }

    @Override
    public void handleStatement(Statement st) {

	super.handleStatement(st);

	descriptionURIs.add(st.getSubject().stringValue());

    }

    @Override
    public void endRDF() {

	descriptionURIs.stream().forEach(uri ->

	findGSKNowledgeClass(uri)
		.ifPresent(kclass -> GSKnowledgeResourceLoader.instantiateKnowledgeResource(uri, kclass).ifPresent(resource -> {

		    GSKnowledgeResourceDescription description = new GSKnowledgeResourceDescription(resource);

		    getStatements().stream().filter(statement ->

	    statement.getSubject().stringValue().equals(uri)

	    ).forEach(statement -> addToDescription(description, statement));

		    instances.add(description);

		})));
    }

    private void addToDescription(GSKnowledgeResourceDescription description, Statement statement) {

	Optional<D2KGSPredicate> predicate = toGSPredicate(statement.getPredicate());

	predicate.ifPresent(p -> description.add(p, statement.getObject()));

    }

    private Optional<D2KGSPredicate> toGSPredicate(IRI predicate) {

	for (D2KGSPredicate gs_predicate : D2KGSPredicate.values()) {

	    if (predicate.stringValue().equals(gs_predicate.getGSPredicate().stringValue()))
		return Optional.of(gs_predicate);

	}

	return Optional.empty();
    }

    private Optional<String> findGSKNowledgeClass(String uri) {

	Optional<Statement> st = getStatements().stream().filter(predicate ->

	predicate.getSubject().stringValue().equals(uri)
		&& predicate.getPredicate().stringValue().equals(D2KGSPredicate.TYPE.getGSPredicate().stringValue())

	).findFirst();

	if (!st.isPresent())
	    return Optional.empty();

	return Optional.of(st.get().getObject().stringValue());
    }

    public List<GSKnowledgeResourceDescription> getParsedObjects() {

	return instances;
    }
}
