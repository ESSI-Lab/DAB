package eu.essi_lab.accessor.ckan;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;

import eu.essi_lab.accessor.ckan.datamodel.CKANDataset;
import eu.essi_lab.accessor.ckan.datamodel.CKANPoint;
import eu.essi_lab.accessor.ckan.datamodel.CKANRelationship;
import eu.essi_lab.accessor.ckan.datamodel.CKANResource;
import eu.essi_lab.accessor.ckan.datamodel.CKANTag;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Parses a string to Java object representing a Dataset in the CKAN data model
 */
public class CKANParser {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private static final String TITLE_KEY = "title";
    private static final String MANTAINER_KEY = "maintainer";
    private static final String AUTHOR_KEY = "author";
    private static final String LICENSE_KEY = "license";
    private static final String ORGANIZATION_KEY = "organization";
    private static final String VALUE_KEY = "value";
    private static final String VERSION_KEY = "version";
    private static final String LICENSE_ID_KEY = "license_id";
    private static final String REVISION_TS_KEY = "revision_timestamp";
    private static final String UNPUBLISHED_KEY = "unpublished";
    private static final String LICENSE_TITLE_KEY = "license_title";
    private static final String NOTES_KEY = "notes";
    private static final String END_DATE_KEY = "end_date";
    private static final String COORDINATES_KEY = "coordinates";
    private static final String THEME_PRIMARY_KEY = "theme-primary";
    private static final String STATE_KEY = "state";

