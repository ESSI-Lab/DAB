package eu.essi_lab.gssrv.rest;

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


import java.util.Date;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.datacache.SeriesStatus;
import eu.essi_lab.access.datacache.SeriesStatusPage;
import eu.essi_lab.access.datacache.SeriesStatusPage.ConceptCount;
import eu.essi_lab.access.datacache.SeriesStatusRecord;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;

/**
 * JSON encoding of a page of the series statuses of a source (see {@link SeriesStatus}), as stored by the data harvester
 * task
 *
 * @author boldrini
 */
class SourceSeriesReport {

    private SourceSeriesReport() {
    }

    /**
     * @param source
     * @param page
     * @param conceptLabels resolves the label of a concept URI
     * @return
     */
    static JSONObject toJSON(GSSource source, SeriesStatusPage page, Function<String, String> conceptLabels) {

	JSONObject ret = new JSONObject();
	ret.put("status", "success");
	ret.put("name", source.getLabel());
	ret.put("id", source.getUniqueIdentifier());
	putDate(ret, "computed", page.getComputed());

	JSONArray statuses = new JSONArray();
	for (SeriesStatus s : SeriesStatus.values()) {
	    JSONObject jsonStatus = new JSONObject();
	    jsonStatus.put("id", s.name());
	    jsonStatus.put("description", s.getDescription());
	    jsonStatus.put("count", page.getCounts().getOrDefault(s, 0l));
	    statuses.put(jsonStatus);
	}
	ret.put("statuses", statuses);

	JSONArray concepts = new JSONArray();
	for (ConceptCount concept : page.getConcepts()) {
	    JSONObject jsonConcept = new JSONObject();
	    jsonConcept.put("key", concept.getKey());
	    String label = concept.getUri() != null ? conceptLabels.apply(concept.getUri()) : concept.getName();
	    jsonConcept.put("label", label == null || label.isBlank() ? "(not specified)" : label);
	    if (concept.getUri() != null) {
		jsonConcept.put("uri", concept.getUri());
	    }
	    jsonConcept.put("count", concept.getCount());
	    jsonConcept.put("issues", concept.getIssues());
	    concepts.put(jsonConcept);
	}
	ret.put("concepts", concepts);

	ret.put("total", page.getTotal());
	if (page.getNextCursor() != null) {
	    ret.put("next", page.getNextCursor());
	}

	JSONArray series = new JSONArray();
	for (SeriesStatusRecord record : page.getRecords()) {
	    series.put(toJSON(record, page.getNow()));
	}
	ret.put("series", series);

	return ret;
    }

    private static JSONObject toJSON(SeriesStatusRecord record, long now) {

	JSONObject ret = new JSONObject();
	ret.put("id", record.getDataIdentifier());
	ret.put("status", record.getEffectiveStatus(now).name());
	putIfNotNull(ret, "concept", record.getConceptKey());
	ret.put("inMetadata", Boolean.TRUE.equals(record.getInMetadata()));
	ret.put("current", Boolean.TRUE.equals(record.getCurrent()));
	putIfNotNull(ret, "title", record.getTitle());
	putIfNotNull(ret, "platform", record.getPlatform());
	putIfNotNull(ret, "observedProperty", record.getObservedProperty());
	if (record.getValuesCount() != null) {
	    ret.put("values", record.getValuesCount());
	}
	putDate(ret, "firstObservation", record.getFirstObservation());
	putDate(ret, "lastObservation", record.getLastObservation());
	putDate(ret, "nextExpected", record.getNextExpected());
	putDate(ret, "lastInsert", record.getLastInsert());
	putDate(ret, "lastUpdate", record.getLastUpdate());
	if (record.getLastUpdateValues() != null) {
	    ret.put("lastUpdateValues", record.getLastUpdateValues());
	}
	putIfNotNull(ret, "lastUpdateError", record.getLastUpdateError());
	putDate(ret, "computed", record.getComputed());
	return ret;
    }

    private static void putIfNotNull(JSONObject object, String key, String value) {

	if (value != null) {
	    object.put(key, value);
	}
    }

    private static void putDate(JSONObject object, String key, Date date) {

	if (date != null) {
	    object.put(key, ISO8601DateTimeUtils.getISO8601DateTime(date));
	}
    }
}
