package eu.essi_lab.profiler.rest;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.ext.RuntimeDelegate;

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.OntologyPropertyBond;
import eu.essi_lab.messages.sem.SemanticBrowsing;
import eu.essi_lab.messages.sem.SemanticBrowsing.BrowsingAction;
import eu.essi_lab.messages.sem.SemanticOperation;
import eu.essi_lab.messages.sem.SemanticSearch;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.GSKnowledgeSchemeLoader;
import eu.essi_lab.model.ontology.OntologyObjectProperty;
import eu.essi_lab.pdk.wrt.SemanticRequestTransformer;

/**
 * @author Fabrizio
 */
public class RestSemanticRequestTransformer extends SemanticRequestTransformer {

    static {
	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    protected Optional<Bond> getUserBond(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	String search = parser.getValue("search", null);

	OntologyPropertyBond bond = null;
	if (search != null) {

	    bond = BondFactory.createOntologyPropertyBond(BondOperator.TEXT_SEARCH, OntologyObjectProperty.ABSTRACT, search);
	}

	return Optional.ofNullable(bond);
    }

    @Override
    protected SemanticOperation getOperation(WebRequest request) throws GSException {

	boolean isSearch = request.getUriInfo().//
		getPathSegments().//
		stream().//
		filter(p -> p.getPath().equals("search")).//
		findFirst().//
		isPresent();

	if (isSearch) {
	    return new SemanticSearch();
	}

	Optional<BrowsingAction> browsingAction = getBrowsingAction(request);

	return new SemanticBrowsing(browsingAction.get());
    }

    @Override
    protected List<String> getSearchTerms(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	String search = parser.getDecodedValue("searchTerms");

	if (search != null && !search.isEmpty()) {

	    return Arrays.asList(search);
	}

	return new ArrayList<>();
    }

    @Override
    protected Optional<String> getOntologyId(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	String ontologyId = parser.getDecodedValue("ontologyId");

	if (ontologyId != null && !ontologyId.isEmpty()) {

	    return Optional.of(ontologyId);
	}

	return Optional.empty();
    }

    @Override
    protected Optional<GSKnowledgeScheme> getScheme(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	String nameSpace = parser.getDecodedValue("schemeNameSpace");

	if (nameSpace != null && !nameSpace.isEmpty()) {

	    return GSKnowledgeSchemeLoader.loadScheme(nameSpace);
	}

	return Optional.empty();
    }

    @Override
    protected Optional<BrowsingAction> getBrowsingAction(WebRequest request) throws GSException {

	boolean isExpand = request.getUriInfo().//
		getPathSegments().//
		stream().//
		filter(p -> p.getPath().equals("expand")).//
		findFirst().//
		isPresent();

	boolean isCollapse = request.getUriInfo().//
		getPathSegments().//
		stream().//
		filter(p -> p.getPath().equals("collapse")).//
		findFirst().//
		isPresent();

	if (isExpand) {
	    return Optional.of(BrowsingAction.EXPAND);
	}

	if (isCollapse) {
	    return Optional.of(BrowsingAction.COLLAPSE);
	}

	return Optional.empty();
    }

    @Override
    protected Optional<String> getSubjectId(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	String subjectId = parser.getDecodedValue("subjectId");

	if (subjectId != null && !subjectId.isEmpty()) {

	    return Optional.of(subjectId);
	}

	return Optional.empty();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	Integer start = Integer.valueOf(parser.getValue("start", "1"));
	Integer count = Integer.valueOf(parser.getValue("count", "10"));

	return new Page(start, count);
    }

    @Override
    public String getProfilerType() {
	// TODO Auto-generated method stub
	return null;
    }

}