    public CKANDataset parseDataset(String originalMetadata) {

	JSONObject json = new JSONObject(originalMetadata);

	CKANDataset ret = new CKANDataset();

	Object res = json.get("result");

	JSONObject result;
	JSONArray resultArray;

	if (res instanceof JSONArray) {

	    resultArray = (JSONArray) res;
	    result = resultArray.getJSONObject(0);

	} else if (res instanceof JSONObject) {

	    result = (JSONObject) res;

	} else {

	    result = json.getJSONObject("result");
	}

	if (result == null) {
	    result = json;
	}

	String datasetIdentifier = getString(result, "id");

	ret.setId(datasetIdentifier);
	ret.setName(getString(result, "name"));
	ret.setTitle(getString(result, TITLE_KEY));
	ret.setUrl(getString(result, "url"));

	// check some particular usecases which had a specific profile Accessor in the old suite
	// 1)ENVIDAT
	String help = getString(json, "help");
	String datasetUrl = getString(result, "dataset_url");
	if (help != null && help.startsWith("https://www.envidat.ch")) {
	    setEnvidatField(result, ret);
	} else if (datasetUrl != null && datasetUrl.startsWith("https://datacatalog.worldbank.org")) {
	    setDKANWorldBank(result, ret);
	} else {

	    ret.setAuthor(getString(result, AUTHOR_KEY));
	    ret.setAuthorEmail(getString(result, "author_email"));
	    ret.setMaintainer(getString(result, MANTAINER_KEY));
	    ret.setMaintainerEmail(getString(result, "maintainer_email"));
	    ret.setLicense(getString(result, LICENSE_KEY));
	    ret.setLicenseId(getString(result, LICENSE_ID_KEY));
	    ret.setLicenseTitle(getString(result, LICENSE_TITLE_KEY));
	    ret.setVersion(getString(result, VERSION_KEY));

	    // check notes
	    String notes = getString(result, NOTES_KEY);

	    String validNotes = validateNotes(notes);

	    ret.setNotes(validNotes);
	    ret.setPrimaryTheme(getString(result, THEME_PRIMARY_KEY));
	    ret.setUnpublished(getString(result, UNPUBLISHED_KEY));

	    if (result.has(ORGANIZATION_KEY)) {

		JSONObject org = result.getJSONObject(ORGANIZATION_KEY);

		if (org.has(TITLE_KEY)) {

		    String orgName = org.getString(TITLE_KEY);

		    ret.setOrganization(orgName);
		}
	    }
	}

	if (result.has("tags")) {
	    JSONArray tags = getJSONArray(result, "tags");
	    for (int i = 0; i < tags.length(); i++) {
		CKANTag cTag = new CKANTag();
		JSONObject tag = tags.getJSONObject(i);
		cTag.setVocabularyId(getString(tag, "vocabulary_id"));
		cTag.setDisplayName(getString(tag, "display_name"));
		cTag.setName(getString(tag, "name"));
		cTag.setRevisionTimestamp(getString(tag, REVISION_TS_KEY));
		cTag.setState(getString(tag, STATE_KEY));
		cTag.setId(getString(tag, "id"));

		ret.getTags().add(cTag);
	    }
	}
	ret.setState(getString(result, STATE_KEY));

	List<CKANRelationship> objectRelationships = getRelationships(result, "relationships_as_object");

	ret.getRelations().addAll(objectRelationships);

	List<CKANRelationship> subjectRelationships = getRelationships(result, "relationships_as_subject");

	ret.getRelations().addAll(subjectRelationships);

	JSONArray resources = getJSONArray(result, "resources");

	for (int i = 0; i < resources.length(); i++) {
	    CKANResource cResource = new CKANResource();
	    JSONObject resource = resources.getJSONObject(i);

	    if (isBroken(resource)) {
		continue;
	    }

	    cResource.setCachedURL(getString(resource, "cache_url"));

	    cResource.setUrl(getString(resource, "url"));

	    cResource.setName(getString(resource, "name"));
	    cResource.setDescription(getString(resource, "description"));
	    cResource.setType(getString(resource, "type"));
	    cResource.setResourceType(getString(resource, "resource_type"));
	    cResource.setFormat(getString(resource, "format"));
	    cResource.setMimetype(getString(resource, "mimetype"));
	    cResource.setMimetypeInner(getString(resource, "mimetype_inner"));
	    cResource.setSize(getString(resource, "size"));
	    cResource.setLastModified(getString(resource, "last_modified"));
	    cResource.setHash(getString(resource, "hash"));

	    // additional fields
	    cResource.setResourceGroupId(getString(resource, "resource_group_id"));
	    cResource.setRevisionTimeStamp(getString(resource, REVISION_TS_KEY));
	    cResource.setId(getString(resource, "id"));
	    cResource.setState(getString(resource, STATE_KEY));
	    cResource.setUrlType(getString(resource, "url_type"));
	    cResource.setCreated(getString(resource, "created"));

	    ret.getResources().add(cResource);
	}
	JSONArray extras = null;
	try {
	    extras = getJSONArray(result, "extras");
	} catch (Exception e) {
	    logger.warn("Exception getting extras", e);
	}

	String spatialCoverageGml = null;
	String spatialCoverageGmlVal = null;

	if (extras != null)
	    for (int i = 0; i < extras.length(); i++) {
		JSONObject extra = extras.getJSONObject(i);

		String key = getString(extra, "key");
		String value = getString(extra, VALUE_KEY);

		SimpleEntry<String, String> kvp = new SimpleEntry<>(key, value);
		ret.getExtras().add(kvp);

		switch (key) {
		case "access_constraints":
		    JSONArray array = getJSONArray(extra, VALUE_KEY);
		    if (array.length() > 0) {
			ret.setAccessConstraints(getJSONArray(extra, VALUE_KEY).getString(0));
		    } else {
			ret.setAccessConstraints(getString(extra, VALUE_KEY));
		    }
		    break;
		case "bbox-east-long":
		    ret.setBboxEastLongitude(getString(extra, VALUE_KEY));
		    break;
		case "bbox-north-lat":
		    ret.setBboxNorthLatitude(getString(extra, VALUE_KEY));
		    break;
		case "bbox-south-lat":
		    ret.setBboxSouthLatitude(getString(extra, VALUE_KEY));
		    break;
		case "bbox-west-long":
		    ret.setBboxWestLongitude(getString(extra, VALUE_KEY));
		    break;
		case "bbox_extended":
		    ret.setBboxExtended(getString(extra, VALUE_KEY));
		    break;
		case "contact-email":
		    ret.setContactEmail(getString(extra, VALUE_KEY));
		    break;
		case "dataset-reference-date":
		    array = getJSONArray(extra, VALUE_KEY);
		    if (array.length() > 0) {
			for (int j = 0; j < array.length(); j++) {
			    JSONObject referenceDate = array.getJSONObject(j);

			    String dateType = referenceDate.getString("type");
			    String dateValue = referenceDate.getString(VALUE_KEY);
			    switch (dateType) {
			    case "creation":
				ret.setDatasetCreationDate(dateValue);
				break;
			    case "publication":
				ret.setDatasetPublicationDate(dateValue);
				break;
			    case "revision":
				ret.setDatasetRevisionDate(dateValue);
				break;
			    default:
				logger.warn("Unrecognized CKAN date type: {}", dateType);
				break;
			    }
			}
		    }

		    break;

		case "frequency-of-update":
		    ret.setFrequencyOfUpdate(getString(extra, VALUE_KEY));
		    break;
		case "guid":
		    ret.setGuid(getString(extra, VALUE_KEY));
		    break;
		case "harvest_timestamp":
		    ret.setHarvestTimestamp(getString(extra, VALUE_KEY));
		    break;
		case "licence":
		    ret.setLicense(getFirstString(extra, VALUE_KEY));
		    break;
		case "metadata-date":
		    ret.setMetadataDate(getString(extra, VALUE_KEY));
		    break;
		case "metadata-language":
		    ret.setMetadataLanguage(getString(extra, VALUE_KEY));
		    break;
		case "progress":
		    ret.setProgress(getString(extra, VALUE_KEY));
		    break;
		case "responsible-party":
		    JSONArray respArray = getJSONArray(extra, VALUE_KEY);
		    if (respArray.length() > 0) {

			JSONObject responsibleParty = respArray.getJSONObject(0);
			String name = responsibleParty.getString("name");

			// TODO roles

			ret.setResponsibleParty(name);

		    } else {

			String valObj = extra.getString(VALUE_KEY);

			if (valObj != null && !"".equalsIgnoreCase(valObj)) {
			    // TODO roles
			    ret.setResponsibleParty(valObj);

			}

		    }
		    break;
		case "spatial":
		    try {
			String object = extra.get(VALUE_KEY).toString();
			JSONObject spatialObject = new JSONObject(object);
			JSONArray coords = getJSONArray(spatialObject, COORDINATES_KEY);
			coords = coords.getJSONArray(0);
			Double e = null;
			Double s = null;
			Double w = null;
			Double n = null;
			for (int j = 0; j < coords.length(); j++) {
			    JSONArray coord = coords.getJSONArray(j);
			    double lat = coord.getDouble(1);
			    double lon = coord.getDouble(0);
			    if (s == null) {
				s = lat;
			    }
			    if (n == null) {
				n = lat;
			    }
			    if (e == null) {
				e = lon;
			    }
			    if (w == null) {
				w = lon;
			    }
			    if (lon > e) {
				e = lon;
			    }
			    if (lat > n) {
				n = lat;
			    }
			    if (lon < w) {
				w = lon;
			    }
			    if (lat < s) {
				s = lat;
			    }
			    CKANPoint p = new CKANPoint("EPSG:4326", lat, lon);
			    ret.getPolygon().add(p);
			}
			ret.setBboxEastLongitude("" + e);
			ret.setBboxWestLongitude("" + w);
			ret.setBboxSouthLatitude("" + s);
			ret.setBboxNorthLatitude("" + n);
		    } catch (RuntimeException thr) {

			logger.warn("Error getting spatial Extra", thr);

		    }
		    break;
		case "spatial-data-service-type":
		    ret.setSpatialDataServiceType(getString(extra, VALUE_KEY));
		    break;
		case "spatial-reference-system":
		    ret.setSpatialReferenceSystem(getString(extra, VALUE_KEY));
		    break;
		case "spatial_harvester":
		    ret.setSpatialHarvester(getString(extra, VALUE_KEY));
		    break;
		case "harvest_object_id":
		    ret.setHarvestObjectId(getString(extra, VALUE_KEY));
		    break;
		case "harvest_source_id":
		    ret.setHarvestSourceId(getString(extra, VALUE_KEY));
		    break;
		case "harvest_source_title":
		    ret.setHarvestSourceTitle(getString(extra, VALUE_KEY));
		    break;
		// JRC case
		case "temporal-coverage":
		    JSONArray jrcTempArray = getJSONArray(extra, VALUE_KEY);
		    if (jrcTempArray.length() > 0) {

			JSONObject timeDates = jrcTempArray.getJSONObject(0);
			if (timeDates.has("start_date"))
			    ret.setDatasetStartDate(timeDates.getString("start_date"));
			if (timeDates.has(END_DATE_KEY))
			    ret.setDatasetEndDate(timeDates.getString(END_DATE_KEY));
		    }
		    break;
		// UK Data Gov
		case "temporal_coverage-from":
		    JSONArray ukFromTempArray = getJSONArray(extra, VALUE_KEY);
		    if (ukFromTempArray.length() > 0) {
			String ukStartTime = ukFromTempArray.getString(0);
			if (ukStartTime != null && !"".equalsIgnoreCase(ukStartTime))
			    ret.setDatasetStartDate(ukStartTime);
		    }
		    break;
		case "temporal_coverage-to":
		    JSONArray ukToTempArray = getJSONArray(extra, VALUE_KEY);
		    if (ukToTempArray.length() > 0) {
			String ukEndTime = ukToTempArray.getString(0);
			if (ukEndTime != null && !"".equalsIgnoreCase(ukEndTime))
			    ret.setDatasetEndDate(ukEndTime);
		    }
		    break;
		// US DATA GOV
		case "temporal":
		    String toSplit = getString(extra, VALUE_KEY);
		    String[] dateRange = toSplit.split("/");
		    if (dateRange.length > 1) {
			ret.setDatasetStartDate(dateRange[0]);
			ret.setDatasetEndDate(dateRange[1]);
		    }
		    ret.setDatasetStartDate(dateRange[0]);
		    break;
		case "temporal-extent-begin":
		    ret.setDatasetStartDate(getString(extra, VALUE_KEY));
		    break;
		case "temporal-extent-end":
		    ret.setDatasetEndDate(getString(extra, VALUE_KEY));
		    break;
		case "spatial_coverage_gml":
		    spatialCoverageGml = key;
		    spatialCoverageGmlVal = value;
		    break;

		case "spatial_coverage_wkt":

		    String polygon = getString(extra, VALUE_KEY);
		    polygon = polygon.replace("[","");
		    polygon = polygon.replace("]","");
		    polygon = polygon.replace("\"","");

		    try {

			double[] bbox = getBoundingBoxFromWKT(polygon);

			ret.setBboxWestLongitude(String.valueOf(bbox[0]));
			ret.setBboxSouthLatitude(String.valueOf(bbox[1]));

			ret.setBboxEastLongitude(String.valueOf(bbox[2]));
			ret.setBboxNorthLatitude(String.valueOf(bbox[3]));

		    } catch (ParseException e) {
			GSLoggerFactory.getLogger(getClass()).error(e);
		    }

		    break;
		default:
		    break;
		}
	    }

	// //
	/**
	 * special metadata cases
	 * 1)BBOX
	 * 2)TIME
	 */

	// 1) BBOX
	if (ret.getBboxEastLongitude() == null) {

	    setBoundingBox(result, spatialCoverageGml, spatialCoverageGmlVal, ret, json);

	}

	// 2) TIME
	if (ret.getDatasetStartDate() == null && ret.getDatasetEndDate() == null) {
	    setTemporalExtent(result, ret);
	}

	ret.setRevisionTimestamp(getString(result, REVISION_TS_KEY));
	ret.setMetadataCreated(getString(result, "metadata_created"));
	ret.setMetadataModified(getString(result, "metadata_modified"));
	ret.setCreatorUserId(getString(result, "creator_user_id"));

	// WRI fields
	// wri_temporal_coverage

	if (ret.getContactEmail() == null) {
	    ret.setContactEmail(getString(result, "wri_main_contact_email"));
	}
	if (ret.getResponsibleParty() == null) {
	    ret.setResponsibleParty(getString(result, "wri_main_contact"));
	}

	logger.info("Dataset with identifier: {} successfully parsed", datasetIdentifier);
	return ret;

    }

