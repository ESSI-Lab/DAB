/**
 * 
 */
package eu.essi_lab.accessor.elixena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

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

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class ElixirENAConnector extends HarvestedQueryConnector<ElixirEnaConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ElixirEnaConnector";

    /**
     * 
     */
    public static final String FIRST_STEP_LOW_QUERY_STRING = "study_accession%3D%22*%22%20AND%20(tag%3D%22env_tax%3Amarine%22%20OR%20tag%3D%22env_tax%3Abrackish%22)";
    public static final String FIRST_STEP_MEDIUM_QUERY_STRING = "study_accession%3D%22*%22%20AND%20(%20tag%3D%22env_tax%3Amarine%22%20AND%20tag!%3D%22env_tax%3Abrackish%22%20AND%20tag!%3D%22env_tax%3Afreshwater%22%20AND%20tag!%3D%22env_tax%3Aterrestrial%22%20AND%20tag!%3D%22env_geo%3Amarine%22%20AND%20tag!%3D%22env_geo%3Acoastal%22%20AND%20tag!%3D%22env_geo%3Afreshwater%22%20AND%20tag!%3D%22env_geo%3Aterrestrial%22%20)%20OR%20(%20tag%3D%22env_tax%3Amarine%22%20AND%20(%20tag%3D%22env_tax%3Abrackish%22%20OR%20tag%3D%22env_tax%3Afreshwater%22%20OR%20tag%3D%22env_tax%3Aterrestrial%22%20)%20AND%20tag%3D%22env_geo%3Amarine%22%20AND%20(%20tag%3D%22env_geo%3Acoastal%22%20OR%20tag%3D%22env_geo%3Afreshwater%22%20OR%20tag%3D%22env_geo%3Aterrestrial%22%20)%20)%20OR%20(%20tag%3D%22env_geo%3Amarine%22%20AND%20tag!%3D%22env_geo%3Acoastal%22%20AND%20tag!%3D%22env_geo%3Afreshwater%22%20AND%20tag!%3D%22env_geo%3Aterrestrial%22%20AND%20tag!%3D%22env_tax%3Amarine%22%20AND%20tag!%3D%22env_tax%3Abrackish%22%20AND%20tag!%3D%22env_tax%3Afreshwater%22%20AND%20tag!%3D%22env_tax%3Aterrestrial%22%20)";
    public static final String FIRST_STEP_HIGH_QUERY_STRING = "study_accession%3D%22*%22%20AND%20(%20tag%3D%22env_tax%3Amarine%22%20AND%20tag%3D%22env_geo%3Amarine%22%20)%20AND%20(%20(%20tag!%3D%22env_tax%3Abrackish%22%20AND%20tag!%3D%22env_tax%3Afreshwater%22%20AND%20tag!%3D%22env_tax%3Aterrestrial%22%20)%20OR%20(%20tag!%3D%22env_geo%3Acoastal%22%20AND%20tag!%3D%22env_geo%3Afreshwater%22%20AND%20tag!%3D%22env_geo%3Aterrestrial%22%20)%20)";
    public static final String FIRST_STEP_MEDIUM_HIGH_QUERY_STRING = "(" + FIRST_STEP_MEDIUM_QUERY_STRING + ")%20OR%20("
	    + FIRST_STEP_HIGH_QUERY_STRING + ")";
    public static final String SECOND_STEP_QUERY_STRING = "query=(study_accession=%22STUDY_IDENTIFIER%22)&result=study&fields=breed,broker_name,center_name,cultivar,first_public,geo_accession,isolate,keywords,last_updated,parent_study_accession,scientific_name,secondary_study_accession,strain,study_accession,study_description,study_name,study_title,tax_id&limit=1&download=true&format=json";
    public static final String THIRD_STEP_QUERY_STRING = "query=study_accession=%22STUDY_IDENTIFIER%22&result=read_run&fields=sequencing_method,collection_date,depth,description,elevation,environment_biome,environment_feature,environment_material,environmental_medium,environmental_sample,fastq_ftp,first_created,instrument_model,instrument_platform,investigation_type,last_updated,lat,library_name,location,lon,project_name,sampling_campaign,sampling_platform,scientific_name,sra_ftp,submitted_format,submitted_ftp&limit=LIMIT&download=true&format=json&offset=OFFSET";

    /**
     * 
     */
    private static final String ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDIES_ARRAY = "ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDIES_ARRAY";

    /**
     * 
     */
    private static final String ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDY_RECORDS = "ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDY_RECORDS";

    private static final int LIMIT = 500;

    private LinkedHashSet<String> studyArray = null;

    /**
     * 
     */
    public ElixirENAConnector() {
    }

    /**
     * @return
     */
    public String getHarvestingType() {

	return getSetting().getHarvestingType();
    }

    /**
     * @param query
     * @return
     */
    private Optional<File> download(String query) {

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.MINUTES, 1);

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(query);

	    InputStream inputStream = response.body();

	    if (Objects.nonNull(inputStream)) {

		File tmpFile = File.createTempFile("elixir-ena", ".txt");
		tmpFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(inputStream, fos);
		inputStream.close();

		return Optional.of(tmpFile);

	    } else {

		GSLoggerFactory.getLogger(getClass()).error("null response");
		return Optional.empty();
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(query + " " + e.getMessage(), e);
	}

	return Optional.empty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	String harvestingType = getHarvestingType();

	GSLoggerFactory.getLogger(getClass()).info("ELIXIR-ENA Harvesting type is {}", harvestingType);

	if (harvestingType == null) {
	    harvestingType = ElixirEnaConnectorSetting.DEFAULT_STUDIES;
	}

	if (harvestingType.equals(ElixirEnaConnectorSetting.DEFAULT_STUDIES)) {

	    String[] superProjects = new String[] { //
		    "PRJEB402", // Tara Oceans Metagenome
		    "PRJEB5129", // Ocean Sampling Day
		    "PRJNA330770" // Malaspina
	    };

	    String base = getSourceURL().replace("portal/api/search", "browser/api/xml/").replace("?", "");

	    studyArray = new LinkedHashSet<>();

	    for (String superProject : superProjects) {

		Optional<File> d = download(base + superProject);
		if (d.isPresent()) {

		    try {
			XMLDocumentReader reader = new XMLDocumentReader(d.get());
			Node[] nodes = reader.evaluateNodes("//*:CHILD_PROJECT/@accession");
			if (nodes.length == 0) {
			    studyArray.add(superProject);
			} else {
			    for (Node node : nodes) {
				String id = reader.evaluateString(node, ".");
				studyArray.add(id);
			    }
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    } finally {
			d.get().delete();
		    }

		}
	    }

	}

	if (Objects.isNull(studyArray)) {

	    studyArray = new LinkedHashSet<String>();
	    String firstStepQuery = getSourceURL() + "result=read_study&fields=study_accession&format=tsv&query=";

	    switch (harvestingType) {
	    case ElixirEnaConnectorSetting.LOW_CONFIDENCE:
		firstStepQuery += FIRST_STEP_LOW_QUERY_STRING;
		break;
	    case ElixirEnaConnectorSetting.MEDIUM_CONFIDENCE:
		firstStepQuery += FIRST_STEP_MEDIUM_QUERY_STRING;
		break;
	    case ElixirEnaConnectorSetting.MEDIUM_PLUS_HIGH_CONFIDENCE:
		firstStepQuery += FIRST_STEP_MEDIUM_HIGH_QUERY_STRING;
		break;
	    default:
	    case ElixirEnaConnectorSetting.HIGH_CONFIDENCE:
		firstStepQuery += FIRST_STEP_HIGH_QUERY_STRING;
		break;
	    }

	    Optional<File> download = download(firstStepQuery);

	    if (!download.isPresent()) {

		throw GSException.createException(//
			getClass(), //
			"Unable to download studies array", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDIES_ARRAY);
	    }

	    File file = download.get();

	    try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
		String line;
		while ((line = br.readLine()) != null) {
		    String[] split = line.split("\t");
		    studyArray.add(split[0]);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    } finally {
		file.delete();
	    }
	}

	String resumptionToken = request.getResumptionToken();
	Integer i;
	if (resumptionToken == null) {
	    i = 0;
	} else {
	    i = Integer.parseInt(resumptionToken);
	}
	if (i == studyArray.size() - 1) {
	    resumptionToken = null;
	} else {
	    resumptionToken = "" + (i + 1);
	}

	String studyId = studyArray.toArray(new String[] {})[i];

	GSLoggerFactory.getLogger(getClass()).info("Current study [" + i + "/" + (studyArray.size() - 1) + "] STARTED");

	String secondStepQuery = getSourceURL() + SECOND_STEP_QUERY_STRING;

	secondStepQuery = secondStepQuery.replace("STUDY_IDENTIFIER", studyId);

	GSLoggerFactory.getLogger(getClass()).info("Study name [" + studyId + "]");

	Optional<File> optResponse = download(secondStepQuery);

	if (!optResponse.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to download study records. Study [" + studyId + "], ", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDY_RECORDS);
	}

	String response = null;
	try {
	    response = Files.readString(optResponse.get().toPath());
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
	optResponse.get().delete();
	System.out.println(response);

	JSONArray a = new JSONArray(response);
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	if (a.length() > 0) {
	    HashSet<String> keywords = new HashSet<>();
	    HashSet<String> projects = new HashSet<>();
	    JSONObject json = a.getJSONObject(0);
	    Optional<String> centerName = getString(json, "center_name");
	    Optional<String> keyword = getString(json, "keywords");
	    if (keyword.isPresent()) {
		keywords.add(keyword.get());
	    }
	    Optional<String> geoAccession = getString(json, "geo_accession");
	    Optional<String> lastUpdated = getString(json, "last_updated");
	    Optional<String> studyDescription = getString(json, "study_description");
	    Optional<String> studyTitle = getString(json, "study_title");
	    Optional<String> studyAccession = getString(json, "study_accession");

	    System.out.println("Study id " + studyId);
	    print(centerName, keyword, geoAccession, lastUpdated, studyDescription, studyTitle);

	    System.out.println();

	    DatasetCollection dataset = new DatasetCollection();

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    MIMetadata miMetadata = coreMetadata.getMIMetadata();

	    JSONArray jsonArray = getSamples(studyId, 0, 0);

	    Double s = null;
	    Double w = null;
	    Double e = null;
	    Double n = null;

	    Date minDate = null;
	    Date maxDate = null;

	    Double minDepth = null;
	    Double maxDepth = null;

	    HashSet<String> parameters = new HashSet<>();
	    HashSet<String> instruments = new HashSet<>();
	    HashSet<String> platforms = new HashSet<>();

	    HashSet<String> formats = new HashSet<>();

	    for (int j = 0; j < jsonArray.length(); j++) {
		try {
		    JSONObject object = jsonArray.getJSONObject(j);

		    Optional<Double> lat = getDouble(object, "lat");
		    Optional<Double> lon = getDouble(object, "lon");

		    Optional<String> location = getString(object, "location");

		    if (location.isPresent()) {

			// e.g."47.71992 N 8.895856 E"

			if (location.get().contains("E") && location.get().contains("N")) {

			    String latString = location.get().split(" ")[0];
			    String lonString = location.get().split(" ")[2];

			    lat = Optional.of(Double.valueOf(latString));
			    lon = Optional.of(Double.valueOf(lonString));

			}
		    }

		    if (lat.isPresent()) {
			s = (s == null || lat.get() < s) ? lat.get() : s;
			n = (n == null || lat.get() > n) ? lat.get() : n;
		    }

		    if (lon.isPresent()) {
			w = (w == null || lon.get() < w) ? lon.get() : w;
			e = (e == null || lon.get() > e) ? lon.get() : e;
		    }

		    //
		    // temporal extent
		    //
		    Optional<String> collectionDate = getString(object, "collection_date");
		    if (!collectionDate.isPresent()) {
			collectionDate = getString(object, "first_created");
		    }
		    if (!collectionDate.isPresent()) {
			collectionDate = getString(object, "last_updated");
		    }

		    if (collectionDate.isPresent()) {

			try {
			    Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(collectionDate.get());
			    if (parsed.isPresent()) {
				minDate = (minDate == null || parsed.get().before(minDate)) ? parsed.get() : minDate;
				maxDate = (maxDate == null || parsed.get().after(maxDate)) ? parsed.get() : maxDate;
			    }
			} catch (RuntimeException parseEx) {
			    String error = "Unable to parse date: " + collectionDate.get();
			    GSLoggerFactory.getLogger(getClass()).error(error);
			    GSLoggerFactory.getLogger(getClass()).info("Trying again to parse dates ");
			    String date = collectionDate.get();
			    if (date.contains("missing") || date.contains("not")) {
				GSLoggerFactory.getLogger(getClass()).debug("Date missing");
			    } else {
				String[] splittedDate = date.split("/");
				if (splittedDate.length > 2) {
				    GSLoggerFactory.getLogger(getClass()).debug("Not valid format date");
				} else {

				    Optional<Date> beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(splittedDate[0]);
				    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(splittedDate[1]);
				    if (beginDate.isPresent()) {
					minDate = (minDate == null || beginDate.get().before(minDate)) ? beginDate.get() : minDate;
				    }
				    if (endDate.isPresent()) {
					maxDate = (maxDate == null || endDate.get().after(maxDate)) ? endDate.get() : maxDate;
				    }

				}
			    }
			}

		    }

		    //
		    // vertical
		    //
		    Optional<Double> depth = getDouble(object, "depth");
		    if (depth.isPresent()) {
			minDepth = (minDepth == null || depth.get() < minDepth) ? depth.get() : minDepth;
			maxDepth = (maxDepth == null || depth.get() > maxDepth) ? depth.get() : maxDepth;

		    }

		    // parameters
		    Optional<String> scientificName = getString(object, "scientific_name");

		    if (scientificName.isPresent()) {
			parameters.add(scientificName.get());
		    }

		    Optional<String> instrument = getString(object, "instrument_model");

		    if (!instrument.isPresent()) {
			instrument = getString(object, "sequencing_method");
		    }

		    if (instrument.isPresent()) {
			instruments.add(instrument.get());
		    }

		    // platform
		    Optional<String> platform = getString(object, "sampling_platform");
		    if (!platform.isPresent()) {
			platform = getString(object, "instrument_platform");
		    }

		    if (platform.isPresent()) {
			platforms.add(platform.get());
		    }
		    // formats
		    Optional<String> format = getString(object, "submitted_format");
		    if (format.isPresent()) {
			String f = format.get();
			if (f.contains(";")) {
			    String[] split = f.split(";");
			    for (String ff : split) {
				formats.add(ff);
			    }
			} else {
			    formats.add(f);
			}
		    }

		    //
		    // keywords
		    //

		    addIfPresent(getString(object, "environment_biome"), keywords);
		    addIfPresent(getString(object, "environment_feature"), keywords);
		    addIfPresent(getString(object, "environment_material "), keywords);
		    addIfPresent(getString(object, "environmental_medium"), keywords);
		    addIfPresent(getString(object, "environmental_sample"), keywords);
		    addIfPresent(getString(object, "investigation_type"), keywords);
		    addIfPresent(getString(object, "country"), keywords);
		    addIfPresent(getString(object, "sample_alias"), keywords);
		    addIfPresent(getString(object, "project_name"), projects);

		} catch (Exception ee) {
		    ee.printStackTrace();
		}

	    }

	    for (String parameter : parameters) {
		CoverageDescription description = new CoverageDescription();
		// description.setAttributeIdentifier(parameter);
		description.setAttributeDescription(parameter);
		description.setAttributeTitle(parameter);
		coreMetadata.getMIMetadata().addCoverageDescription(description);

	    }

	    for (String instrument : instruments) {

		MIInstrument myInstrument = new MIInstrument();
		myInstrument.setMDIdentifierTypeIdentifier(instrument);
		// myInstrument.setMDIdentifierTypeCode(sensorId);
		myInstrument.setDescription(instrument);
		myInstrument.setTitle(instrument);
		// myInstrument.getElementType().getCitation().add(e)
		coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		Keywords k = new Keywords();
		k.setTypeCode("instrument");
		k.addKeyword(instrument);
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(k);
	    }

	    if (s != null && w != null) {
		coreMetadata.addBoundingBox(n, w, s, e);
	    }

	    if (minDepth != null && maxDepth != null) {
		miMetadata.getDataIdentification().addVerticalExtent(-maxDepth, -minDepth);
	    }

	    keywords.add("ELIXIR-ENA");
	    Keywords k = new Keywords();

	    for (String kwd : keywords) {
		if (!kwd.isEmpty()) {
		    k.addKeyword(kwd);
		}
	    }

	    miMetadata.getDataIdentification().addKeywords(k);

	    DataIdentification dataIdentification = miMetadata.getDataIdentification();
	    if (centerName.isPresent()) {
		addPointOfContact(dataIdentification, centerName.get());
	    }

	    if (studyTitle.isPresent()) {
		dataIdentification.setCitationTitle(studyTitle.get());
	    }

	    if (studyDescription.isPresent()) {
		dataIdentification.setAbstract(studyDescription.get());
	    }

	    Date dateStamp = null;

	    if (lastUpdated.isPresent()) {
		Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(lastUpdated.get());
		if (d.isPresent()) {
		    dateStamp = d.get();
		}
	    }

	    if (minDate != null && maxDate != null) {
		String minString = ISO8601DateTimeUtils.getISO8601Date(minDate);
		String maxString = ISO8601DateTimeUtils.getISO8601Date(maxDate);
		coreMetadata.addTemporalExtent(minString, maxString);
		if (dateStamp != null && maxDate.after(dateStamp)) {
		    dateStamp = maxDate;
		}
	    }

	    if (dateStamp != null) {
		miMetadata.setDateStampAsDate(ISO8601DateTimeUtils.getISO8601Date(dateStamp));
		coreMetadata.getMIMetadata().getDataIdentification()
			.setCitationRevisionDate(ISO8601DateTimeUtils.getISO8601DateTime(dateStamp));
	    }

	    for (String p : platforms) {
		MIPlatform platform = new MIPlatform();
		platform.setMDIdentifierCode(p);
		platform.setDescription(p);
		Citation platformCitation = new Citation();
		platformCitation.setTitle(p);
		platform.setCitation(platformCitation);
		coreMetadata.getMIMetadata().addMIPlatform(platform);
		Keywords kp = new Keywords();
		kp.setTypeCode("platform");
		kp.addKeyword(p);
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kp);
	    }

	    for (String format : formats) {
		Format f = new Format();
		f.setName(format);
		coreMetadata.getMIMetadata().getDistribution().addFormat(f);
	    }

	    if (projects != null && !projects.isEmpty()) {
		Keywords kkk = new Keywords();
		kkk.setTypeCode("project");
		for (String project : projects) {
		    kkk.addKeyword(project.trim());
		}
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kkk);
	    }

	    try {

		try {
		    String identifier = StringUtils.hashSHA1messageDigest("ELIXIR-ENA:" + studyId);
		    dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(identifier);
		    miMetadata.setFileIdentifier(identifier);
		    if (studyAccession.isPresent()) {
			coreMetadata.getDataIdentification().setResourceIdentifier(studyAccession.get());
		    } else {
			coreMetadata.getDataIdentification().setResourceIdentifier(studyId);
		    }
		} catch (Exception ee) {
		    ee.printStackTrace();
		}

		String str = dataset.asString(true);
		OriginalMetadata record = new OriginalMetadata();
		record.setMetadata(str);
		record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		ret.addRecord(record);
	    } catch (Exception ee) {
		ee.printStackTrace();
	    }

	}
	ret.setResumptionToken(resumptionToken);
	return ret;

    }

    private void addIfPresent(Optional<String> string, HashSet<String> keywords) {
	if (string.isPresent()) {
	    keywords.add(string.get());
	}

    }

    private void addPointOfContact(DataIdentification dataIdentification, String centerName) {
	ResponsibleParty pointOfContact = new ResponsibleParty();
	pointOfContact.setOrganisationName(centerName);
	dataIdentification.addPointOfContact(pointOfContact);
    }

    private void print(Optional<String>... opts) {
	for (Optional<String> opt : opts) {
	    opt.ifPresent(o -> System.out.println(o));
	}

    }

    /**
     * @param object
     * @param key
     * @return
     */
    private Optional<String> getString(JSONObject object, String key) {

	try {
	    if (!object.has(key)) {
		return Optional.empty();
	    }
	    String k = object.getString(key);
	    if (k.isEmpty()) {
		return Optional.empty();
	    }
	    return Optional.of(k);

	} catch (Exception ex) {
	}

	return Optional.empty();
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private Optional<Double> getDouble(JSONObject object, String key) {

	try {
	    return Optional.of(object.getDouble(key));

	} catch (Exception ex) {
	}

	return Optional.empty();
    }

    public ListRecordsResponse<OriginalMetadata> getGranules(String resumptionToken) throws GSException {
	ListRecordsResponse<OriginalMetadata> listRecordsResponse = new ListRecordsResponse<>();

	int studyIndex = 0;
	int offset = 0;

	if (Objects.nonNull(resumptionToken)) {

	    studyIndex = Integer.valueOf(resumptionToken.split("SEP")[0]);
	    offset = Integer.valueOf(resumptionToken.split("SEP")[1]);
	}

	if (studyIndex == 0 && offset == 0) {

	    GSLoggerFactory.getLogger(getClass()).info("Process STARTED");
	}

	String studyName = studyArray.toArray(new String[] {})[studyIndex];

	GSLoggerFactory.getLogger(getClass()).info("Current study [" + studyIndex + "/" + (studyArray.size() - 1) + "] STARTED");

	JSONArray jsonArray = getSamples(studyName, offset, LIMIT);

	GSLoggerFactory.getLogger(getClass()).info("Retrieved [" + jsonArray.length() + "] records");

	for (int j = 0; j < jsonArray.length(); j++) {

	    JSONObject jsonObject = jsonArray.getJSONObject(j);

	    OriginalMetadata metadata = new OriginalMetadata();
	    metadata.setMetadata(jsonObject.toString(3));
	    metadata.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);

	    listRecordsResponse.addRecord(metadata);
	}

	int nextOffset = 0;

	if (jsonArray.length() == LIMIT) {

	    //
	    // current study not yet done
	    //
	    nextOffset = offset + LIMIT;

	} else {

	    GSLoggerFactory.getLogger(getClass())
		    .info("Current study " + studyName + " [" + studyIndex + "/" + (studyArray.size() - 1) + "] ENDED");

	    //
	    // current study done
	    // ready for the next study
	    //
	    studyIndex++;
	}

	resumptionToken = null;

	if (studyIndex == studyArray.size()) {

	    GSLoggerFactory.getLogger(getClass()).info("Process ENDED");
	} else {

	    resumptionToken = studyIndex + "SEP" + nextOffset;
	}

	listRecordsResponse.setResumptionToken(resumptionToken);

	return listRecordsResponse;
    }

    private JSONArray getSamples(String studyName, int offset, int limit) throws GSException {
	String secondStepQuery = getSourceURL() + THIRD_STEP_QUERY_STRING;

	secondStepQuery = secondStepQuery.replace("STUDY_IDENTIFIER", studyName);
	secondStepQuery = secondStepQuery.replace("OFFSET", String.valueOf(offset));
	secondStepQuery = secondStepQuery.replace("LIMIT", String.valueOf(limit));

	GSLoggerFactory.getLogger(getClass()).info("Study name [" + studyName + "]");
	GSLoggerFactory.getLogger(getClass()).info("Study offset [" + offset + "]");

	Optional<File> optResponse = download(secondStepQuery);

	if (!optResponse.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to download study records. Study [" + studyName + "], offset [" + offset + "]", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ELIXIR_ENA_UNABLE_TO_DOWNLOAD_STUDY_RECORDS);
	}

	File response = optResponse.get();
	String str = null;
	try {
	    str = Files.readString(Paths.get(response.getAbsolutePath()));
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("reading file");
	} finally {
	    response.delete();
	}

	JSONArray jsonArray = new JSONArray(str);
	return jsonArray;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://www.ebi.ac.uk/ena/portal/api/search");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(new String[] { "ELIXIR-ENA_JSON" });
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ElixirEnaConnectorSetting initSetting() {

	return new ElixirEnaConnectorSetting();
    }

    public static void main(String[] args) {
	String[] dates = { "27/09/2010", "not provided", "2018-06-21T11:00:00Z/2018-06-21T12:00:00Z", "2012-07-15/2012-07-31", "missing" };
	for (String d : dates) {
	    if (d.contains("missing") || d.contains("not")) {
		System.out.println("not supported");
		continue;
	    }
	    String[] splittedDate = d.split("/");
	    if (splittedDate.length > 2) {
		System.out.println("not supported");
		continue;
	    } else {

		Optional<Date> begin = ISO8601DateTimeUtils.parseISO8601ToDate(splittedDate[0]);
		Optional<Date> end = ISO8601DateTimeUtils.parseISO8601ToDate(splittedDate[1]);
		Date date1 = begin.get();
		Date date2 = end.get();
		System.out.println(date1);
		System.out.println(date2);
	    }
	}
    }
}
