package eu.essi_lab.accessor.obis.distributed;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;

import eu.essi_lab.accessor.obis.harvested.OBISResourceMapper;
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
public class OBISGranulesResultMapper extends OriginalIdentifierMapper {

    public static final String OBIS_GRANULES_SCHEME_URI = "http://api.iobis.org/granules/scheme";
    private static final String IDENTIFIER_KEY = "id";
    private static final String LONGITUDE_KEY = "decimalLongitude";
    private static final String LATITUDE_KEY = "decimalLatitude";
    private static final String EVENT_DATE_KEY = "eventDate";
    private static final String SCIENTIFIC_NAME_KEY = "scientificName";
    private static final String DATASETNAME_KEY = "datasetName";
    private static final String GENUS_KEY = "genus";
    private static final String FAMILY_KEY = "family";
    private static final String ORDER_KEY = "order";
    private static final String CLASS_KEY = "class";
    private static final String PHYLUM_KEY = "phylum";
    private static final String COLLECTION_CODE_KEY = "collectionCode";
    private static final String ORIGINAL_SCIENTIFIC_NAME_KEY = "originalScientificName";
    private static final String SCINAME_AUTHORSHIP_KEY = "scientificNameAuthorship";
    private static final String SCIENTIFIC_NAME_ID_KEY = "scientificNameID";
    private static final String REVISION_KEY = "modified";
    private static final String PROVIDER_NAME_KEY = "institutionCode";
    private static final String SPECIES_NAME_KEY = "species";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject json = new JSONObject(resource.getOriginalMetadata().getMetadata());

	Optional<Object> object = OBISResourceMapper.readObject(json, IDENTIFIER_KEY);
	if (object.isPresent()) {

	    return object.get().toString();
	}

	return null;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject json = new JSONObject(originalMD.getMetadata());

	Dataset dataset = new Dataset();

	String id = OBISResourceMapper.readObject(json, IDENTIFIER_KEY).orElse(UUID.randomUUID().toString()).toString();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	OBISResourceMapper.readObject(json, LONGITUDE_KEY).ifPresent(lon ->

	OBISResourceMapper.readObject(json, LATITUDE_KEY).ifPresent(lat ->

	dataIdentification.addGeographicBoundingBox(//
		"Event point", //
		Double.valueOf(lat.toString()), //
		Double.valueOf(lon.toString()), //
		Double.valueOf(lat.toString()), //
		Double.valueOf(lon.toString()))));

	OBISResourceMapper.readString(json, EVENT_DATE_KEY).ifPresent(dateString ->

	ISO8601DateTimeUtils.parseISO8601ToDate(dateString.replace(" ", "T")).ifPresent(parsed -> {

	    TemporalExtent tempExtent = new TemporalExtent();

	    tempExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(parsed));

	    tempExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(parsed));

	    dataIdentification.addTemporalExtent(tempExtent);
	}));

	String title = "OBIS Occurrence " + id;

	dataIdentification.setCitationTitle(title);

	OBISResourceMapper.readString(json, SCIENTIFIC_NAME_KEY).ifPresent(scname ->

	OBISResourceMapper.readString(json, DATASETNAME_KEY).ifPresent(dname ->

	dataIdentification.setCitationTitle(scname + " from " + dname)

	));

	OBISResourceMapper.readString(json, REVISION_KEY).ifPresent(up ->

	ISO8601DateTimeUtils.parseISO8601ToDate(up.replace(" ", "T"))
		.ifPresent(t -> dataIdentification.addCitationDate(ISO8601DateTimeUtils.getISO8601DateTime(t), REVISION))

	);

	OBISResourceMapper.readString(json, PROVIDER_NAME_KEY).ifPresent(org -> {

	    ResponsibleParty responsibleParty = new ResponsibleParty();
	    responsibleParty.setRoleCode("originator");

	    responsibleParty.setOrganisationName(org);

	    dataIdentification.addPointOfContact(responsibleParty);
	});

	Set<String> keywords = extractKeywords(json);
	keywords.forEach(k -> miMetadata.getDataIdentification().addKeyword(k));

	return dataset;
    }

    /**
     * @param json
     * @return
     */
    private Set<String> extractKeywords(JSONObject json) {

	Set<String> ret = new HashSet<>();

	// -----------------------------------------
	// Taxonomy

	OBISResourceMapper.readString(json, GENUS_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, FAMILY_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, ORDER_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, CLASS_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, PHYLUM_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, SCIENTIFIC_NAME_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, SPECIES_NAME_KEY).ifPresent(ret::add);

	// -------------------------------------------

	OBISResourceMapper.readString(json, COLLECTION_CODE_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, ORIGINAL_SCIENTIFIC_NAME_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, SCINAME_AUTHORSHIP_KEY).ifPresent(ret::add);

	OBISResourceMapper.readString(json, SCIENTIFIC_NAME_ID_KEY).ifPresent(ret::add);

	return ret;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return OBIS_GRANULES_SCHEME_URI.toLowerCase();
    }
}
