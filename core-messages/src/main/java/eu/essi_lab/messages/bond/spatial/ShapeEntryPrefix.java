package eu.essi_lab.messages.bond.spatial;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Resolves and sanitizes the OpenSearch shape-files entry prefix used when storing a zipped shapefile.
 */
public final class ShapeEntryPrefix {

    private static final Pattern ALLOWED = Pattern.compile("[a-zA-Z0-9_-]+");
    private static final int MAX_LENGTH = 128;

    private ShapeEntryPrefix() {
    }

    /**
     * @param explicitId user-provided shape identifier, may be empty
     * @param zipFileName uploaded archive file name
     * @return sanitized prefix
     */
    public static String resolve(String explicitId, String zipFileName) {

	if (explicitId != null && !explicitId.isBlank()) {

	    return sanitize(explicitId);
	}

	return fromZipFileName(zipFileName);
    }

    /**
     * @param zipFileName archive file name (may include path segments)
     * @return sanitized prefix derived from the base name without {@code .zip}
     */
    public static String fromZipFileName(String zipFileName) {

	if (zipFileName == null || zipFileName.isBlank()) {
	    throw new IllegalArgumentException("Missing shapefile name");
	}

	String base = zipFileName;
	int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
	if (slash >= 0) {
	    base = base.substring(slash + 1);
	}

	if (base.toLowerCase(Locale.ROOT).endsWith(".zip")) {
	    base = base.substring(0, base.length() - 4);
	}

	return sanitize(base);
    }

    /**
     * @param raw candidate prefix
     * @return sanitized prefix safe for OpenSearch entry names
     */
    public static String sanitize(String raw) {

	if (raw == null || raw.isBlank()) {
	    throw new IllegalArgumentException("Shape identifier is empty");
	}

	String normalized = raw.trim().toLowerCase(Locale.ROOT);
	normalized = normalized.replaceAll("[\\s.]+", "_");
	normalized = normalized.replaceAll("[^a-z0-9_-]", "");
	normalized = normalized.replaceAll("_+", "_");
	normalized = normalized.replaceAll("^-+", "");
	normalized = normalized.replaceAll("-+$", "");

	if (normalized.isEmpty()) {
	    throw new IllegalArgumentException("Shape identifier is not valid after sanitization");
	}

	if (normalized.length() > MAX_LENGTH) {
	    normalized = normalized.substring(0, MAX_LENGTH);
	}

	if (!ALLOWED.matcher(normalized).matches()) {
	    throw new IllegalArgumentException("Shape identifier contains invalid characters");
	}

	return normalized;
    }
}
