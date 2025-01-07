package eu.essi_lab.accessor.wod;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.Dataset;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.DSAssociationTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.DSInitiativeTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDAggregateInformationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDAggregateInformationType;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class WODMetadataCreator {

    private File tmpFile;
    private String url;

    public WODMetadataCreator(File tmpFile, String url) {
	this.tmpFile = tmpFile;
	this.url = url;
    }

    HashMap<String, String> globals = new HashMap<String, String>();

    public MIMetadata mapMetadata() throws IOException {
	NetcdfDataset dataset = null;

	try {
	    String path = tmpFile.getAbsolutePath();
	    String fileName = tmpFile.getName();
	    String[] fileSplit = fileName.substring(0, fileName.indexOf(".")).split("_");
	    String type = fileSplit[1];
	    String year = fileSplit[2];
	    dataset = NetcdfDataset.openDataset(path);
	    List<Attribute> gas = dataset.getGlobalAttributes();

	    globals = new HashMap<String, String>();
	    readGlobals = new HashSet<String>();

	    for (Attribute ga : gas) {
		globals.put(ga.getShortName(), ga.getValue(0).toString());
	    }
	    List<Variable> variables = dataset.getVariables();
	    List<Variable> mainVariables = new ArrayList<Variable>();
	    HashSet<String> ancillaryVariables = new HashSet<String>();
	    for (Variable variable : variables) {
		Attribute av = variable.findAttribute("ancillary_variables");
		if (av != null) {
		    String ancillaryVariableString = av.getStringValue();
		    String[] split = ancillaryVariableString.split(" ");
		    for (String s : split) {
			ancillaryVariables.add(s);
		    }
		}
	    }
	    Variable platformVariable = null;
	    Variable instituteVariable = null;
	    Variable projectVariable = null;
	    List<Variable> instrumentVariable = new ArrayList<Variable>();
	    Variable countryVariable = null;
	    for (Variable variable : variables) {
		String name = variable.getShortName();
		String longName = "";
		Attribute longAttribute = variable.findAttribute("long_name");
		if (longAttribute != null) {
		    longName = longAttribute.getStringValue();
		}
		if (name.equals("lat")) {
		} else if (name.equals("lon")) {
		} else if (name.equals("latitude")) {
		} else if (name.equals("longitude")) {
		} else if (name.equals("Latitude")) {
		} else if (name.equals("Longitude")) {
		} else if (name.equals("Primary_Investigator")) { // TODO use it?
		} else if (name.equals("Primary_Investigator_VAR")) { // TODO use it?
		} else if (name.equals("ARGOS_ID")) {
		} else if (name.equals("WMO_ID")) {
		} else if (name.equals("WOD_code")) {
		} else if (longName.equals("WOD_code")) {
		} else if (name.equals("time")) {
		} else if (name.equals("Project")) {
		    projectVariable = variable;
		} else if (name.equals("date")) {
		} else if (longName.equals("real_time_data")) {
		} else if (name.equals("Recorder")) {
		} else if (name.equals("dbase_orig")) {
		} else if (name.equals("WOD_cruise_identifier")) {
		} else if (name.equals("originators_cruise_identifier")) {
		} else if (name.equals("wod_unique_cast")) {
		} else if (name.endsWith("_row_size")) {
		} else if (name.equals("GMT_time")) {
		} else if (name.equals("Access_no")) {
		} else if (name.equals("needs_z_fix")) {
		} else if (name.endsWith("_Access_no")) {
		} else if (name.endsWith("_units")) {
		} else if (name.endsWith("JulianDay")) {
		} else if (name.equals("High_res_pair")) {
		} else if (name.equals("Platform")) {
		    platformVariable = variable;
		} else if (name.equals("Institute")) {
		    instituteVariable = variable;
		} else if (name.endsWith("_Instrument")) {
		    instrumentVariable.add(variable);
		} else if (name.equals("country")) {
		    countryVariable = variable;
		} else if (name.equals("Orig_Stat_Num")) {
		} else if (name.equals("Bottom_Depth")) {
		} else if (name.equals("dataset")) {
		} else if (name.equals("Ocean_Vehicle")) {
		} else if (name.equals("origflagset")) {
		} else if (name.equals("crs")) {
		} else if (ancillaryVariables.contains(name)) {
		} else {
		    mainVariables.add(variable);
		}
	    }

	    Dataset ds = new Dataset();

	    MIMetadata metadata = ds.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    String typeDescription;

	    switch (type) {
	    case "osd":
		typeDescription = "Bottle, low-resolution Conductivity-Temperature-Depth (CTD), low-resolution XCTD data, and plankton data";
		break;
	    case "ctd":
		typeDescription = "High-resolution Conductivity-Temperature-Depth (CTD) data and high-resolution XCTD data";
		break;
	    case "mbt":
		typeDescription = "Mechanical Bathythermograph (MBT) data, Digital BT (DBT), micro-BT (µBT)";
		break;
	    case "xbt":
		typeDescription = "Expendable (XBT) data";
		break;
	    case "sur":
		typeDescription = "Surface only data (bucket, thermosalinograph)";
		break;
	    case "apb":
		typeDescription = "Autonomous Pinniped Bathythermograph - Time-Temperature-Depth recorders and CTDs attached to elephant seals";
		break;
	    case "mrb":
		typeDescription = "Moored buoy data mainly from the Equatorial buoy arrays -TAO";
		break;
	    case "pfl":
		typeDescription = "Profiling float data, mainly from the Argo program";
		break;
	    case "drb":
		typeDescription = "Drifting buoy data from surface drifting buoys with thermistor chains and from icetethered profilers";
		break;
	    case "uor":
		typeDescription = "Undulating Oceanographic Recorder data from a Conductivity/Temperature/Depth probe mounted on a towed undulating vehicle";
		break;
	    case "gld":
		typeDescription = "Glider data";
		break;
	    default:
		typeDescription = type;
		break;
	    }

	    metadata.getDataIdentification().setCitationTitle(readGlobal("title") + " - " + typeDescription + " - year " + year);

	    for (Variable variable : mainVariables) {
		String variableName = variable.getShortName();
		Attribute nameAttribute = variable.findAttribute("long_name");
		String name = variableName;
		if (nameAttribute != null) {
		    name = nameAttribute.getStringValue();
		}
		CoverageDescription description = new CoverageDescription();
		// description.setAttributeIdentifier("WOD:" + name);
		description.setAttributeDescription(name);
		description.setAttributeTitle(name);
		metadata.addCoverageDescription(description);

		System.out.println("Parameter: " + name + " (" + variableName + ")");

		// Attribute units = variable.findAttribute("units");
		// if (units != null) {
		// System.out.println("Units: " + units.getStringValue());
		// }
		// System.out.println();
	    }

	    ResponsibleParty pointOfContact = new ResponsibleParty();
	    pointOfContact.setOrganisationName(readGlobal("institution"));
	    metadata.getDataIdentification().addPointOfContact(pointOfContact);

	    MDAggregateInformationPropertyType aggregationType = new MDAggregateInformationPropertyType();
	    MDAggregateInformationType aggregation = new MDAggregateInformationType();
	    CICitationPropertyType aggCitation = new CICitationPropertyType();
	    CICitationType aCitation = new CICitationType();
	    aCitation.setOtherCitationDetails(MIMetadata.createCharacterStringPropertyType(readGlobal("references")));
	    aCitation.setTitle(MIMetadata.createCharacterStringPropertyType(readGlobal("source")));
	    aggCitation.setCICitation(aCitation);
	    aggregation.setAggregateDataSetName(aggCitation);
	    DSAssociationTypeCodePropertyType atpt = new DSAssociationTypeCodePropertyType();
	    atpt.setDSAssociationTypeCode(MIMetadata.createCodeListValueType(ISOMetadata.DS_ASSOCIATION_TYPE_CODE_CODELIST, "source",
		    ISOMetadata.ISO_19115_CODESPACE, "source"));
	    aggregation.setAssociationType(atpt);
	    DSInitiativeTypeCodePropertyType itcpt = new DSInitiativeTypeCodePropertyType();
	    itcpt.setDSInitiativeTypeCode(MIMetadata.createCodeListValueType(ISOMetadata.DS_INITIATIVE_TYPE_CODE_CODELIST, "collection",
		    ISOMetadata.ISO_19115_CODESPACE, "collection"));
	    aggregation.setInitiativeType(itcpt);
	    aggregationType.setMDAggregateInformation(aggregation);

	    metadata.getElementType().getIdentificationInfo().get(0).getAbstractMDIdentification().getValue().getAggregationInfo()
		    .add(aggregationType);

	    addProject(metadata, readGlobal("project"));

	    metadata.getDataIdentification().setAbstract(readGlobal("summary") + " - year " + year + " - " + typeDescription);

	    metadata.setFileIdentifier(readGlobal("id"));

	    metadata.getDataIdentification().setResourceIdentifier(readGlobal("id"), readGlobal("naming_authority"));

	    String geospatial_lat_min = readGlobal("geospatial_lat_min");
	    String geospatial_lat_max = readGlobal("geospatial_lat_max");
	    String geospatial_lon_min = readGlobal("geospatial_lon_min");
	    String geospatial_lon_max = readGlobal("geospatial_lon_max");

	    if (geospatial_lat_min != null && geospatial_lat_max != null && geospatial_lon_min != null && geospatial_lon_max != null) {
		double north = Double.parseDouble(geospatial_lat_max);
		double west = Double.parseDouble(geospatial_lon_min);
		double south = Double.parseDouble(geospatial_lat_min);
		double east = Double.parseDouble(geospatial_lon_max);
		metadata.getDataIdentification().addGeographicBoundingBox(north, west, south, east);
	    }

	    String time_coverage_start = readGlobal("time_coverage_start");
	    String time_coverage_end = readGlobal("time_coverage_end");

	    if (time_coverage_start != null && time_coverage_end != null) {
		time_coverage_start = correctDate(time_coverage_start);
		time_coverage_end = correctDate(time_coverage_end);

		Optional<Date> optionalT1 = ISO8601DateTimeUtils.parseISO8601ToDate(time_coverage_start);
		Optional<Date> optionalT2 = ISO8601DateTimeUtils.parseISO8601ToDate(time_coverage_end);
		if (optionalT1.isPresent() && optionalT2.isPresent()) {
		    metadata.getDataIdentification().addTemporalExtent(time_coverage_start, time_coverage_end);
		}
	    }

	    String geospatial_vertical_min = readGlobal("geospatial_vertical_min");
	    String geospatial_vertical_max = readGlobal("geospatial_vertical_max");

	    if (geospatial_vertical_min != null && geospatial_vertical_max != null) {

		double minD = Double.parseDouble(geospatial_vertical_min);
		double maxD = Double.parseDouble(geospatial_vertical_max);
		metadata.getDataIdentification().addVerticalExtent(minD, maxD);
	    }

	    ResponsibleParty creator = new ResponsibleParty();
	    creator.setOrganisationName(readGlobal("creator_name"));
	    Contact creatorInfo = new Contact();
	    Address creatorAddress = new Address();
	    creatorAddress.addElectronicMailAddress(readGlobal("creator_email"));
	    creatorInfo.setAddress(creatorAddress);
	    Online creatorOnline = new Online();
	    creatorOnline.setLinkage(readGlobal("creator_url"));
	    creatorInfo.setOnline(creatorOnline);
	    creator.setContactInfo(creatorInfo);
	    creator.setRoleCode("author");

	    metadata.getDataIdentification().addCitationResponsibleParty(creator);

	    HashSet<String> projects = getUniqueValues(projectVariable);
	    // printStrings("Platforms", platforms);

	    for (String project : projects) {
		addProject(metadata, project);
	    }

	    HashSet<String> platforms = getUniqueValues(platformVariable);
	    // printStrings("Platforms", platforms);

	    for (String plat : platforms) {
		MIPlatform platform = new MIPlatform();
		platform.setMDIdentifierCode("WOD:" + plat);
		platform.setDescription(plat);
		Citation platformCitation = new Citation();
		platformCitation.setTitle(plat);
		platform.setCitation(platformCitation);
		metadata.addMIPlatform(platform);
		Keywords keyword = new Keywords();
		keyword.setTypeCode("platform");
		keyword.addKeyword(plat);
		metadata.getDataIdentification().addKeywords(keyword);
	    }

	    HashSet<String> institutes = getUniqueValues(instituteVariable);
	    // printStrings("Institutes", institutes);

	    for (String inst : institutes) {
		ResponsibleParty creator2 = new ResponsibleParty();
		creator2.setOrganisationName(inst);
		creator2.setRoleCode("author");
		metadata.getDataIdentification().addCitationResponsibleParty(creator2);

	    }

	    for (Variable instrumentV : instrumentVariable) {
		HashSet<String> instruments = getUniqueValues(instrumentV);
		// printStrings("Institutes", institutes);

		for (String inst : instruments) {
		    MIInstrument myInstrument = new MIInstrument();
		    myInstrument.setMDIdentifierTypeCode("WOD:" + inst);
		    myInstrument.setDescription(inst);
		    myInstrument.setTitle(inst);
		    metadata.addMIInstrument(myInstrument);
		    Keywords keyword = new Keywords();
		    keyword.setTypeCode("instrument");
		    keyword.addKeyword(inst);
		    metadata.getDataIdentification().addKeywords(keyword);

		}
	    }

	    HashSet<String> countries = getUniqueValues(countryVariable);
	    // printStrings("Countries", countries);
	    for (String country : countries) {
		metadata.getDataIdentification().addKeyword(country);
	    }

	    metadata.getDataIdentification().setSupplementalInformation(readGlobal("acknowledgements"));

	    String keys = readGlobal("keywords");
	    if (keys != null) {
		String sep = ";";
		if (!keys.contains(";")) {
		    sep = " ";
		}
		String[] split = keys.split(sep);
		for (String key : split) {
		    if (key != null && !key.isEmpty()) {
			metadata.getDataIdentification().addKeyword(key);
			Keywords keyword = new Keywords();
			keyword.addKeyword(key);
			metadata.getDataIdentification().addKeywords(keyword);
		    }
		}

	    }

	    metadata.setDateStampAsDate(readGlobal("date_modified"));

	    metadata.getDataIdentification().setCitationCreationDate(readGlobal("date_created"));

	    ResponsibleParty publisher = new ResponsibleParty();
	    publisher.setOrganisationName(readGlobal("publisher_name"));
	    Contact publisherInfo = new Contact();
	    Address publisherAddress = new Address();
	    publisherAddress.addElectronicMailAddress(readGlobal("publisher_email"));
	    publisherInfo.setAddress(publisherAddress);
	    Online publisherOnline = new Online();
	    publisherOnline.setLinkage(readGlobal("publisher_url"));
	    publisherInfo.setOnline(publisherOnline);
	    publisher.setContactInfo(publisherInfo);
	    publisher.setRoleCode("publisher");

	    metadata.getDataIdentification().addCitationResponsibleParty(publisher);

	    HashSet<String> unReadGlobals = new HashSet<String>(globals.keySet());
	    unReadGlobals.removeAll(readGlobals);
	    unReadGlobals.remove("Conventions");
	    unReadGlobals.remove("_CoordSysBuilder");
	    unReadGlobals.remove("geospatial_lat_resolution");
	    unReadGlobals.remove("standard_name_vocabulary");
	    unReadGlobals.remove("cdm_data_type");
	    unReadGlobals.remove("featureType");
	    unReadGlobals.remove("geospatial_vertical_positive");
	    unReadGlobals.remove("geospatial_vertical_units");
	    unReadGlobals.remove("geospatial_lon_resolution");
	    Predicate<? super String> filter = new Predicate<String>() {

		@Override
		public boolean test(String t) {
		    if (t == null || t.isEmpty()) {
			return true;
		    }
		    String value = globals.get(t);
		    if (value == null || value.isEmpty()) {
			return true;
		    }
		    return false;
		}
	    };
	    unReadGlobals.removeIf(filter);
	    if (!unReadGlobals.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info(("Missing globals"));
		for (String unread : unReadGlobals) {
		    GSLoggerFactory.getLogger(getClass()).info(unread);
		}
	    }

	    Online onLine = new Online();
	    onLine.setLinkage(url);
	    onLine.setProtocol("HTTP");
	    onLine.setFunctionCode("download");
	    metadata.getDistribution().addDistributionOnline(onLine);
	    return metadata;
	} catch (Exception e) {
	    e.printStackTrace();
	    throw e;
	} finally {
	    dataset.close();
	}

    }

    private String correctDate(String time_coverage_start) {
	if (time_coverage_start.endsWith("-01-00")) {
	    time_coverage_start = time_coverage_start.replace("-01-00", "-01-01");
	}
	return time_coverage_start;
    }

    private void addProject(MIMetadata metadata, String project) {
	MDAggregateInformationPropertyType aggregationType2 = new MDAggregateInformationPropertyType();
	MDAggregateInformationType aggregation2 = new MDAggregateInformationType();
	CICitationPropertyType aggCitation2 = new CICitationPropertyType();
	CICitationType aCitation2 = new CICitationType();
	aCitation2.setTitle(MIMetadata.createCharacterStringPropertyType(project));
	aggCitation2.setCICitation(aCitation2);
	aggregation2.setAggregateDataSetName(aggCitation2);
	DSAssociationTypeCodePropertyType atpt2 = new DSAssociationTypeCodePropertyType();
	atpt2.setDSAssociationTypeCode(MIMetadata.createCodeListValueType(ISOMetadata.DS_ASSOCIATION_TYPE_CODE_CODELIST, "source",
		ISOMetadata.ISO_19115_CODESPACE, "source"));
	aggregation2.setAssociationType(atpt2);
	DSInitiativeTypeCodePropertyType itcpt2 = new DSInitiativeTypeCodePropertyType();
	itcpt2.setDSInitiativeTypeCode(MIMetadata.createCodeListValueType(ISOMetadata.DS_INITIATIVE_TYPE_CODE_CODELIST, "project",
		ISOMetadata.ISO_19115_CODESPACE, "project"));
	aggregation2.setInitiativeType(itcpt2);
	aggregationType2.setMDAggregateInformation(aggregation2);

	metadata.getElementType().getIdentificationInfo().get(0).getAbstractMDIdentification().getValue().getAggregationInfo()
		.add(aggregationType2);

    }

    private HashSet<String> readGlobals = new HashSet<String>();

    private String readGlobal(String key) {
	String ret = globals.get(key);
	readGlobals.add(key);
	return ret;
    }

    private static void printStrings(String title, HashSet<String> strings) {
	System.out.println(title);
	for (String string : strings) {
	    System.out.println(string);
	}
	System.out.println();

    }

    private static HashSet<String> getUniqueValues(Variable var) throws IOException {
	HashSet<String> ret = new HashSet<String>();
	if (var != null) {
	    Array array = var.read();
	    String total = "";
	    int len = var.getShape()[1];
	    int tmp = 0;
	    for (int i = 0; i < array.getSize(); i++) {
		char newChar = array.getChar(i);
		if (++tmp == len) {
		    ret.add(total.trim());
		    total = "";
		    tmp = 0;
		} else {
		    total += newChar;
		}
	    }
	}
	return ret;
    }

}
