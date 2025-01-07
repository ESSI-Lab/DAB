package eu.essi_lab.profiler.os.handler.discover.eiffel;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.eiffel.api.EiffelAPI;
import eu.essi_lab.eiffel.api.EiffelAPI.SearchIdentifiersApi;
import eu.essi_lab.messages.DiscoveryMessage.EiffelAPIDiscoveryOption;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.UserBondMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSRequestParser;

/**
 * @author Fabrizio
 */
public class EiffelDiscoveryHelper {

    /**
     * 
     */
    static final String EIFFEL_ID_PREFIX = "eiffel_record_";

    /**
     * 
     */
    private static final int DEFAULT_SORT_AND_FILTER_IDS_PARTITION_SIZE = 10000;
    /**
     * 
     */
    private static final boolean DEFAULT_USE_EIFFEL_FILTER_API_CACHE = true;
    /**
     * 
     */
    private static final boolean DEFAULT_USE_MERGED_IDS_CACHE = true;
    /**
     * 
     */
    private static final int DEFAULT_FILTER_AND_SORT_SPLIT_TRESHOLD = 3;

    /**
     * @param request
     * @return
     */
    public static Optional<EiffelAPIDiscoveryOption> readEiffelOption(WebRequest request) {

	if (request.getFormData().isPresent()) {

	    KeyValueParser keyValueParser = new KeyValueParser(request.getFormData().get());
	    OSRequestParser parser = new OSRequestParser(keyValueParser);

	    String eiffelDiscoveryOption = parser.parse(OSParameters.EIFFEL_DISCOVERY);
	    if (eiffelDiscoveryOption != null && !eiffelDiscoveryOption.equals(KeyValueParser.UNDEFINED)) {

		return EiffelAPIDiscoveryOption.fromValue(eiffelDiscoveryOption);
	    }
	}

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();
	    String option = properties.getProperty("forceEiffelAPIDiscoveryOption");

	    if (option != null) {

		return EiffelAPIDiscoveryOption.fromValue(option);
	    }
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    private static int readIntegerOption(String option, int def) {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	int value = def;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty(option);

	    if (propValue != null) {

		value = Integer.valueOf(propValue);
	    }
	}

	return value;
    }

    /**
     * @return
     */
    static int getMaxSortIdentifiers() {

	return readIntegerOption("eiffelAPIMaxSortIdentifiers", EiffelAPI.DEFAULT_MAX_SORT_IDENTIFIERS);
    }

    /**
     * 0 -> dsabled
     * 1 -> always enabled
     * > 1 -> treshold
     * 
     * @return
     */
    public static Optional<Integer> getFilterAndSortSplitTreshold() {

	int value = readIntegerOption("eiffelFilterAndSortSplitTreshold", DEFAULT_FILTER_AND_SORT_SPLIT_TRESHOLD);

	return value >= 1 ? Optional.of(value) : Optional.empty();
    }

    /**
     * @return
     */
    static int getSortAndFilterIdsPartitionSize() {

	return readIntegerOption("eiffelSortAndFilterPartitionSize", DEFAULT_SORT_AND_FILTER_IDS_PARTITION_SIZE);
    }

    /**
     * @return
     */
    private static boolean readBooleanOption(String option, boolean def) {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	boolean value = def;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty(option);

	    if (propValue != null) {

		value = Boolean.valueOf(propValue);
	    }
	}

	return value;
    }

    /**
     * @return
     */
    static boolean useEiffelFilterApiCache() {

	return readBooleanOption("eiffelUseFilterAPICache", DEFAULT_USE_EIFFEL_FILTER_API_CACHE);
    }

    /**
     * @return
     */
    static boolean useEiffelMergedIdsCache() {

	return readBooleanOption("eiffelUseMergedIdsCache", DEFAULT_USE_MERGED_IDS_CACHE);
    }

    /**
     * @return
     */
    static SearchIdentifiersApi getSortAndFilterApiOption() {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	SearchIdentifiersApi out = SearchIdentifiersApi.FILTER;

	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();

	    String propValue = properties.getProperty("eiffelSortAndFilterAPI");

	    if (propValue != null) {

		out = SearchIdentifiersApi.valueOf(propValue);
	    }
	}

	return out;
    }

    /**
     * @param identifiers
     * @return
     */
    static Bond getIdsBond(List<String> identifiers) {

	if (identifiers.size() == 1) {

	    return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, identifiers.get(0));
	}

	List<Bond> idsOperands = identifiers.//
		stream().//
		map(i -> BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, i)).//
		collect(Collectors.toList());

	return BondFactory.createOrBond(idsOperands);
    }

    /**
     * @author Fabrizio
     */
    static class CountSetWrapper extends CountSet {

	private int count;
	private CountSet countSet;
	private int pageCount;
	private int pageIndex;

	/**
	 * @param countSet
	 * @param count
	 */
	public CountSetWrapper(CountSet countSet, int count, int pageCount, int pageIndex) {

	    this.countSet = countSet;
	    this.count = count;
	    this.pageCount = pageCount;
	    this.pageIndex = pageIndex;
	}

	@Override
	public void setPageCount(int pageCount) {
	}

	@Override
	public int getPageCount() {

	    return pageCount;
	}

	@Override
	public void setPageIndex(int pageIndex) {
	}

	@Override
	public int getPageIndex() {

	    return pageIndex;
	}

	@Override
	public Optional<TermFrequencyMap> mergeTermFrequencyMaps(int maxItemsCount) {

	    return countSet.mergeTermFrequencyMaps(maxItemsCount);
	}

	@Override
	public void addCountPair(SimpleEntry<String, DiscoveryCountResponse> pair) {

	    countSet.addCountPair(pair);
	}

	@Override
	public Set<String> getSourceIdentifiers() {

	    return countSet.getSourceIdentifiers();
	}

	@Override
	public Integer getCount(String sourceIdentifier) {

	    return countSet.getCount(sourceIdentifier);
	}

	@Override
	public int getCount() {

	    return count;
	}
    }

    /**
     * @param message
     * @param idsBond
     */
    static void mergeBonds(RequestMessage message, Bond idsBond) {

	Optional<Bond> userBond = ((UserBondMessage) message).getUserBond();

	Bond bond = userBond.get().clone();

	if (bond instanceof LogicalBond) {

	    LogicalBond userLogical = ((LogicalBond) bond);

	    if (userLogical.getLogicalOperator() == LogicalOperator.AND) {

		userLogical.getOperands().add(idsBond);

		((UserBondMessage) message).setUserBond(userLogical);

	    } else {

		((UserBondMessage) message).setUserBond(BondFactory.createAndBond(bond, idsBond));
	    }

	} else {

	    ((UserBondMessage) message).setUserBond(BondFactory.createAndBond(bond, idsBond));
	}
    }

    /**
     * @param ids
     * @return
     */
    static List<String> addEiffelPrefixId(List<String> ids) {

	return ids.//
		stream().//
		map(id -> EiffelDiscoveryHelper.EIFFEL_ID_PREFIX + id).//
		collect(Collectors.toList());
    }

    /**
     * @param ids
     * @return
     */
    static List<String> removeEiffelIdPrefix(List<String> ids) {

	return ids.//
		stream().//
		map(id -> id.replace(EiffelDiscoveryHelper.EIFFEL_ID_PREFIX, "")).//
		collect(Collectors.toList());
    }
}
