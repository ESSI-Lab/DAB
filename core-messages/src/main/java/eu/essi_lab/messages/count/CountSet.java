package eu.essi_lab.messages.count;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.messages.termfrequency.TermFrequencyMap;

/**
 * A count set collecting pairs of (source identifier/count) and the exceptions generated during a count, listed per
 * source
 *
 * @author boldrini
 */
public class CountSet extends AbstractCountResponse {

    private HashMap<String, SimpleEntry<String, DiscoveryCountResponse>> countPairs;
    private TermFrequencyMap mergedMap;
    private int pageCount;
    private int pageIndex;

    public CountSet() {
	countPairs = new HashMap<>();
    }

    /**
     * @param pageCount
     */
    public void setPageCount(int pageCount) {

	this.pageCount = pageCount;
    }

    /**
     * @return
     */
    public int getPageCount() {

	return pageCount;
    }

    /**
     * @param pageIndex
     */
    public void setPageIndex(int pageIndex) {

	this.pageIndex = pageIndex;
    }

    /**
     * @return
     */
    public int getPageIndex() {

	return pageIndex;
    }

    /**
     * Returns the {@link TermFrequencyMap} resulting from the merging of the {@link TermFrequencyMap}s retrieved by all
     * the
     * <code>&lt;String, CountResult&gt;</code> pairs
     *
     * @param maxItemsCount
     * @return
     * @see TermFrequencyMap#merge(TermFrequencyMap, int)
     * @see CountSet#addCountPair(SimpleEntry)
     */
    public Optional<TermFrequencyMap> mergeTermFrequencyMaps(int maxItemsCount) {

	if (mergedMap == null) {

	    Collection<SimpleEntry<String, DiscoveryCountResponse>> values = countPairs.values();

	    for (SimpleEntry<String, DiscoveryCountResponse> countResponse : values) {

		Optional<TermFrequencyMap> op = countResponse.getValue().getTermFrequencyMap();

		if (op.isPresent()) {
		    TermFrequencyMap currentMap = op.get();

		    if (mergedMap == null) {
			mergedMap = currentMap;
		    } else {
			mergedMap = currentMap.merge(mergedMap, maxItemsCount);
		    }
		}
	    }
	}

	return Optional.ofNullable(mergedMap);
    }

    public void addCountPair(SimpleEntry<String, DiscoveryCountResponse> pair) {

	countPairs.put(pair.getKey(), pair);
    }

    public Set<String> getSourceIdentifiers() {
	Set<String> ret = new HashSet<>();
	ret.addAll(countPairs.keySet());
	return ret;
    }

    /**
     * Returns the count result of the supplied <code>sourceIdentifier</code>
     *
     * @param sourceIdentifier
     * @return
     */
    public Integer getCount(String sourceIdentifier) {
	SimpleEntry<String, DiscoveryCountResponse> pair = countPairs.get(sourceIdentifier);
	if (pair == null) {
	    return null;
	}
	return pair.getValue().getCount();
    }

    /**
     * Returns the total count of {@link #getCount(String)} invoked with the source identifier of all the
     * <code>&lt;String,
     * CountResult&gt;</code> pairs
     *
     * @return
     * @see #getCount(String)
     * @see CountSet#addCountPair(SimpleEntry)
     */
    public int getCount() {

	int totalCount = 0;
	Set<String> keySet = countPairs.keySet();
	for (String sourceIdentifier : keySet) {
	    Integer count = getCount(sourceIdentifier);
	    if (count != null) {
		totalCount += count;
	    }
	}
	return totalCount;
    }
}
