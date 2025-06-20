package eu.essi_lab.lib.net.sa;

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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SAClient {
    private static final String DEFAULT_ENDPOINT = "https://semantics.bodc.ac.uk/api/analyse";
    private final String endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SAClient() {
	this(DEFAULT_ENDPOINT);
    }

    public SAClient(String endpoint) {
	this.endpoint = endpoint;
	this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	this.objectMapper = new ObjectMapper();
    }

    public List<SemanticAnalysisResponse> analyzeTerms(Collection<String> terms, List<String> matchTypes, List<String> matchProperties)
	    throws Exception {
	int max = 100;
	List<String> toAnalyze = new ArrayList<String>();
	List<SemanticAnalysisResponse> ret = new ArrayList<SAClient.SemanticAnalysisResponse>();
	System.out.println("Total terms: " + terms.size());
	int analyzed = 0;
	int errors = 0;
	for (String term : terms) {
	    toAnalyze.add(term);
	    if (toAnalyze.size() > max) {
		try {
		    SemanticAnalysisResponse response = getSimpleResponse(toAnalyze, matchTypes, matchProperties);
		    ret.add(response);
		    analyzed += toAnalyze.size();
		    System.out.println("Analyzed: " + analyzed);

		} catch (Exception e) {
		    e.printStackTrace();
		    errors++;
		}
		toAnalyze.clear();
	    }
	}
	if (!toAnalyze.isEmpty()) {
	    try {
		SemanticAnalysisResponse response = getSimpleResponse(toAnalyze, matchTypes, matchProperties);
		ret.add(response);
	    } catch (Exception e) {
		e.printStackTrace();
		errors++;
	    }
	}
	System.out.println("Analyzed all terms. Error response: " + errors);
	return ret;
    }

    private SemanticAnalysisResponse getSimpleResponse(List<String> terms, List<String> matchTypes, List<String> matchProperties)
	    throws Exception {
	terms.remove("LAO");

	SemanticAnalysisRequest request = new SemanticAnalysisRequest(terms , matchTypes, matchProperties);
	String requestBody = objectMapper.writeValueAsString(request);
//	System.out.println(requestBody);
	HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("Content-Type", "application/json")
		.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

	HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {
	    for (String term : terms) {
		System.out.println(term);
	    }
	    System.out.println(response.body());
	    throw new IOException("API request failed with status code: " + response.statusCode());
	}
	System.out.println("Response obtained");

	SemanticAnalysisResponse ret = objectMapper.readValue(response.body(), SemanticAnalysisResponse.class);

	return ret;
    }

    // Request and Response classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticAnalysisRequest {
	private Collection<String> terms;
	@JsonProperty("matchType")
	private List<String> matchTypes;
	@JsonProperty("matchProperties")
	private List<String> matchProperties;

	public SemanticAnalysisRequest() {
	}

	public SemanticAnalysisRequest(Collection<String> terms, List<String> matchTypes, List<String> matchProperties) {
	    this.terms = terms;
	    this.matchTypes = matchTypes;
	    this.matchProperties = matchProperties;
	}

	// Getters and setters
	public Collection<String> getTerms() {
	    return terms;
	}

	public void setTerms(List<String> terms) {
	    this.terms = terms;
	}

	public List<String> getMatchTypes() {
	    return matchTypes;
	}

	public void setMatchTypes(List<String> matchTypes) {
	    this.matchTypes = matchTypes;
	}

	public List<String> getMatchProperties() {
	    return matchProperties;
	}

	public void setMatchProperties(List<String> matchProperties) {
	    this.matchProperties = matchProperties;
	}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticAnalysisResponse {
	@JsonProperty("@context")
	private List<Object> context;
	@JsonProperty("@graph")
	private List<SearchAction> graph;
	@JsonProperty("search_terms_not_found")
	private List<String> searchTermsNotFound;
	private Stats stats;

	/**
	 * Holds URI and additionalType for a matched term.
	 */
	public static class TermMatchInfo {
	    private final String uri;
	    private final String additionalType;

	    public TermMatchInfo(String uri, String additionalType) {
		this.uri = uri;
		this.additionalType = additionalType;
	    }

	    public String getUri() {
		return uri;
	    }

	    public String getAdditionalType() {
		return additionalType;
	    }
	}

	/**
	 * Finds the URI and additionalType for a specific term in the semantic analysis results.
	 * 
	 * @param term The term to search for
	 * @return TermMatchInfo if found, null otherwise
	 */
	public TermMatchInfo findTermMatchInfo(String term) {
	    if (graph == null || term == null) {
		return null;
	    }
	    for (SearchAction action : graph) {
		if (action.getResult() != null) {
		    for (SearchResult result : action.getResult()) {
			if (term.equalsIgnoreCase(result.getName())) {
			    return new TermMatchInfo(result.getId(), result.getAdditionalType());
			}
		    }
		}
	    }
	    return null;
	}

	// Getters and setters
	public List<Object> getContext() {
	    return context;
	}

	public void setContext(List<Object> context) {
	    this.context = context;
	}

	public List<SearchAction> getGraph() {
	    return graph;
	}

	public void setGraph(List<SearchAction> graph) {
	    this.graph = graph;
	}

	public List<String> getSearchTermsNotFound() {
	    return searchTermsNotFound;
	}

	public void setSearchTermsNotFound(List<String> searchTermsNotFound) {
	    this.searchTermsNotFound = searchTermsNotFound;
	}

	public Stats getStats() {
	    return stats;
	}

	public void setStats(Stats stats) {
	    this.stats = stats;
	}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchAction {
	@JsonProperty("@type")
	private String type;
	private String query;
	private List<SearchResult> result;

	// Getters and setters
	public String getType() {
	    return type;
	}

	public void setType(String type) {
	    this.type = type;
	}

	public String getQuery() {
	    return query;
	}

	public void setQuery(String query) {
	    this.query = query;
	}

	public List<SearchResult> getResult() {
	    return result;
	}

	public void setResult(List<SearchResult> result) {
	    this.result = result;
	}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {
	@JsonProperty("@id")
	private String id;
	@JsonProperty("@type")
	private List<String> type;
	private String additionalType;
	private String inDefinedTermSet;
	private String matchProperty;
	private String matchType;
	private String name;
	@JsonProperty("skos:deprecated")
	private String deprecated;
	private String termCode;
	private String url;

	// Getters and setters
	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}

	public List<String> getType() {
	    return type;
	}

	public void setType(List<String> type) {
	    this.type = type;
	}

	public String getAdditionalType() {
	    return additionalType;
	}

	public void setAdditionalType(String additionalType) {
	    this.additionalType = additionalType;
	}

	public String getInDefinedTermSet() {
	    return inDefinedTermSet;
	}

	public void setInDefinedTermSet(String inDefinedTermSet) {
	    this.inDefinedTermSet = inDefinedTermSet;
	}

	public String getMatchProperty() {
	    return matchProperty;
	}

	public void setMatchProperty(String matchProperty) {
	    this.matchProperty = matchProperty;
	}

	public String getMatchType() {
	    return matchType;
	}

	public void setMatchType(String matchType) {
	    this.matchType = matchType;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getDeprecated() {
	    return deprecated;
	}

	public void setDeprecated(String deprecated) {
	    this.deprecated = deprecated;
	}

	public String getTermCode() {
	    return termCode;
	}

	public void setTermCode(String termCode) {
	    this.termCode = termCode;
	}

	public String getUrl() {
	    return url;
	}

	public void setUrl(String url) {
	    this.url = url;
	}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
	@JsonProperty("total_number_terms_found")
	private int totalNumberTermsFound;

	public int getTotalNumberTermsFound() {
	    return totalNumberTermsFound;
	}

	public void setTotalNumberTermsFound(int totalNumberTermsFound) {
	    this.totalNumberTermsFound = totalNumberTermsFound;
	}
    }

}
