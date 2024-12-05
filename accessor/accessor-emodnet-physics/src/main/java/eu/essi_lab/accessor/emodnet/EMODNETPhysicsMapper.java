package eu.essi_lab.accessor.emodnet;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.edmo.EDMOClient;
import eu.essi_lab.lib.net.nvs.NVSClient;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmd.v_20060504.CIRoleCodePropertyType;

public class EMODNETPhysicsMapper extends FileIdentifierMapper {

	@Override
	public String getSupportedOriginalMetadataSchema() {
		return CommonNameSpaceContext.EMODNET_PHYSICS_NS_URI;
	}

	@Override
	protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
		DatasetCollection dataset = new DatasetCollection();
		dataset.setSource(source);

		mapMetadata(originalMD, dataset);

		return dataset;
	}

	private class Party {
		private String name;
		private String role;
		private String uri; // concept from ontology
		boolean organization = true;
		private String email;
		private String url; // organization homepage

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Party) {
				Party party = (Party) obj;
				if (party.getUri() != null && party.getUri().equals(uri)) {
					if ((party.getRole() == null && role == null) || (party.getRole().equals(role))) {
						return true;
					}
				}
				if (party.getName() != null && party.getName().equals(name)) {
					if ((party.getRole() == null && role == null) || (party.getRole().equals(role))) {
						return true;
					}
				}
			}
			return super.equals(obj);
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public boolean isOrganization() {
			return organization;
		}

		public void setOrganization(boolean organization) {
			this.organization = organization;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

	}

	private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

		GSLoggerFactory.getLogger(getClass()).info("EMODNet Physics Mappper STARTED");

		String originalMetadata = originalMD.getMetadata();

		JSONObject json = new JSONObject(originalMetadata);

		JSONArray countries = null;
		JSONArray dataOwners = null;
		JSONArray platforms = null;

		if (json.has("additionalMetadata")) {
			JSONObject additionalMetadata = json.getJSONObject("additionalMetadata");
			countries = additionalMetadata.optJSONArray("metadata_countries");
			dataOwners = additionalMetadata.optJSONArray("metadata_data_owners");
			platforms = additionalMetadata.optJSONArray("metadata_platforms");
		}

		String linkage = null;
		JSONArray rows = json.getJSONObject("table").getJSONArray("rows");
		String title = null;
		String description = null;
		LinkedHashSet<String> sensorURNs = new LinkedHashSet<>();
		LinkedHashSet<String> sensorLabels = new LinkedHashSet<>();
		LinkedHashSet<String> parameterURNs = new LinkedHashSet<>();
		LinkedHashSet<String> parameterLabels = new LinkedHashSet<>();
		String west = null, south = null, east = null, north = null, up = null, down = null;
		String verticalUnits = null;

		String beginPosition = null;
		String endPosition = null;
		String project = null;
		String lineage = null;
