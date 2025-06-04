package eu.essi_lab.messages.sem;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.UserBondMessage;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.rip.RuntimeInfoProvider;

/**
 * @author Fabrizio
 */
public class SemanticMessage extends UserBondMessage implements RuntimeInfoProvider {

    /**
     * 
     */
    private static final long serialVersionUID = 2698928629747379836L;
    private static final String SEARCH_OPERATION = "SEARCH_OPERATION";
    private static final String BROWSING_OPERATION = "BROWSING_OPERATION";
    private static final String BROWSING_ACTION = "BROWSING_ACTION";
    private static final String ONTOLOGY_ID = "ONTOLOGY_ID";
    private static final String SCHEME = "SCHEME";
    private static final String SUBJECT_ID = "SUBJECT_ID";
    private static final String SEARCH_TERMS = "SEARCH_TERMS";

    public SemanticMessage() {
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = super.provideInfo();

	getBrowsingOperation().ifPresent(op -> {

	    map.put(BROWSING_ACTION, Arrays.asList(op.getAction().name()));

	    op.getOntologyId().ifPresent(id -> map.put(ONTOLOGY_ID, Arrays.asList(id)));

	    map.put(SCHEME, Arrays.asList(op.getScheme().getNamespace()));

	    op.getSubjectId().ifPresent(id -> map.put(SUBJECT_ID, Arrays.asList(id)));

	    map.put(SEARCH_TERMS, op.getSearchTerms());
	});

	getSearchOperation().ifPresent(op -> {

	    op.getOntologyId().ifPresent(id -> map.put(ONTOLOGY_ID, Arrays.asList(id)));

	    map.put(SCHEME, Arrays.asList(op.getScheme().getNamespace()));

	    map.put(SEARCH_TERMS, op.getSearchTerms());
	});

	return map;
    }

    @Override
    public String getName() {

	return "SEMANTIC_MESSAGE";
    }

    /**
     * @return
     */
    public Optional<SemanticBrowsing> getBrowsingOperation() {

	SemanticBrowsing browsing = getHeader().get(BROWSING_OPERATION, SemanticBrowsing.class);

	if (browsing != null) {

	    return Optional.of(browsing);
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<SemanticSearch> getSearchOperation() {

	SemanticSearch search = getHeader().get(SEARCH_OPERATION, SemanticSearch.class);

	if (search != null) {

	    return Optional.of(search);
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public boolean isBrowsingOperationSet() {

	return getBrowsingOperation().isPresent();
    }

    /**
     * @return
     */
    public boolean isSearchOperationSet() {

	return getSearchOperation().isPresent();
    }

    /**
     * @param operation
     */
    public void setOperation(SemanticOperation operation) {

	if (operation instanceof SemanticBrowsing) {

	    getHeader().add(new GSProperty<SemanticBrowsing>(BROWSING_OPERATION, (SemanticBrowsing) operation));

	} else if (operation instanceof SemanticSearch) {

	    getHeader().add(new GSProperty<SemanticSearch>(SEARCH_OPERATION, (SemanticSearch) operation));
	}
    }
}
