package eu.essi_lab.api.database.marklogic.search;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.ExtendedElementsPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class MarkLogicSelectorBuilder {

    private static final String ORIGINAL_XPATH = "$x//gs:originalMetadata/*";
    private static final String SOURCE_XPATH = "$x//gs:source,";
    private static final String CORE_XPATH = "$x//gs:coreMetadata";
    private static final String AUGMENTED_XPATH = "$x//gs:augmentedMetadata";
    private static final String EXTENDED_XPATH = "$x//gs:extendedMetadata";
    private static final String ORIGINAL = "ORIGINAL";
    private static final String SOURCE = "SOURCE";
    private static final String CORE = "CORE";
    private static final String AUGMENTED = "AUGMENTED";
    private static final String EXTENDED = "EXTENDED";
    private static final String SELECTED_INDEXES = "SELECTED_INDEXES";
    private static final String SELECTED_EXTENDED_ELEMENTS = "SELECTED_EXTENDED_ELEMENTS";

    private ResourceSelector selector;
    private String ctsSearch;
    private String template;
    private boolean isDistinct;

    public MarkLogicSelectorBuilder(DiscoveryMessage message, String ctsSearch) {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("selectorTemplate");

	try {
	    template = IOStreamUtils.asUTF8String(stream);
	    stream.close();
	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't load Marklogic selector template", e);
	    throw new RuntimeException(e.getMessage());
	}

	this.selector = message.getResourceSelector();
	this.ctsSearch = ctsSearch;
	this.isDistinct = message.getDistinctValuesElement().isPresent();
    }

    public String applySelection() {

	ResourceSubset subset = selector.getSubset();
	switch (subset) {

	case NONE:
	    clear(SOURCE);
	    clear(CORE);
	    clear(AUGMENTED);
	    clearExtendedElements();
	    break;	
	case SOURCE:
	    apply(SOURCE);
	    clear(CORE);
	    clear(AUGMENTED);
	    clearExtendedElements();
	    break;
	case NO_HARMONIZED:
	    apply(SOURCE);
	    clear(CORE);
	    clear(AUGMENTED);
	    clearExtendedElements();
	    break;

	case CORE:
	    clear(SOURCE);
	    apply(CORE, true);
	    clear(AUGMENTED);
	    clearExtendedElements();
	    break;

	case SOURCE_CORE:
	    apply(SOURCE);
	    apply(CORE, true);
	    clear(AUGMENTED);
	    clearExtendedElements();
	    break;

	case AUGMENTED:
	    clear(SOURCE);
	    clear(CORE);
	    apply(AUGMENTED, true);
	    clearExtendedElements();
	    break;

	case EXTENDED:
	    clear(SOURCE);
	    clear(CORE);
	    clear(AUGMENTED);
	    applyExtendedElements();
	    break;

	case HARMONIZED:
	    clear(SOURCE);
	    apply(CORE, true);
	    apply(AUGMENTED, true);
	    applyExtendedElements();
	    break;
	case NO_SOURCE:
	    clear(SOURCE);
	    apply(CORE, true);
	    apply(AUGMENTED, true);
	    applyExtendedElements();
	    break;

	case NO_AUGMENTED:
	    apply(SOURCE);
	    apply(CORE, true);
	    clear(AUGMENTED);
	    applyExtendedElements();
	    break;

	case NO_CORE:
	    apply(SOURCE);
	    clear(CORE);
	    apply(AUGMENTED, true);
	    applyExtendedElements();
	    break;

	case NO_EXTENDED:
	    apply(SOURCE);
	    apply(CORE, true);
	    apply(AUGMENTED, true);
	    clearExtendedElements();
	    break;

	case CORE_AUGMENTED:
	    clear(SOURCE);
	    apply(CORE, true);
	    apply(AUGMENTED, true);
	    clearExtendedElements();
	    break;

	case CORE_EXTENDED:
	    clear(SOURCE);
	    apply(CORE, true);
	    clear(AUGMENTED);
	    applyExtendedElements();
	    break;

	case AUGMENTED_EXTENDED:
	    clear(SOURCE);
	    clear(CORE);
	    apply(AUGMENTED, true);
	    applyExtendedElements();
	    break;

	case FULL:
	default:
	    apply(SOURCE);
	    apply(CORE, true);
	    apply(AUGMENTED, true);
	    applyExtendedElements();
	    break;
	}

	handleIndexes();

	handleExtendedElements();

	if (selector.isOriginalIncluded()) {

	    template = template.replace(ORIGINAL, ORIGINAL_XPATH);

	} else {

	    template = template.replace(ORIGINAL, "");
	}

	if (!isDistinct) {

	    ctsSearch = ctsSearch.replace(//
		    "subsequence", //
		    "for $x in subsequence");
	}

	ctsSearch = ctsSearch.replace(//
		",xdmp:query-trace(false());", //
		template);

	return ctsSearch;
    }

    private void clearExtendedElements() {
	if (selector.getExtendedElementsPolicy() == null && selector.getExtendedElements().isEmpty()) {
	    selector.setExtendedElementsPolicy(ExtendedElementsPolicy.NONE);
	}
    }

    private void applyExtendedElements() {
	if (selector.getExtendedElementsPolicy() == null && selector.getExtendedElements().isEmpty()) {
	    selector.setExtendedElementsPolicy(ExtendedElementsPolicy.ALL);
	}
    }

    private void clear(String element) {

	template = template.replace(element, "");
    }

    private void apply(String element) {

	apply(element, false);
    }

    private void apply(String element, boolean addComma) {

	String comma = addComma ? ", \n" : " \n";

	switch (element) {
	case CORE:
	    template = template.replace(CORE, CORE_XPATH + comma);
	    break;
	case AUGMENTED:
	    template = template.replace(AUGMENTED, AUGMENTED_XPATH + comma);
	    break;
	case EXTENDED:
	    template = template.replace(EXTENDED, EXTENDED_XPATH);
	    break;
	case SOURCE:
	    template = template.replace(SOURCE, SOURCE_XPATH);
	    break;
	}
    }

    private void handleIndexes() {

	String selection = null;

	if (!selector.getIndexes().isEmpty()) {

	    selection = selector.getIndexes().//
		    stream().//
		    collect(Collectors.joining(",", "'", "'"));

	} else {

	    switch (selector.getIndexesPolicy()) {
	    case ALL:
		selection = "'ALL'";
		break;
	    case NONE:

		selection = "'NONE'";
		break;

	    case PROPERTIES:

		selection = Arrays.asList(ResourceProperty.values()).//
			stream().//
			map(p -> p.getName()).//
			collect(Collectors.joining(",", "'", "'"));
	    }
	}

	template = template.replace(SELECTED_INDEXES, selection);
    }

    private void handleExtendedElements() {

	String selection = null;

	if (!selector.getExtendedElements().isEmpty()) {

	    selection = selector.getExtendedElements().//
		    stream().//
		    collect(Collectors.joining(",", "'", "'"));

	} else {

	    switch (selector.getExtendedElementsPolicy()) {
	    case ALL:
		selection = "'ALL'";
		break;
	    case NONE:

		selection = "'NONE'";
		break;
	    }

	}

	template = template.replace(SELECTED_EXTENDED_ELEMENTS, selection);
    }
}