//	HashSet<String> platformNames = new HashSet<String>();
		String license = null;
		String timeStamp = null;
		List<String> keywords = new ArrayList<>();
		String keywordsVoc = null;

		List<String> institutions = new ArrayList<String>();
		List<String> institutionURIs = new ArrayList<String>();

		List<String> creatorNames = new ArrayList<String>();
		List<String> creatorEmails = new ArrayList<String>();
		List<String> creatorTypes = new ArrayList<String>();
		List<String> creatorURLs = new ArrayList<String>();

		List<String> publisherNames = new ArrayList<String>();
		List<String> publisherEmails = new ArrayList<String>();
		List<String> publisherTypes = new ArrayList<String>();
		List<String> publisherURLs = new ArrayList<String>();

		List<String> piNames = new ArrayList<String>();
		List<String> piEmails = new ArrayList<String>();

		String dataIdentifier = null;

		HashSet<Party> parties = new HashSet<>();

		HashMap<String, HashMap<String, String>> attributesByVariable = new HashMap<String, HashMap<String, String>>();

		List<SimpleEntry<String, String>> platformNameCode = new ArrayList<>();

		if (platforms != null) {
			for (int i = 0; i < platforms.length(); i++) {
				String code = platforms.getJSONArray(i).optString(0);
				String name = platforms.getJSONArray(i).optString(1);
				platformNameCode.add(new SimpleEntry<String, String>(name, code));
			}
		}

		List<String> tmpPNames = new ArrayList<>();
		List<String> tmpPCodes = new ArrayList<>();
		for (int i = 0; i < rows.length(); i++) {
			JSONArray row = rows.getJSONArray(i);
			String type = row.getString(0);
			String var = row.getString(1);
			String name = row.getString(2);
			String dataType = row.getString(3);
			String value = row.getString(4);
			switch (var) {
			case "NC_GLOBAL":
				switch (name) {
				case "id":
					dataIdentifier = value;
					break;
				case "title":
					title = value;
					break;
				case "platform_name":
					addSeparatedValues(tmpPNames, value);
					break;
				case "platform_code":
					addSeparatedValues(tmpPCodes, value);
					break;
				case "license":
					license = value;
					break;
				case "project":
					project = value;
					break;
				case "history":
					lineage = value;
					break;
				case "summary":
					description = value;
					break;
				case "SDN":
					addSeparatedValues(parameterURNs, value);
					break;
				case "geospatial_lat_max":
					north = value;
					break;
				case "geospatial_lat_min":
					south = value;
					break;
				case "geospatial_lon_max":
					east = value;
					break;
				case "geospatial_lon_min":
					west = value;
					break;
				case "geospatial_vertical_max":
					up = value;
					break;
				case "geospatial_vertical_min":
					down = value;
					break;
				case "geospatial_vertical_units":
					verticalUnits = value;
					break;
				case "time_coverage_start":
					beginPosition = value;
					break;
				case "time_coverage_end":
					endPosition = value;
					break;
				case "institution":
					addSeparatedValues(institutions, value);
					break;
				case "creator_name":
					addSeparatedValues(creatorNames, value);
					break;
				case "creator_email":
					addSeparatedValues(creatorEmails, value);
					break;
				case "creator_type":
					addSeparatedValues(creatorTypes, value);
					break;
				case "creator_url":
					addSeparatedValues(creatorURLs, value);
					break;
				case "publisher_name":
					addSeparatedValues(publisherNames, value);
					break;
				case "publisher_email":
					addSeparatedValues(publisherEmails, value);
					break;
				case "publisher_type":
					addSeparatedValues(publisherTypes, value);
					break;
				case "publisher_url":
					addSeparatedValues(publisherURLs, value);
					break;
				case "principal_investigator":
					addSeparatedValues(piNames, value);
					break;
				case "principal_investigator_email":
					addSeparatedValues(piEmails, value);
					break;
				case "keywords_vocabulary":
					keywordsVoc = value;
					break;
				case "institution_edmo_uri":
					addSeparatedValues(institutionURIs, value);
					break;
				case "keywords":
					addSeparatedValues(keywords, value);
					break;
				case "date_update":
					timeStamp = value;
					break;

				default:
					break;
				}
				break;

			default:
				break;
			}

			HashMap<String, String> attributes = attributesByVariable.get(var);
			if (attributes == null) {
				attributes = new HashMap<>();
				attributesByVariable.put(var, attributes);
			}
			attributes.put(name, value);
		}

		if (tmpPCodes.size() == tmpPNames.size()) {
			for (int i = 0; i < tmpPCodes.size(); i++) {
				String code = tmpPCodes.get(i);
				String name = tmpPNames.get(i);
				platformNameCode.add(new SimpleEntry<String, String>(name, code));
			}
		}else {
			for (int i = 0; i < tmpPNames.size(); i++) {
				String name = tmpPNames.get(i);
				platformNameCode.add(new SimpleEntry<String, String>(name, null));
			}
		}

		if (dataOwners != null) {
			for (int j = 0; j < dataOwners.length(); j++) {
				JSONArray arr = dataOwners.getJSONArray(j);
				String label = arr.optString(1);

				Integer edmo = arr.optIntegerObject(0);
				Party party = new Party();
				if (edmo != null) {
					EDMOClient client = new EDMOClient();
					label = client.getLabelFromCode("" + edmo);
					party.setUri(client.getURN("" + edmo));
				}
				party.setOrganization(true);
				party.setName(label);
				party.setRole("originator");
				parties.add(party);

			}

		}


		List<String> empty = new ArrayList<>();
		List<Party> originatorParties = getParties(institutions, institutionURIs, empty, empty, empty, "originator");
		parties.addAll(originatorParties);

		List<Party> publisherParties = getParties(publisherNames, empty, publisherEmails, publisherURLs, publisherTypes,
				"publisher");
		parties.addAll(publisherParties);

		List<Party> creatorParties = getParties(creatorNames, empty, creatorEmails, creatorURLs, creatorTypes,
				"originator");
		parties.addAll(creatorParties);

		List<String> persons = new ArrayList<String>();
		for (int j = 0; j < piNames.size(); j++) {
			persons.add("person");
		}
		List<Party> piParties = getParties(piNames, empty, piEmails, empty, persons, "principalInvestigator");
		parties.addAll(piParties);

		Set<String> variables = attributesByVariable.keySet();
		for (String variable : variables) {
			HashMap<String, String> attributes = attributesByVariable.get(variable);
			String parURN = attributes.get("sdn_parameter_urn");
			if (parURN != null) {
				addSeparatedValues(parameterURNs, parURN);
			}
			String parLabel = attributes.get("sdn_parameter_name");
			if (parLabel != null) {
				addSeparatedValues(parameterLabels, parLabel);
			}
			String sURN = attributes.get("sensor_reference");
			if (sURN != null && !sURN.isEmpty() && !sURN.equals("NaN")) {
				addSeparatedValues(sensorURNs, sURN);
			}
			String sLabel = attributes.get("sensor_model");
			if (sLabel != null) {
				addSeparatedValues(sensorLabels, sLabel);
			}

		}

		GSPropertyHandler additionalInfo = originalMD.getAdditionalInfo();
		if (additionalInfo != null) {
			Boolean isDownloadLink = originalMD.getAdditionalInfo().get("isDownloadLink", Boolean.class);
			if (isDownloadLink) {
				linkage = originalMD.getAdditionalInfo().get("downloadLink", String.class);
			}
		}

		CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

		coreMetadata.getMIMetadata().setHierarchyLevelName("series");
		coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

		String identifier = json.getString("identifier");
		coreMetadata.setIdentifier(identifier);
		coreMetadata.getMIMetadata().setFileIdentifier(identifier);

		if (countries != null) {
			for (int i = 0; i < countries.length(); i++) {
				String country = countries.getString(i);
				dataset.getExtensionHandler().setCountry(country);
			}
		}

		coreMetadata.getDataIdentification().setResourceIdentifier(dataIdentifier);

		if (linkage != null) {
			Online online = new Online();
			online.setLinkage(linkage);
			online.setProtocol("GET DATA");
			online.setName(identifier);
			online.setFunctionCode("download");
			online.setDescription("NetCDF Download");
			coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		}

		// title
		if (title != null && !title.isEmpty())

		{
			coreMetadata.setTitle(title);
		}
		// abstract
		if (description != null && !description.isEmpty()) {
			coreMetadata.setAbstract(description);
		}
		// keywords and PARAMETER IDENTIFIERS

		if (parameterURNs != null && !parameterURNs.isEmpty()) {

			for (int i = 0; i < parameterURNs.size(); i++) {

				String urn = parameterURNs.toArray(new String[] {})[i];

				if (urn.contains("P01")) {
					if (urn.contains("::")) {
						urn = urn.split("::")[1];
						urn = "http://vocab.nerc.ac.uk/collection/P01/current/" + urn + "/";
					}
				} else if (urn.contains("P02")) {
					if (urn.contains("::")) {
						urn = urn.split("::")[1];
						urn = "http://vocab.nerc.ac.uk/collection/P02/current/" + urn + "/";
					}
				}

				CoverageDescription descr = new CoverageDescription();
				descr.setAttributeIdentifier(urn);
				if (parameterLabels.size() == parameterURNs.size()) {
					String label = parameterLabels.toArray(new String[] {})[i].trim();
					descr.setAttributeTitle(label);
					descr.setAttributeDescription(label);
				} else {
					NVSClient client = new NVSClient();
					String label = client.getLabel(urn);
					if (label != null) {
						descr.setAttributeTitle(label);
						descr.setAttributeDescription(label);
					}
				}
				coreMetadata.getMIMetadata().addCoverageDescription(descr);
			}

		}
		{
			Keywords keyword = new Keywords();
			for (String s : keywords) {
				keyword.addKeyword(s.trim());
			}
			if (keywordsVoc != null) {
				keyword.setThesaurusNameCitationTitle(keywordsVoc);
			}
			coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
		}

		if (project != null) {
			project = project.replace(";", ",");
			String[] split = project.split(",");
			Keywords keyword = new Keywords();
			keyword.setTypeCode("project");
			for (String s : split) {
				keyword.addKeyword(s.trim());
			}
			coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
		}

		Keywords keyword = new Keywords();
		keyword.setTypeCode("instrument");

		String[] sensorURNsList = sensorURNs.toArray(new String[] {});
		String[] sensorLabelsList = sensorLabels.toArray(new String[] {});
		for (int j = 0; j < sensorLabelsList.length; j++) {
			String sensorLabel = sensorLabelsList[j];
			String sensorURI = null;
			if (sensorURNsList.length == sensorLabelsList.length) {
				sensorURI = sensorURNsList[j];
			}
			if (sensorLabel == null && sensorURI != null && sensorURI.contains("vocab.nerc.ac.uk")) {
				NVSClient client = new NVSClient();
				sensorLabel = client.getLabel(sensorURI);
			}

			MIInstrument myInstrument = new MIInstrument();
			myInstrument.setMDIdentifierTypeCode(sensorURI);
			keyword.addKeyword(sensorLabel, sensorURI);
			myInstrument.setDescription(sensorLabel);
			myInstrument.setTitle(sensorLabel);

			coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

			coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		}

		// platform

		Keywords pk = new Keywords();
		pk.setTypeCode("platform");


		for (int i = 0; i < platformNameCode.size(); i++) {
			SimpleEntry<String, String> nameCode = platformNameCode.get(i);
			String platformName = nameCode.getKey();
			String platformCode = nameCode.getValue();
			MIPlatform platform = new MIPlatform();
			platform.setMDIdentifierCode(platformCode);
			Citation platformCitation = new Citation();
			platformCitation.setTitle(platformName);
			platform.setCitation(platformCitation);
			coreMetadata.getMIMetadata().addMIPlatform(platform);
			pk.addKeyword(platformName, platformCode);		    
		}

		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(pk);
		// bbox

		if ((west != null && !west.isEmpty()) && (east != null && !east.isEmpty())
				&& (north != null && !north.isEmpty()) && (south != null && !south.isEmpty())) {
			BigDecimal w = new BigDecimal(west);
			BigDecimal e = new BigDecimal(east);
			BigDecimal n = new BigDecimal(north);
			BigDecimal s = new BigDecimal(south);

			dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);
		}

		// datestamp
		if (timeStamp != null && !timeStamp.isEmpty()) {
			coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(timeStamp);
			coreMetadata.getMIMetadata().setDateStampAsDate(timeStamp);
		}

		// temporal extent
		TemporalExtent tempExtent = new TemporalExtent();

		if (beginPosition != null && !beginPosition.isEmpty()) {
			tempExtent.setBeginPosition(beginPosition);
		}
		if (endPosition != null && !endPosition.isEmpty()) {
			tempExtent.setEndPosition(endPosition);
		}

		coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

		for (Party party : parties) {
			ResponsibleParty partyContact = new ResponsibleParty();
			String name = party.getName();
			String uri = party.getUri();
			if (uri != null) {
				uri = uri.trim();
			}
			try {
				EDMOClient client = new EDMOClient();
				String semanticName = client.getLabelFromURI(uri);
				if (semanticName != null) {
					name = semanticName;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(uri, name);
			if (party.isOrganization()) {
				partyContact.getElementType().setOrganisationName(value);
			} else {
				partyContact.getElementType().setIndividualName(value);
			}
			if (party.getRole().equals("originator")) {
				dataset.getExtensionHandler().addOriginatorOrganisationIdentifier(party.getUri());
			}
			CIRoleCodePropertyType role = new CIRoleCodePropertyType();
			CodeListValueType clvt = new CodeListValueType();
			clvt.setCodeListValue(party.getRole());
			clvt.setValue(party.getRole());
			role.setCIRoleCode(clvt);
			partyContact.getElementType().setRole(role);

			Contact contact = new Contact();
			Address address = new Address();
			if (party.getEmail() != null) {
				address.addElectronicMailAddress(party.getEmail());
			}
			contact.setAddress(address);
			if (party.getUrl() != null) {
				Online online = new Online();
				online.setLinkage(party.getUrl());
				contact.setOnline(online);
			}
			partyContact.setContactInfo(contact);
			coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(partyContact);

		}

		DataQuality dq = new DataQuality();
		dq.setLineageStatement(lineage);

		coreMetadata.getMIMetadata().addDataQuality(dq);

		LegalConstraints lc = new LegalConstraints();

		lc.addUseConstraintsCode("license");
		lc.addOtherConstraints(license);
		coreMetadata.getDataIdentification().addLegalConstraints(lc);

		GSLoggerFactory.getLogger(getClass()).info("EMODNet Physics Mappper ENDED");

	}

	private List<Party> getParties(List<String> names, List<String> uris, List<String> emails, List<String> urls,
			List<String> types, String role) {
		List<Party> ret = new ArrayList<>();
		if (names.size() < uris.size()) {
			names.clear();
			for (String uri : uris) {
				EDMOClient client = new EDMOClient();
				String label = client.getLabelFromURI(uri.trim());
				names.add(label);
			}
		}
		boolean oneToOne = false;
		if (names.size() == uris.size()) {
			oneToOne = true;
		}
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			String uri = oneToOne ? uris.get(i) : null;
			Party party = new Party();
			party.setName(name);
			party.setUri(uri);
			party.setRole(role);
			if (names.size() == types.size()) {
				String pType = types.get(i);
				if (pType != null && pType.equals("person")) {
					party.setOrganization(false);
				}
			}
			if (names.size() == urls.size()) {
				String url = urls.get(i);
				party.setUrl(url);
			}
			if (names.size() == emails.size()) {
				String eMail = emails.get(i);
				party.setEmail(eMail);
			}
			ret.add(party);
		}

		return ret;
	}

	private void addSeparatedValues(Collection<String> collection, String value) {
		if (value == null || value.isEmpty()) {
			return;
		}
		value = value.trim();
		String[] values;
		if (value.contains(";")) {
			values = value.split(";");
		} else if (value.contains(",")) {
			values = value.split(",");
		} else {
			values = value.split(" ");
		}
		for (String v : values) {
			if (v != null) {
				v = v.trim();
				if (!v.isEmpty()) {
					collection.add(v);
				}
			}
		}
	}

}
