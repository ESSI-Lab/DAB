package eu.essi_lab.profiler.om;

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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;

/**
 * Writes bulk metadata export as a single CSV document.
 * <p>
 * Reorder CSV columns by editing {@link #COLUMNS} only.
 */
public final class MetadataCsvWriter extends AbstractMetadataWriter {

    private static final CsvColumn[] COLUMNS = { //
	    column("observation_id", AbstractMetadataWriter::extractObservationId), //
	    column("feature_id", AbstractMetadataWriter::extractFeatureId), //
	    column("feature_name", AbstractMetadataWriter::extractFeatureName), //
	    column("observed_property", AbstractMetadataWriter::extractObservedPropertyTitle), //
	    column("observed_property_uri", AbstractMetadataWriter::extractObservedPropertyUri), //
	    column("latitude", AbstractMetadataWriter::extractLatitude), //
	    column("longitude", AbstractMetadataWriter::extractLongitude), //
	    column("altitude", AbstractMetadataWriter::extractAltitude), //
	    column("aggregation_duration", AbstractMetadataWriter::extractAggregationDuration), //
	    column("interpolation_type", AbstractMetadataWriter::extractInterpolationType), //
	    column("intended_observation_spacing", AbstractMetadataWriter::extractIntendedObservationSpacing), //
	    column("country", AbstractMetadataWriter::extractCountry), //
	    column("phenomenon_time_begin", AbstractMetadataWriter::extractPhenomenonBegin), //
	    column("phenomenon_time_end", AbstractMetadataWriter::extractPhenomenonEnd), //
	    column("provider_id", AbstractMetadataWriter::extractProviderId), //
	    column("provider_label", AbstractMetadataWriter::extractProviderLabel), //
	    column("uom", AbstractMetadataWriter::extractUom) //
    };

    public static final String HEADER = Stream.of(COLUMNS).map(c -> c.name).collect(Collectors.joining(","));

    private MetadataCsvWriter() {
    }

    public static void writeHeader(Writer writer) throws IOException {
	writer.write(HEADER);
	writer.write("\n");
    }

    public static void writeObservationRow(Writer writer, JSONObject observation) throws IOException {
	writer.write(formatRow(observation));
	writer.write("\n");
    }

    public static BufferedWriter asBuffered(Writer writer) {
	if (writer instanceof BufferedWriter) {
	    return (BufferedWriter) writer;
	}
	return new BufferedWriter(writer);
    }

    private static String formatRow(JSONObject observation) {
	String[] values = new String[COLUMNS.length];
	for (int i = 0; i < COLUMNS.length; i++) {
	    values[i] = formatField(COLUMNS[i].extract(observation));
	}
	return String.join(",", values);
    }

    private static String formatField(String value) {
	if (value == null || value.isEmpty()) {
	    return "";
	}
	if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
	    return "\"" + value.replace("\"", "\"\"") + "\"";
	}
	return value;
    }

    private static CsvColumn column(String name, Function<JSONObject, String> extractor) {
	return new CsvColumn(name, extractor);
    }

    private static final class CsvColumn {
	private final String name;
	private final Function<JSONObject, String> extractor;

	private CsvColumn(String name, Function<JSONObject, String> extractor) {
	    this.name = name;
	    this.extractor = extractor;
	}

	private String extract(JSONObject observation) {
	    return extractor.apply(observation);
	}
    }
}