    private String validateNotes(String notes) {
	if (notes == null)
	    return null;

	if (notes.contains("<div id='content'>")) {
	    String[] splittedString = notes.split("<div id='content'>");
	    String[] abstraktSplit = splittedString[1].split("</div>");
	    notes = abstraktSplit[0];
	}

	String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";

	return notes.replaceAll(xml10pattern, "");
    }

    private void setDKANWorldBank(JSONObject result, CKANDataset ret) {

	ret.setAuthor(getString(result, AUTHOR_KEY));
	ret.setAuthorEmail(getString(result, "author_email"));
	ret.setMaintainer(getString(result, MANTAINER_KEY));
	ret.setMaintainerEmail(getString(result, "maintainer_email"));
	ret.setLicense(getString(result, LICENSE_KEY));
	ret.setLicenseId(getString(result, LICENSE_ID_KEY));
	ret.setLicenseTitle(getString(result, LICENSE_TITLE_KEY));
	ret.setVersion(getString(result, VERSION_KEY));
	ret.setNotes(getString(result, NOTES_KEY));
	ret.setPrimaryTheme(getString(result, THEME_PRIMARY_KEY));
	ret.setUnpublished(getString(result, UNPUBLISHED_KEY));

	if (result.has(ORGANIZATION_KEY)) {
	    Object org = result.get(ORGANIZATION_KEY);

	    JSONObject organization = null;
	    JSONArray organizationArray;

	    if (org instanceof JSONArray) {
		organizationArray = (JSONArray) org;
		organization = organizationArray.getJSONObject(0);

	    } else {
		organization = (JSONObject) org;
	    }

	    if (organization != null && organization.has(TITLE_KEY)) {

		String orgName = organization.getString(TITLE_KEY);

		ret.setOrganization(orgName);
	    }
	}

	String description = getString(result, "body");
	ret.setNotes(description);
	// release_date

	String releaseDate = getString(result, "release_date");
	ret.setDatasetPublicationDate(releaseDate);
	// last_update
	String lastUpdate = getString(result, "last_update");
	ret.setRevisionTimestamp(lastUpdate);

	// languages_supported
	String language = getString(result, "languages_supported");
	ret.setMetadataLanguage(language);
	// terms_of_use
	String termsOfUse = getString(result, "terms_of_use");
	ret.setAccessConstraints(termsOfUse);
	// copyright
	String copyright = getString(result, "copyright");
	ret.setLicense(copyright);
	// type
	String type = getString(result, "type");
	ret.setType(type);
	// //
	ret.setVersion(getString(result, VERSION_KEY));
    }

