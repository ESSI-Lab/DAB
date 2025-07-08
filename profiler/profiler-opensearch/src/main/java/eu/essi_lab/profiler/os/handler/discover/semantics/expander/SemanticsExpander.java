package eu.essi_lab.profiler.os.handler.discover.semantics.expander;

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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.profiler.os.handler.discover.semantics.SemanticSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.jena.util.FileUtils.isURI;

/**
 * @author Mattia Santoro
 */
public class SemanticsExpander {

    private final SemanticSource semanticSource;

    public SemanticsExpander(SemanticSource source) {
	this.semanticSource = source;
    }

    public List<String> expandSearchTerm(String searchTerm, SemanticExpansion expansion, List<String> langs) {

	if (langs == null || langs.size() == 0) {
	    langs = Arrays.asList("en");
	}

	List<String> tr_langs = langs;

	List<String> results = new ArrayList<>();
	results.add(searchTerm);

	List<URI> matchedUris = new ArrayList<>();

	if (isURI(searchTerm)) {

	    try {
		matchedUris.add(new URI(searchTerm));

	    } catch (URISyntaxException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	} else {

	    searchTerm = StringUtils.URLEncodeUTF8(searchTerm);

	    matchedUris = getConceptsMatchingKeyword(searchTerm);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Matched uris: {}", matchedUris.size());

	matchedUris.parallelStream().forEach(uri -> {

	    results.addAll(translateURI(uri, tr_langs));

	    List<URI> expanded = expandURI(uri, expansion);

	    GSLoggerFactory.getLogger(getClass()).debug("Expanded uris: {}", expanded.size());

	    expanded.parallelStream().forEach(euri -> {

		results.addAll(translateURI(euri, tr_langs));
	    });
	});
	
        List<String> distinct = results.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).debug("Results: {}", distinct);
	
	return distinct;
    }

    private List<URI> expandURI(URI uri, SemanticExpansion expansion) {
	return semanticSource.expandURI(uri, expansion);
    }

    private List<String> translateURI(URI uri, List<String> trLangs) {
	return semanticSource.translateURI(uri, trLangs);
    }

    private List<URI> getConceptsMatchingKeyword(String searchTerm) {
	return semanticSource.getConceptsMatchingKeyword(searchTerm);
    }
}
