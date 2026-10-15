package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;

/**
 * Utility methods to detect and extract DOIs from {@link GSResource} identifiers.
 */
public final class DoiUtils {

    private DoiUtils() {
    }

    /**
     * @param identifier
     * @return <code>true</code> if the supplied identifier looks like a DOI
     */
    public static boolean isDoi(String identifier) {

	if (identifier == null || identifier.isBlank()) {
	    return false;
	}

	String trimmed = identifier.trim();
	String lower = trimmed.toLowerCase();

	return trimmed.startsWith("10.") || lower.startsWith("doi:") || lower.contains("doi.org");
    }

    /**
     * @param identifier
     * @return the normalized DOI (e.g. {@code 10.1594/PANGAEA.66871}) or empty if not a DOI
     */
    public static Optional<String> normalizeDoi(String identifier) {

	if (!isDoi(identifier)) {
	    return Optional.empty();
	}

	String trimmed = identifier.trim();
	String lower = trimmed.toLowerCase();

	if (lower.startsWith("doi:")) {
	    trimmed = trimmed.substring(4).trim();
	} else {
	    int doiOrgIndex = lower.indexOf("doi.org/");
	    if (doiOrgIndex >= 0) {
		trimmed = trimmed.substring(doiOrgIndex + "doi.org/".length()).trim();
	    }
	}

	int queryIndex = trimmed.indexOf('?');
	if (queryIndex >= 0) {
	    trimmed = trimmed.substring(0, queryIndex);
	}

	trimmed = trimmed.replaceAll("/+$", "");

	if (trimmed.startsWith("10.")) {
	    return Optional.of(trimmed);
	}

	return Optional.empty();
    }

    /**
     * @param resource
     * @return all distinct DOIs found in the resource identifiers
     */
    public static List<String> extractDois(GSResource resource) {

	LinkedHashSet<String> dois = new LinkedHashSet<>();

	for (String identifier : collectResourceIdentifiers(resource)) {
	    normalizeDoi(identifier).ifPresent(dois::add);
	}

	return new ArrayList<>(dois);
    }

    /**
     * @param resource
     * @return the first DOI found in the resource identifiers
     */
    public static Optional<String> extractDoi(GSResource resource) {

	return extractDois(resource).stream().findFirst();
    }

    private static List<String> collectResourceIdentifiers(GSResource resource) {

	List<String> identifiers = new ArrayList<>();

	try {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    if (miMetadata == null) {
		return identifiers;
	    }

	    addIfPresent(identifiers, miMetadata.getFileIdentifier());

	    Iterator<DataIdentification> dataIdentifications = miMetadata.getDataIdentifications();
	    while (dataIdentifications.hasNext()) {

		DataIdentification dataIdentification = dataIdentifications.next();
		addIfPresent(identifiers, dataIdentification.getResourceIdentifier());

		for (MDIdentifierPropertyType identifierProperty : dataIdentification.getFirstCitation().getIdentifier()) {

		    try {

			addIfPresent(identifiers, ISOMetadata.getStringFromCharacterString(
				identifierProperty.getMDIdentifier().getValue().getCode()));

		    } catch (NullPointerException | IndexOutOfBoundsException ex) {
			// nothing to do here
		    }
		}
	    }

	    Iterator<String> aggregatedIdentifiers = miMetadata.getAggregatedResourcesIdentifiers();
	    while (aggregatedIdentifiers.hasNext()) {
		addIfPresent(identifiers, aggregatedIdentifiers.next());
	    }

	} catch (RuntimeException ex) {
	    // nothing to do here
	}

	return identifiers;
    }

    private static void addIfPresent(List<String> identifiers, String value) {

	if (value != null && !value.isBlank()) {
	    identifiers.add(value.trim());
	}
    }
}
