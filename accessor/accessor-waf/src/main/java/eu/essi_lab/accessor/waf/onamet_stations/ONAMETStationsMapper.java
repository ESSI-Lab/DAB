package eu.essi_lab.accessor.waf.onamet_stations;

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

import com.google.common.base.CaseFormat;

import eu.essi_lab.accessor.waf.onamet_stations.ONAMETParameter.ONAMETParameterId;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * @author Fabrizio
 */
public class ONAMETStationsMapper extends FileIdentifierMapper {

    /**
     * 
     */
    public static final String ONAMET_STATIONS_METADATA_SCHEMA = "ONAMET_STATIONS_METADATA_SCHEMA";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	ONAMETStation station = originalMD.getAdditionalInfo().get("station", ONAMETStation.class);
	ONAMETParameter parameter = originalMD.getAdditionalInfo().get("parameter", ONAMETParameter.class);

	//
	//
	//
	Dataset dataset = new Dataset();
	dataset.setSource(source);
	dataset.setOriginalMetadata(originalMD);

	//
	// title
	//
	String stationName = station.getName();
	String paraName = parameter.getName();
	String interpolation = parameter.getInterpolation();
	interpolation = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, interpolation);

	dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(stationName + " - " + paraName + " - " + interpolation);

	//
	// description
	//

	String location = station.getLocation();
	String elevation = station.getElevation();

	dataset.getHarmonizedMetadata().getCoreMetadata()
		.setAbstract("Station " + stationName + " located in " + location + " at an elevation of " + elevation + " meters");

	//
	// spatial extent
	//
	double lat = Double.valueOf(station.getLat());
	double lon = Double.valueOf(station.getLon());

	dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(lat, lon, lat, lon);

	//
	// elevation
	//

	VerticalExtent verticalExtent = new VerticalExtent();
	verticalExtent.setMaximumValue(Double.valueOf(elevation));
	verticalExtent.setMinimumValue(Double.valueOf(elevation));

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);

	//
	// contact
	//
	ResponsibleParty party = new ResponsibleParty();

	String country = station.getCountry();
	String institution = station.getInstitution();
	String phone = station.getPhone();
	String email = station.getEmail();

	party.setOrganisationName(institution);
	party.setRoleCode("author");

	Contact contact = new Contact();
	contact.addPhoneVoice(phone);

	Address address = new Address();
	address.setCountry(country);
	address.addElectronicMailAddress(email);

	contact.setAddress(address);
	party.setContactInfo(contact);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addContact(party);

	//
	// keywords
	//
	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addKeyword(interpolation);
	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addKeyword(parameter.getAggregation());
	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addKeyword(parameter.getInstrument());
	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addKeyword(parameter.getUnits());
	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addKeyword(parameter.getName());

	//
	// instrument
	//

	MIInstrument instrument = new MIInstrument();
	instrument.setTitle(parameter.getName());
	instrument.setSensorType(parameter.getInstrument());
	instrument.setDescription("Units: " + parameter.getUnits() + " Valid range: " + parameter.getValidRange());

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addMIInstrument(instrument);

	//
	// platform
	//
	
	MIPlatform miPlatform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(stationName);
	miPlatform.setCitation(citation);
	
	miPlatform.setMDIdentifierCode(station.getId());
	
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addMIPlatform(miPlatform);
	
	//
	// coverage description
	//
	CoverageDescription coverageDescription = new CoverageDescription();

	String attributePrefix = "onamet.gov.do.";
	coverageDescription.setAttributeIdentifier(attributePrefix + parameter.getId());
	coverageDescription.setAttributeTitle(parameter.getName());

	InterpolationType interp = getInterpolationType(parameter.getInterpolation());
	dataset.getExtensionHandler().setTimeInterpolation(interp);

	// Number timeSupport = getTimeScaleTimeSupport(series);
	// if (timeSupport != null && !timeSupport.toString().equals("0")) {
	// dataset.getExtensionHandler().setTimeSupport(timeSupport.toString());
	// }

	// String timeUnits = getTimeScaleUnitName(series);
	// dataset.getExtensionHandler().setTimeUnits(timeUnits);

	// String timeUnitsAbbreviation = getTimeScaleUnitAbbreviation(series);
	// dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnitsAbbreviation);

	// String missingValue = series.getNoDataValue();
	// dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	String unitName = parameter.getUnits();
	dataset.getExtensionHandler().setAttributeUnits(unitName);

	// String unitAbbreviation = getUnitAbbreviation(series);
	// dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

	String attributeDescription = parameter.getName() + //
		" Units: " + parameter.getUnits() + //
		" Instrument: " + parameter.getInstrument() + //
		" Valid range: " + parameter.getValidRange();

	coverageDescription.setAttributeDescription(attributeDescription);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addCoverageDescription(coverageDescription);

	return dataset;
    }

    /**
     * @param interpolation
     * @return
     */
    private InterpolationType getInterpolationType(String interpolation) {
	switch (interpolation.toLowerCase()) {
	case "instantanea":
	    return InterpolationType.CONTINUOUS;
	case "acumulado":
	    return InterpolationType.INCREMENTAL;
	}
	return null;
    }

    /**
     * @param station
     * @param parameter
     * @return
     */
    static OriginalMetadata create(ONAMETStation station, ONAMETParameter parameter) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(ONAMET_STATIONS_METADATA_SCHEMA);

	JSONObject object = new JSONObject();
	object.put("stationName", station.getName());
	object.put("parameterName", parameter.getName());
	object.put("stationId", station.getId());
	object.put("parameterId", parameter.getId());

	originalMetadata.setAdditionalInfo(GSPropertyHandler.of(//
		new GSProperty<ONAMETStation>("station", station), //
		new GSProperty<ONAMETParameter>("parameter", parameter)//
	));

	originalMetadata.setMetadata(object.toString());

	return originalMetadata;
    }

    /**
     * @param originalMetadata
     * @return
     */
    static String readStationName(OriginalMetadata originalMetadata) {

	return new JSONObject(originalMetadata.getMetadata()).getString("stationName");
    }

    /**
     * @param originalMetadata
     * @return
     */
    static String readParameterName(OriginalMetadata originalMetadata) {

	return new JSONObject(originalMetadata.getMetadata()).getString("parameterName");
    }

    /**
     * @param originalMetadata
     * @return
     */
    static String readStationId(OriginalMetadata originalMetadata) {

	return new JSONObject(originalMetadata.getMetadata()).getString("stationId");
    }

    /**
     * @param originalMetadata
     * @return
     */
    static ONAMETParameterId readParameterId(OriginalMetadata originalMetadata) {

	try {

	    return ONAMETParameterId.valueOf(new JSONObject(originalMetadata.getMetadata()).getString("parameterId"));
	} catch (Exception ex) {

	    ex.printStackTrace();
	}
	
	return null;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ONAMET_STATIONS_METADATA_SCHEMA;
    }
}
