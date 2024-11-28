/**
 * 
 */
package eu.essi_lab.accessor.mgnify;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class MGnifyConnector extends HarvestedQueryConnector<MGnifyConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "MGnifyConnector";

    /**
     * 
     */
    public MGnifyConnector() {
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	MGnifyClient client = new MGnifyClient(getSourceURL());

	boolean superStudiesApproach = false;

	List<Pages<Study>> pagedStudies = new ArrayList<Pages<Study>>();

	if (superStudiesApproach) {
	    Pages<SuperStudy> superStudiesPages = null;
	    try {
		superStudiesPages = client.getSelectedSuperStudies();
	    } catch (Exception e1) {
		e1.printStackTrace();
	    }

	    while (superStudiesPages != null) {
		List<SuperStudy> superStudies = superStudiesPages.getObjects();
		for (SuperStudy superStudy : superStudies) {
		    System.out.println("SS: " + superStudy.getUrlSlug() + " - " + superStudy.getTitle());
		    Pages<Study> studyPages = null;
		    try {
			studyPages = client.getStudies(superStudy.getFlagshipStudiesLink());
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }
		    pagedStudies.add(studyPages);
		}
		try {
		    superStudiesPages = superStudiesPages.getNext();
		} catch (Exception e) {
		    e.printStackTrace();
		    superStudiesPages = null;
		}
	    }

	} else {
	    Pages<Study> studyPages = null;
	    try {
		studyPages = client.getPages(new StudyFactory(), null, "biome_name=root%3AEnvironmental%3AAquatic%3AMarine");
	    } catch (Exception e1) {
		e1.printStackTrace();
	    }
	    pagedStudies.add(studyPages);
	}

	for (Pages<Study> studyPages : pagedStudies) {

	    while (studyPages != null) {
		List<Study> studies = studyPages.getObjects();
		GSLoggerFactory.getLogger(getClass()).info("Study page {} out of {}", studyPages.getPage(), studyPages.getPages());

		for (Study study : studies) {
		    // START STUDY
		    try {
			HashSet<String> parameters = new HashSet<>();
			HashSet<String> platforms = new HashSet<>();
			HashSet<String> instruments = new HashSet<>();
			HashSet<String> formats = new HashSet<>();
			HashSet<String> keywords = new HashSet<>();
			String organization = study.getCentreName();
			String title = study.getName();
			String summary = study.getAbstract();
			String resourceId = study.getAccession();
			String dateStamp = study.getLastUpdate();

			BigDecimal s = null;
			BigDecimal w = null;
			BigDecimal e = null;
			BigDecimal n = null;

			Date minDate = null;
			Date maxDate = null;
			DatasetCollection dataset = new DatasetCollection();

			CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

			MIMetadata miMetadata = coreMetadata.getMIMetadata();

			System.out.println(study.getName());
			System.out.println(study.getCentreName());
			System.out.println(study.getJSON().toString());
			System.out.println(study.getAccession() + " " + study.getSecondaryAccession());
			Pages<Sample> samplePages = client.getSamples(study.getSamplesLink());
			while (samplePages != null) {
			    GSLoggerFactory.getLogger(getClass()).info("Sample page {} out of {}", samplePages.getPage(),
				    samplePages.getPages());
			    List<Sample> samples = samplePages.getObjects();
			    for (Sample sample : samples) {

				BigDecimal lat = sample.getLatitude();
				BigDecimal lon = sample.getLongitude();

				if (lat != null && lon != null) {
				    s = (s == null || lat.compareTo(s) < 0) ? lat : s;
				    n = (n == null || lat.compareTo(n) > 0) ? lat : n;
				    w = (w == null || lon.compareTo(w) < 0) ? lon : w;
				    e = (e == null || lon.compareTo(e) > 0) ? lon : e;
				}

				String dateString = sample.getCollectionDate();
				if (dateString != null) {
				    Date date = ISO8601DateTimeUtils.parseISO8601(dateString);
				    if (minDate == null || date.before(minDate)) {
					minDate = date;
				    }
				    if (maxDate == null || date.after(maxDate)) {
					maxDate = date;
				    }
				}
				keywords.add(sample.getEnvironmentBiome());
				keywords.add(sample.getEnvironmentFeature());
				keywords.add(sample.getEnvironmentMaterial());
			    }

			    samplePages = samplePages.getNext();

			}
			Pages<Analysis> analysisPages = client.getAnalyses(study.getAnalysisLink());
			while (analysisPages != null) {
			    GSLoggerFactory.getLogger(getClass()).info("Analysis page {} out of {}", analysisPages.getPage(),
				    analysisPages.getPages());

			    List<Analysis> analyses = analysisPages.getObjects();
			    for (Analysis ana : analyses) {
				String dateString = ana.getCompleteTime();
				if (dateString != null) {
				    Date date = ISO8601DateTimeUtils.parseISO8601(dateString);
				    if (minDate == null || date.before(minDate)) {
					minDate = date;
				    }
				    if (maxDate == null || date.after(maxDate)) {
					maxDate = date;
				    }
				}
				String instrumentPlatform = ana.getInstrumentPlatform();
				if (instrumentPlatform != null) {
				    platforms.add(instrumentPlatform);
				}
				String instrumentModel = ana.getInstrumentModel();
				if (instrumentModel != null) {
				    instruments.add(instrumentModel);
				}
				Pages<Download> downloadPages = client.getDownloads(ana.getDownloadsLink());
				while (downloadPages != null) {
				    List<Download> downloads = downloadPages.getObjects();
				    for (Download download : downloads) {
					// System.out.println(download.getAlias() + " " + download.getDescription()
					// + "
					// "
					// + download.getFileFormat() + " " + download.getDownloadLink());
					formats.add(download.getFileFormat());
				    }

				    downloadPages = downloadPages.getNext();

				}

				Pages<TaxonomyLSU> taxonomyPages = client.getTaxonomiesLSU(ana.getTaxonomyLSULink());
				while (taxonomyPages != null) {
				    GSLoggerFactory.getLogger(getClass()).info("Taxonomy page {} out of {}", taxonomyPages.getPage(),
					    taxonomyPages.getPages());

				    List<TaxonomyLSU> taxonomies = taxonomyPages.getObjects();
				    for (TaxonomyLSU taxonomy : taxonomies) {
					String taxon = taxonomy.getId();
					parameters.add(taxon);
				    }

				    taxonomyPages = taxonomyPages.getNext();
				}
			    }

			    analysisPages = analysisPages.getNext();

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

			if (s != null && w != null && e != null && n != null) {
			    coreMetadata.addBoundingBox(n, w, s, e);
			}

			keywords.add("ELIXIR-ENA");
			Keywords k = new Keywords();

			for (String kwd : keywords) {
			    if (kwd != null && !kwd.isEmpty()) {
				k.addKeyword(kwd);
			    }
			}

			miMetadata.getDataIdentification().addKeywords(k);

			DataIdentification dataIdentification = miMetadata.getDataIdentification();
			addPointOfContact(dataIdentification, organization);

			dataIdentification.setCitationTitle(title);

			dataIdentification.setAbstract(summary);

			miMetadata.setDateStampAsDate(dateStamp);
			coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(dateStamp);

			if (minDate != null && maxDate != null) {
			    String minString = ISO8601DateTimeUtils.getISO8601Date(minDate);
			    String maxString = ISO8601DateTimeUtils.getISO8601Date(maxDate);
			    coreMetadata.addTemporalExtent(minString, maxString);
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

			String identifier = StringUtils.hashSHA1messageDigest("ELIXIR-ENA:" + resourceId);
			dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(identifier);
			miMetadata.setFileIdentifier(identifier);
			coreMetadata.getDataIdentification().setResourceIdentifier(resourceId);

			String str = dataset.asString(true);
			OriginalMetadata record = new OriginalMetadata();
			record.setMetadata(str);
			record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
			ret.addRecord(record);

			// END STUDY

		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).error("Error for MGNIFY study {}, {}", study.getId(), e.getMessage());
			e.printStackTrace();
		    }
		    // return ret;
		}
		try {
		    studyPages = studyPages.getNext();
		} catch (Exception e) {
		    e.printStackTrace();
		    studyPages = null;
		}
	    }

	}

	return ret;

    }

    private void addPointOfContact(DataIdentification dataIdentification, String centerName) {
	ResponsibleParty pointOfContact = new ResponsibleParty();
	pointOfContact.setOrganisationName(centerName);
	dataIdentification.addPointOfContact(pointOfContact);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://www.ebi.ac.uk/metagenomics");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected MGnifyConnectorSetting initSetting() {

	return new MGnifyConnectorSetting();
    }
}