    private void setEnvidatField(JSONObject result, CKANDataset ret) {
	// TODO: envidat case

	if (result.has(AUTHOR_KEY)) {
	    Object auth = result.get(AUTHOR_KEY);
	    JSONObject author = null;
	    JSONArray authorArray;
	    String authorName = null;
	    String authorMail = null;

	    if (auth instanceof JSONArray) {
		authorArray = (JSONArray) auth;
		author = authorArray.getJSONObject(0);
	    } else if (auth instanceof JSONObject) {
		author = (JSONObject) auth;
	    }

	    if (author != null) {

		authorName = getString(author, "name");
		authorMail = getString(author, "email");
	    }

	    if (authorName != null) {
		ret.setAuthor(authorName);
	    }
	    if (authorMail != null) {
		ret.setAuthorEmail(authorMail);
	    }

	}

	if (result.has(MANTAINER_KEY)) {
	    Object main = result.get(MANTAINER_KEY);
	    JSONObject maintainer = null;
	    JSONArray maintainerArray;
	    String maintainerName = null;
	    String maintainerMail = null;

	    if (main instanceof JSONArray) {
		maintainerArray = (JSONArray) main;
		maintainer = maintainerArray.getJSONObject(0);
	    } else if (main instanceof JSONObject) {
		maintainer = (JSONObject) main;
	    }

	    if (maintainer != null) {

		maintainerName = getString(maintainer, "name");
		maintainerMail = getString(maintainer, "email");
	    }

	    if (maintainerName != null) {
		ret.setMaintainer(maintainerName);
	    }
	    if (maintainerMail != null) {
		ret.setMaintainerEmail(maintainerMail);
	    }

	}

	if (result.has("date")) {
	    Object d = result.get("date");
	    JSONObject date = null;
	    JSONArray dateArray;
	    String startDate = null;
	    String endDate = null;
	    if (d instanceof JSONArray) {
		dateArray = (JSONArray) d;
		date = dateArray.getJSONObject(0);
	    } else if (d instanceof JSONObject) {
		date = (JSONObject) d;
	    }

	    if (date != null) {
		startDate = getString(date, "date");
		endDate = getString(date, END_DATE_KEY);
	    }

	    if (startDate != null) {
		ret.setDatasetStartDate(startDate);
	    }
	    if (endDate != null) {
		ret.setDatasetEndDate(endDate);
	    }
	}

	// TODO: envidat case
	ret.setLicense(getString(result, LICENSE_KEY));
	ret.setLicenseId(getString(result, LICENSE_ID_KEY));
	ret.setLicenseTitle(getString(result, LICENSE_TITLE_KEY));

	ret.setVersion(getString(result, VERSION_KEY));
	ret.setNotes(getString(result, NOTES_KEY));
	ret.setPrimaryTheme(getString(result, THEME_PRIMARY_KEY));
	ret.setUnpublished(getString(result, UNPUBLISHED_KEY));

	// TODO: envidat case check
	if (result.has(ORGANIZATION_KEY)) {
	    Object org = result.get(ORGANIZATION_KEY);
	    JSONObject organization = null;
	    JSONArray organizationArray;

	    if (org instanceof JSONArray) {
		organizationArray = (JSONArray) org;
		organization = organizationArray.getJSONObject(0);
	    } else {
		organization = (JSONObject) org;
	    }

	    if (organization != null && organization.has(TITLE_KEY)) {

		String orgName = organization.getString(TITLE_KEY);

		ret.setOrganization(orgName);
	    }
	}

    }

