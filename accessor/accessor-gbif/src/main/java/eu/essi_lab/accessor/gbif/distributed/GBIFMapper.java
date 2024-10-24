package eu.essi_lab.accessor.gbif.distributed;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author ilsanto
 */
public class GBIFMapper extends OriginalIdentifierMapper {

    public static final String GBIFOCCURRENCE_SCHEMA = "http://essi-lab.eu/gbif/occurrence";
    private static final String IDENTIFIER_KEY = "datasetKey";
    private static final String LONGITUDE_KEY = "decimalLongitude";
    private static final String LATITUDE_KEY = "decimalLatitude";
    private static final String EVENT_DATE_KEY = "eventDate";
    private static final String REVISION_KEY = "modified";
    private static final String SCIENTIFIC_NAME_KEY = "scientificName";
    private static final String ORGANIZATION_NAME_KEY = "ownerInstitutionCode";
    private static final String CLASS_KEY = "class";
    private static final String PHYLUM_KEY = "phylum";
    private static final String ORDER_KEY = "order";
    private static final String FAMILY_KEY = "family";
    private static final String KINGDOM_KEY = "kingdom";
    private static final String GENUS_KEY = "genus";
    private static final String SPECIES_NAME_KEY = "species";
    private static final String GENERIC_NAME_KEY = "genericName";
    private static final String SPECIES_EPITHET_KEY = "specificEpithet";

    private boolean checkNotNull(JSONObject object, String key) {
	return (object.has(key) && !object.isNull(key));
    }

    private Optional<String> readString(JSONObject object, String key) {

	if (checkNotNull(object, key))
	    return Optional.of(object.getString(key));

	return Optional.empty();

    }

    private Optional<Object> readObject(JSONObject object, String key) {

	if (checkNotNull(object, key))
	    return Optional.of(object.get(key));

	return Optional.empty();

    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject json = new JSONObject(resource.getOriginalMetadata().getMetadata());

	Optional<Object> id = readObject(json, IDENTIFIER_KEY);

	return id.isPresent() ? id.get().toString() : null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	JSONObject json = new JSONObject(originalMD.getMetadata());

	Dataset dataset = new Dataset();

	String id = readObject(json, IDENTIFIER_KEY).orElse(UUID.randomUUID().toString()).toString();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	//
	// thumbnail
	//
	if (json.has("media")) {
	    try {
		for (int i = 0; i < json.getJSONArray("media").length(); i++) {

		    JSONObject media = json.getJSONArray("media").getJSONObject(i);

		    String file = media.getString("identifier");
		    String format = media.getString("format");

		    BrowseGraphic graphic = new BrowseGraphic();
		    graphic.setFileName(file);
		    graphic.setFileType(format);

		    dataIdentification.addGraphicOverview(graphic);
		}
	    } catch (Exception ex) {
	    }
	}

	readObject(json, LONGITUDE_KEY).ifPresent(lon ->

	readObject(json, LATITUDE_KEY).ifPresent(lat ->

	dataIdentification.addGeographicBoundingBox("Event point", Double.valueOf(lat.toString()), Double.valueOf(lon.toString()),
		Double.valueOf(lat.toString()), Double.valueOf(lon.toString()))

	)

	);

	readString(json, EVENT_DATE_KEY).ifPresent(dateString ->

	ISO8601DateTimeUtils.parseISO8601ToDate(dateString.replace(" ", "T")).ifPresent(parsed -> {

	    TemporalExtent tempExtent = new TemporalExtent();

	    tempExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(parsed));

	    tempExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(parsed));

	    dataIdentification.addTemporalExtent(tempExtent);

	})

	);

	String title = "GBIF Occurrence " + id;

	dataIdentification.setCitationTitle(title);

	readString(json, SCIENTIFIC_NAME_KEY).ifPresent(

		dataIdentification::setCitationTitle

	);

	readString(json, REVISION_KEY).ifPresent(up ->

	ISO8601DateTimeUtils.parseISO8601ToDate(up.replace(" ", "T"))
		.ifPresent(t -> dataIdentification.addCitationDate(ISO8601DateTimeUtils.getISO8601DateTime(t), REVISION))

	);

	ResponsibleParty responsibleParty = new ResponsibleParty();
	responsibleParty.setRoleCode("originator");

	responsibleParty.setOrganisationName("Global Biodiversity Informatrion Facility");

	readString(json, ORGANIZATION_NAME_KEY).ifPresent(

		responsibleParty::setOrganisationName

	);

	dataIdentification.addPointOfContact(responsibleParty);

	Set<String> keywords = extractKeywords(json);
	keywords.forEach(k -> miMetadata.getDataIdentification().addKeyword(k));

	return dataset;
    }

    private Set<String> extractKeywords(JSONObject json) {

	Set<String> ret = new HashSet<>();

	// -----------------------------------------
	// Taxonomy

	readString(json, GENERIC_NAME_KEY).ifPresent(ret::add);

	readString(json, SPECIES_EPITHET_KEY).ifPresent(ret::add);

	readString(json, SPECIES_NAME_KEY).ifPresent(ret::add);

	readString(json, GENUS_KEY).ifPresent(ret::add);
	readString(json, KINGDOM_KEY).ifPresent(ret::add);

	readString(json, FAMILY_KEY).ifPresent(ret::add);

	readString(json, ORDER_KEY).ifPresent(ret::add);

	readString(json, CLASS_KEY).ifPresent(ret::add);

	readString(json, PHYLUM_KEY).ifPresent(ret::add);

	readString(json, SCIENTIFIC_NAME_KEY).ifPresent(ret::add);

	return ret;

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return GBIFOCCURRENCE_SCHEMA;
    }
}
