/**
 * 
 */
package eu.essi_lab.accessor.egaskro;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Fabrizio
 */
public class EGASKROResourceMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String EGASKRO_SCHEME_URI = "http://www.feerc.ru/geoss/egaskro/scheme";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject object = new JSONObject(originalMD.getMetadata());
	if (object.getString("type").equals("dataset")) {

	    return createDataset(originalMD, source);
	}

	return createCollection(originalMD, source);
    }

    private Dataset createDataset(OriginalMetadata originalMD, GSSource source) {

	JSONObject object = new JSONObject(originalMD.getMetadata());

	String date = object.getString("date");
	String time = object.getString("time");
	String name = object.getString("name");
	String localIndex = object.getString("localIndex");
	String lon = object.getString("longitude");
	String lat = object.getString("latitude");
	String quantity = object.getString("quantity");
	String parentId = object.getString("parentId");
	String titlePrefix = object.getString("titlePrefix");

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	miMetadata.setParentIdentifier(parentId);

	DataIdentification info = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification();
	double north = Double.parseDouble(lat.replace(",", "."));
	double west = Double.parseDouble(lon.replace(",", "."));
	double south = Double.parseDouble(lat.replace(",", "."));
	double east = Double.parseDouble(lon.replace(",", "."));

	info.addGeographicBoundingBox(north, west, south, east);

	String[] split = date.split("\\.");
	String year = split[2];
	String month = split[1];
	String day = split[0];

	String isoDate = year + "-" + month + "-" + day;
	String isoDateTime = year + "-" + month + "-" + day + "T" + time + ":00Z";

	miMetadata.setDateStampAsDate(isoDate);

	ResponsibleParty contact = new ResponsibleParty();
	contact.setOrganisationName("Research Production Association \"Typhoon\"");
	contact.setIndividualName("Valery S. Kosykh, Ph.D");
	contact.setRoleCode("pointOfContact");

	Contact contactInfo = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress("vsk@feerc.ru");
	contactInfo.setAddress(address);
	contact.setContactInfo(contactInfo);

	miMetadata.addContact(contact);

	String parameter = quantity + " @ " + name;

	info.setCitationTitle(titlePrefix + "," + parameter);
	info.addTemporalExtent(isoDateTime, isoDateTime);

	info.addKeyword(localIndex);
	info.addKeyword(parameter);
	info.addKeyword("EGASKRO");
	info.addKeyword("Unified State Automated System for Monitoring Radiation Situation on the territory of the Russian Federation");
	info.addKeyword("RPA Typhoon");
	info.addKeyword("Roshydromet");
	info.addKeyword("radiation monitoring");

	info.addTopicCategory(MDTopicCategoryCodeType.ENVIRONMENT);

	info.setAbstract(
		"This data is part of the Unified State Automated System for Monitoring Radiation Situation on the territory of the Russian Federation EGASKRO System: Departmental "
			+ name + " Subsystem: Roshydromet Parameter: " + parameter + " Value: " + quantity + " Local index: " + localIndex);

	info.addLanguage("rus");
	info.setSupplementalInformation("Visit Egaskro homepage for additional information on egaskro");

	dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
		"EGASKRO Data Access Service", //
		"http://www.feerc.ru/geoss/egaskro", //
		"HTTP", //
		"order");

	return dataset;
    }

    private DatasetCollection createCollection(OriginalMetadata originalMD, GSSource source) {

	JSONObject object = new JSONObject(originalMD.getMetadata());

	DatasetCollection collection = new DatasetCollection();

	collection.setSource(source);

	collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setFileIdentifier(object.getString("id"));
	collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier(source.getUniqueIdentifier());

	DataIdentification info = collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification();
	info.setCitationTitle(object.getString("title"));

	collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setDateStampAsDate(ISO8601DateTimeUtils.getISO8601Date());

	ResponsibleParty contact = new ResponsibleParty();
	contact.setOrganisationName("Research Production Association \"Typhoon\"");
	contact.setIndividualName("Valery S. Kosykh, Ph.D");
	contact.setRoleCode("pointOfContact");

	Contact contactInfo = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress("vsk@feerc.ru");
	contactInfo.setAddress(address);
	contact.setContactInfo(contactInfo);

	collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addContact(contact);

	info.setAbstract(
		"This data is part of the Unified State Automated System for Monitoring Radiation Situation on the territory of the Russian Federation EGASKRO System: "
			+ object.getString("title"));

	info.setSupplementalInformation("Visit Egaskro homepage for additional information on egaskro");
	info.addLanguage("en");

	info.addTopicCategory(MDTopicCategoryCodeType.ENVIRONMENT);

	collection.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(//
		"EGASKRO Data Access Service", //
		"http://www.feerc.ru/geoss/egaskro", //
		"HTTP", //
		"order");

	return collection;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return EGASKRO_SCHEME_URI;
    }
}