    private String getFirstString(JSONObject result, String key) {
	JSONArray array = getJSONArray(result, key);
	if (array.length() > 0) {
	    return getJSONArray(result, key).getString(0);
	} else {
	    return getString(result, key);
	}
    }

    private JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    boolean hasKey = result.has(key);
	    if (!hasKey) {
		return new JSONArray();
	    }

	    Object object = result.get(key);
	    if (object instanceof JSONArray) {

		return (JSONArray) object;
	    }

	    if (object.toString().startsWith("[") && object.toString().endsWith("]")) {

		JSONArray ret = new JSONArray(object.toString());
		return ret;
	    }

	    return new JSONArray();
	} catch (Exception e) {
	    logger.warn("Error getting json array", e);
	    return new JSONArray();
	}

    }

    private String getString(JSONObject result, String key) {
	try {
	    String ret = result.optString(key, null);
	    if (ret == null || "".equals(ret) || "[]".equals(ret) || "null".equals(ret)) {
		return null;
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    private List<CKANRelationship> getRelationships(JSONObject result, String key) {
	JSONArray objectRelationships = getJSONArray(result, key);
	List<CKANRelationship> ret = new ArrayList<>();
	if (objectRelationships.length() > 0) {
	    for (int i = 0; i < objectRelationships.length(); i++) {
		JSONObject relationObject = objectRelationships.getJSONObject(i);
		String commentString = getString(relationObject, "comment");
		String typeString = getString(relationObject, "type");
		JSONObject extraObject = relationObject.getJSONObject("__extras");
		String objecttId = getString(extraObject, "object_package_id");
		String subjectId = getString(extraObject, "subject_package_id");
		CKANRelationship relation = new CKANRelationship();
		relation.setComment(commentString);
		relation.setType(typeString);
		relation.setObjectId(objecttId);
		relation.setSubjectId(subjectId);
		ret.add(relation);
	    }
	}

	return ret;
    }

    private boolean isBroken(JSONObject resource) {

	try {
	    if (resource.has("archiver")) {

		JSONObject archiver = resource.getJSONObject("archiver");

		if (archiver != null && archiver.has("is_broken")) {

		    Boolean b = archiver.getBoolean("is_broken");
		    if (b != null) {
			return b;
		    }
		}
	    }
	} catch (RuntimeException e) {

	    logger.warn("Error getting isBroken", e);
	}

	return false;
    }

    private void setTemporalExtent(JSONObject result, CKANDataset ret) {

	// CCCA use case
	if (result.has("temporal_start") || result.has("temporal_end")) {
	    ret.setDatasetStartDate(getString(result, "temporal_start"));
	    ret.setDatasetEndDate(getString(result, "temporal_end"));

	    if (logger.isInfoEnabled()) {

		logger.info("Value: {}", String.valueOf(ret.getDatasetStartDate() != null));
		logger.info("Value: {}", String.valueOf(ret.getDatasetEndDate() != null));

	    }
	}

	// WRI use case
	String wriTemporalCoverage = null;
	if (result.has("wri_temporal_coverage")) {
	    wriTemporalCoverage = getString(result, "wri_temporal_coverage");
	    if (wriTemporalCoverage != null) {
		String[] splittedTemporal = null;
		if (wriTemporalCoverage.contains("-")) {
		    splittedTemporal = wriTemporalCoverage.split("-");
		}
		if (wriTemporalCoverage.contains(",")) {
		    splittedTemporal = wriTemporalCoverage.split(",");
		}
		if (splittedTemporal != null && splittedTemporal.length > 1) {
		    ret.setDatasetStartDate(splittedTemporal[0]);
		    ret.setDatasetEndDate(splittedTemporal[1]);
		} else {
		    if (!wriTemporalCoverage.isEmpty()) {
			ret.setDatasetStartDate(wriTemporalCoverage);
		    }
		}
	    }
	}

    }

    /**
     * @param wkt
     * @return
     * @throws ParseException
     */
    private double[] getBoundingBoxFromWKT(String wkt) throws ParseException {

	WKTReader reader = new WKTReader();
	Geometry geometry = reader.read(wkt);

	double maxX = geometry.getEnvelopeInternal().getMaxX();
	double maxY = geometry.getEnvelopeInternal().getMaxY();

	double minX = geometry.getEnvelopeInternal().getMinX();
	double minY = geometry.getEnvelopeInternal().getMinY();

	return new double[] { minX, minY, maxX, maxY };
    }

    public void setBoundingBox(JSONObject result, String spatialCoverageGml, String spatialCoverageGmlVal, CKANDataset ret,
	    JSONObject originalMetadata) {

	Object spatialObj = null;
	// 2) spatial case
	if (result.has("spatial")) {
	    spatialObj = result.get("spatial");
	}
	JSONObject spatial = null;

	if (spatialObj != null && spatialObj instanceof String) {
	    String s = (String) spatialObj;
	    if (!s.isEmpty()) {
		JSONObject jsonSpatial = new JSONObject(s);
		spatial = jsonSpatial;
	    }
	} else if (spatialObj != null) {
	    spatial = (JSONObject) spatialObj;
	}

	if (spatial != null && spatial.has("type") && spatial.has(COORDINATES_KEY)) {

	    spatialCoordinates(spatial, ret);

	} else {
	    // 2) spatial_coverage

	    JSONArray spatialArrayJSON = null;
	    try {
		spatialArrayJSON = getJSONArray(result, "spatial_coverage");
	    } catch (Exception e) {
		logger.warn("Can't read spatial_coverage", e);
	    }

	    spatialCoverageCoordinates(spatialArrayJSON, spatialCoverageGml, spatialCoverageGmlVal, ret);
	}

	// check other very particular cases: EARTH2Observe, WRI, ...
	if (ret.getBboxEastLongitude() == null) {

	    // check if it is EARTH2OBSERVE catalog
	    String help = getString(originalMetadata, "help");
	    String datasetUrl = getString(result, "dataset_url");
	    if (help != null && help.startsWith("https://wci.earth2observe.eu")) {
		ret.setBboxEastLongitude("180");
		ret.setBboxWestLongitude("-180");
		ret.setBboxSouthLatitude("-90");
		ret.setBboxNorthLatitude("90");
		// check if it is World Resources Institute (WRI) catalog
	    } else if (help != null && help.startsWith("http://datasets.wri.org")) {

		String spatialString = null;
		try {
		    spatialString = getString(result, "wri_spatial_extent");
		} catch (Exception e) {

		    logger.warn("Can't read wri_spatial_extent", e);

		}

		if (spatialString != null && !spatialString.isEmpty() && spatialString.contains(",")) {

		    String[] splitted = spatialString.split(",");

		    if (splitted.length > 0) {
			ret.setBboxEastLongitude(splitted[1]);
			ret.setBboxWestLongitude(splitted[0]);
			ret.setBboxSouthLatitude(splitted[2]);
			ret.setBboxNorthLatitude(splitted[3]);
		    }

		}
		// DKAN WORLD BANK
	    } else if (datasetUrl != null && datasetUrl.startsWith("https://datacatalog.worldbank.org")) {
		String location = getString(result, "geographical_coverage");
		if (location != null && !location.equals("")) {

		    if (location.equalsIgnoreCase(("world"))) {
			ret.setBboxEastLongitude("180");
			ret.setBboxWestLongitude("-180");
			ret.setBboxSouthLatitude("-90");
			ret.setBboxNorthLatitude("90");
		    } else {

			String jsonString = execToponomySearch(location);

			if (jsonString != null) {
			    JSONObject box = new JSONObject(jsonString).getJSONObject("bbox");

			    ret.setBboxEastLongitude(box.getString("east"));
			    ret.setBboxWestLongitude(box.getString("west"));
			    ret.setBboxSouthLatitude(box.getString("south"));
			    ret.setBboxNorthLatitude(box.getString("north"));
			}
		    }
		}
	    }

	}

    }

    private void spatialCoverageCoordinates(JSONArray spatialArrayJSON, String spatialCoverageGml, String spatialCoverageGmlVal,
	    CKANDataset ret) {

	if (spatialArrayJSON != null && spatialArrayJSON.length() > 0) {

	    JSONObject box = spatialArrayJSON.getJSONObject(0);

	    if (box.has("east") && box.has("west") && box.has("south") && box.has("north")) {

		ret.setBboxEastLongitude(box.getString("east"));
		ret.setBboxWestLongitude(box.getString("west"));
		ret.setBboxSouthLatitude(box.getString("south"));
		ret.setBboxNorthLatitude(box.getString("north"));
	    }

	} else {

	    if (spatialCoverageGml != null) {

		String[] westSouth = getLowerCorner(spatialCoverageGmlVal);

		String[] eastNorth = getUpperCorner(spatialCoverageGmlVal);

		if (westSouth != null && eastNorth != null && westSouth.length > 1 && eastNorth.length > 1) {
		    ret.setBboxEastLongitude(eastNorth[0]);
		    ret.setBboxWestLongitude(westSouth[0]);
		    ret.setBboxSouthLatitude(westSouth[1]);
		    ret.setBboxNorthLatitude(eastNorth[1]);

		}

	    }
	}

    }

    private void spatialCoordinates(JSONObject spatial, CKANDataset ret) {
	String type = spatial.getString("type").toLowerCase();
	String coord = spatial.get(COORDINATES_KEY).toString();
	String[] coordinates;
	Double[] bbox;
	String east;
	String south;

	switch (type) {
	case "point":
	    coordinates = coord.split(",");
	    south = coordinates[1].substring(0, coordinates[1].length() - 1);
	    east = coordinates[0].substring(1, coordinates[0].length());
	    ret.setBboxEastLongitude(east);
	    ret.setBboxWestLongitude(east);
	    ret.setBboxNorthLatitude(south);
	    ret.setBboxSouthLatitude(south);

	    break;
	case "polygon":
	case "multipoint":
	case "multipolygon":
	    coordinates = coord.split(",");
	    bbox = multipointToBbox(coordinates);
	    if (bbox.length == 4) {
		ret.setBboxEastLongitude(String.valueOf(bbox[2]));
		ret.setBboxWestLongitude(String.valueOf(bbox[0]));
		ret.setBboxNorthLatitude(String.valueOf(bbox[3]));
		ret.setBboxSouthLatitude(String.valueOf(bbox[1]));
	    }
	    break;

	default:
	    break;
	}

    }

    private Double[] multipointToBbox(String[] coordinates) {
	Double[] bbox = new Double[4];
	Double maxlat = null;
	Double minlat = null;
	Double maxlon = null;
	Double minlon = null;
	try {
	    for (int i = 0; i < coordinates.length; i++) {
		if (i % 2 == 0) {
		    String value = coordinates[i].replaceAll("\\[", "").replaceAll("\\]", "");
		    Double val = Double.valueOf(value);
		    if (maxlon == null) {
			maxlon = val;
			minlon = val;
		    }
		    if (val > maxlon) {
			maxlon = val;
		    }
		    if (val < minlon) {
			minlon = val;
		    }

		} else {
		    String value = coordinates[i].replaceAll("\\[", "").replaceAll("\\]", "");
		    Double val = Double.valueOf(value);
		    if (maxlat == null) {
			maxlat = val;
			minlat = val;
		    }
		    if (val > maxlat) {
			maxlat = val;
		    }
		    if (val < minlat) {
			minlat = val;
		    }
		}
	    }
	    if (maxlat != null && maxlon != null) {
		bbox[0] = minlon;
		bbox[1] = minlat;
		bbox[2] = maxlon;
		bbox[3] = maxlat;
		return bbox;
	    }

	    return new Double[0];
	} catch (Exception e) {
	    logger.warn("Exception converting multipoint to bounding box", e);
	    return new Double[0];
	}
    }

    private String[] getUpperCorner(String gml) {
	String[] res1 = gml.split("<gml:upperCorner>");
	if (res1.length < 2)
	    return new String[0];

	String[] upper = res1[1].split("<");

	return upper[0].split(" ");

    }

    private String[] getLowerCorner(String gml) {
	String[] res1 = gml.split("<gml:lowerCorner>");
	if (res1.length < 2)
	    return new String[0];

	String[] lower = res1[1].split("<");
	return lower[0].split(" ");

    }

    public static String execToponomySearch(String location) {

	ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
	searchCriteria.setQ(location);

	WebService.setUserName("ilsanto23");

	try {

	    ToponymSearchResult searchResult = WebService.search(searchCriteria);

	    if (searchResult.getTotalResultsCount() > 0) {
		Toponym toponym = searchResult.getToponyms().get(0);

		int geoID = toponym.getGeoNameId();

		String getBBOXURL = "http://api.geonames.org/getJSON?geonameId=" + geoID + "&username=ilsanto23&type=json";

		Downloader downloader = new Downloader();
		return downloader.downloadOptionalString(getBBOXURL).orElse(null);

	    }
	} catch (Exception e) {
	    // nothing to do...
	}

	return null;
    }
}
