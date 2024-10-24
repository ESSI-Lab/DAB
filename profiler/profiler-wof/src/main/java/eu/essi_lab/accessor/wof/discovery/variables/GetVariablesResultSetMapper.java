package eu.essi_lab.accessor.wof.discovery.variables;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.TimeScale;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.accessor.wof.WOFQueryUtils;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * @author boldrini
 */
public class GetVariablesResultSetMapper extends DiscoveryResultSetMapper<VariableInfoType> {

    private static final String HIS_RES_SET_MAPPER_ERROR = "HIS_RES_SET_MAPPER_ERROR";

    public GetVariablesResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema HYDRO_SERVER_VARIABLES_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return HYDRO_SERVER_VARIABLES_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public VariableInfoType map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    // MDMetadata metadata = coreMetadata.getMDMetadata();
	    MIMetadata metadata = coreMetadata.getMIMetadata();

	    String varCode = "";
	    try {
		varCode = metadata.getCoverageDescription().getAttributeIdentifier();
	    } catch (Exception e) {
	    }
	    String varName = "";// Battery voltage
	    try {
		varName = metadata.getCoverageDescription().getAttributeTitle();
	    } catch (Exception e) {
	    }
	    if (varName == null || varName.equals("")) {
		varName = varCode;
	    }
	    String varDescription = "";
	    try {
		varDescription = metadata.getCoverageDescription().getAttributeDescription();
	    } catch (Exception e) {
	    }

	    // SEMANTIC_HARMONIZATION if attribute URI is present, is preferred, to have an harmonized set of attributes
	    Optional<String> optionalAttributeURI = res.getExtensionHandler().getObservedPropertyURI();
	    if (WOFQueryUtils.isSemanticHarmonizationEnabled(message.getWebRequest()) && optionalAttributeURI.isPresent()) {
		String uri = optionalAttributeURI.get();
		if (uri != null) {
		    HydroOntology ontology = new WHOSOntology();
		    SKOSConcept concept = ontology.getConcept(uri);
		    if (concept != null) {
			varName = concept.getPreferredLabel().getKey();
			HashSet<String> closeMatches = concept.getCloseMatches();
			if (closeMatches != null && !closeMatches.isEmpty()) {
			    try {
				WMOOntology wmoOntology = new WMOOntology();
				for (String closeMatch : closeMatches) {
				    SKOSConcept variable = wmoOntology.getVariable(closeMatch);
				    if (variable != null) {
					SimpleEntry<String, String> preferredLabel = variable.getPreferredLabel();
					if (preferredLabel != null) {
					    varName = preferredLabel.getKey();
					}
				    }
				}
			    } catch (Exception e) {
				e.printStackTrace();
			    }

			}
		    }
		}
	    }

	    String variableUnitName = "";
	    Optional<String> optionalVariableUnit = res.getExtensionHandler().getAttributeUnits();
	    if (optionalVariableUnit.isPresent()) {
		variableUnitName = optionalVariableUnit.get();
	    }
	    String variableUnitAbbreviation = "";
	    Optional<String> optionalVariableUnitAbbreviation = res.getExtensionHandler().getAttributeUnitsAbbreviation();
	    if (optionalVariableUnitAbbreviation.isPresent()) {
		variableUnitAbbreviation = optionalVariableUnitAbbreviation.get();
	    }
	    if (variableUnitAbbreviation.equals("")) {
		variableUnitAbbreviation = variableUnitName;
	    }

	    // SEMANTIC_HARMONIZATION if attribute URI is present, is preferred, to have an harmonized set of attribute
	    // units
	    Optional<String> optionalAttributeUnitsURI = res.getExtensionHandler().getAttributeUnitsURI();
	    if (WOFQueryUtils.isSemanticHarmonizationEnabled(message.getWebRequest()) && optionalAttributeUnitsURI.isPresent()) {
		String uri = optionalAttributeUnitsURI.get();
		if (uri != null) {

		    try {
			WMOOntology codes = new WMOOntology();
			WMOUnit unit = codes.getUnit(uri);
			if (unit != null) {
			    variableUnitName = unit.getPreferredLabel().getKey();
			    variableUnitAbbreviation = unit.getAbbreviation();
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }

	    String varUniqueCode = "";
	    try {
		varUniqueCode = res.getExtensionHandler().getUniqueAttributeIdentifier().get();
	    } catch (Exception e) {
	    }
	    String network = "";
	    try {
		network = res.getSource().getUniqueIdentifier();
	    } catch (Exception e) {
	    }

	    String dataType = "";// Minimum
	    Optional<InterpolationType> optionalTimeInterpolation = res.getExtensionHandler().getTimeInterpolation();
	    if (optionalTimeInterpolation.isPresent()) {
		InterpolationType timeInterpolation = optionalTimeInterpolation.get();
		switch (timeInterpolation) {
		case CONTINUOUS:
		    dataType = "continuous";
		    break;
		case DISCONTINUOUS:
		    dataType = "sporadic";
		    break;
		case TOTAL:
		case TOTAL_PREC:
		case TOTAL_SUCC:
		    dataType = "cumulative";
		    break;
		case INCREMENTAL:
		    dataType = "incremental";
		    break;
		case AVERAGE:
		case AVERAGE_PREC:
		case AVERAGE_SUCC:
		    dataType = "average";
		    break;
		case MAX:
		case MAX_PREC:
		case MAX_SUCC:
		    dataType = "maximum";
		    break;
		case MIN:
		case MIN_PREC:
		case MIN_SUCC:
		    dataType = "minimum";
		    break;
		case CONST:
		case CONST_PREC:
		case CONST_SUCC:
		    dataType = "constant over interval";
		    break;
		case CATEGORICAL:
		    dataType = "categorical";
		    break;
		default:
		    dataType = timeInterpolation.name();
		    break;
		}
	    } else {
		Optional<String> opt = res.getExtensionHandler().getTimeInterpolationString();
		if (opt.isPresent()) {
		    dataType = opt.get();
		}

	    }

	    Optional<String> aggregationPeriod = res.getExtensionHandler().getTimeAggregationDuration8601();
	    Optional<String> resolutionPeriod = res.getExtensionHandler().getTimeResolutionDuration8601();
	    String aggregationTimeUnits = null;
	    String resolutionTimeUnits = null;
	    BigDecimal aggregationPeriodValue = null;
	    BigDecimal resolutionPeriodValue = null;

	    if (aggregationPeriod.isPresent()) {
		try {
		    javax.xml.datatype.Duration duration = ISO8601DateTimeUtils.getDuration(aggregationPeriod.get());
		    SimpleEntry<BigDecimal, String> unitsValue = ISO8601DateTimeUtils.getUnitsValueFromDuration(duration);
		    aggregationTimeUnits = unitsValue.getValue();
		    aggregationPeriodValue = unitsValue.getKey();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	    if (resolutionPeriod.isPresent()) {
		try {
		    javax.xml.datatype.Duration duration = ISO8601DateTimeUtils.getDuration(resolutionPeriod.get());
		    SimpleEntry<BigDecimal, String> unitsValue = ISO8601DateTimeUtils.getUnitsValueFromDuration(duration);
		    resolutionTimeUnits = unitsValue.getValue();
		    resolutionPeriodValue = unitsValue.getKey();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	    if (aggregationTimeUnits != null && resolutionTimeUnits != null && !aggregationTimeUnits.equals(resolutionTimeUnits)) {
		GSLoggerFactory.getLogger(getClass()).error("Different time units in WOF profiler!");
	    }

	    String timeUnits = aggregationTimeUnits;
	    if (timeUnits == null) {
		timeUnits = resolutionTimeUnits;
	    }

	    boolean regularTime = false;
	    String timeSpacing = null;

	    if (resolutionPeriodValue != null) {
		timeSpacing = resolutionPeriodValue.toString();
		if (!timeSpacing.isEmpty() && !timeSpacing.equals("0")) {
		    regularTime = true;
		}
	    }

	    VariableInfoType ret = new VariableInfoType();
	    VariableCode variableCode = new VariableCode();
	    variableCode.setVocabulary(network);
	    variableCode.setDefault(true);
	    variableCode.setVariableID(1);
	    variableCode.setValue(varUniqueCode);
	    ret.getVariableCode().add(variableCode);
	    ret.setVariableName(varName);
	    ret.setVariableDescription(varDescription);
	    ret.setValueType("");
	    ret.setDataType(dataType);
	    ret.setGeneralCategory("");
	    ret.setSampleMedium("");
	    UnitsType units = new UnitsType();
	    units.setUnitName(variableUnitName);
	    units.setUnitAbbreviation(variableUnitAbbreviation);
	    units.setUnitType("");
	    units.setUnitCode("");
	    ret.setUnit(units);

	    Optional<String> optionalVariableMissingValue = res.getExtensionHandler().getAttributeMissingValue();
	    if (optionalVariableMissingValue.isPresent() && !optionalVariableMissingValue.get().isEmpty()) {
		ret.setNoDataValue(Double.parseDouble(optionalVariableMissingValue.get()));
	    }

	    TimeScale ts = new TimeScale();
	    ts.setIsRegular(regularTime);
	    UnitsType tu = new UnitsType();
	    tu.setUnitName(timeUnits);
	    if (timeUnits != null) {
		tu.setUnitAbbreviation(ISO8601DateTimeUtils.getTimeUnitsAbbreviation(timeUnits));
	    }
	    tu.setUnitType("");
	    tu.setUnitCode("");
	    ts.setUnit(tu);

	    // Optional<String> optionalTimeSupport = res.getExtensionHandler().getTimeSupport();
	    if (aggregationPeriodValue != null) {
		Float timeSupport = aggregationPeriodValue.floatValue();

		// this is to remove the sign to negative time supports, as WML 1.1 expects always positive time
		// supports.
		if (timeSupport < 0) {
		    timeSupport = -timeSupport;
		}
		ts.setTimeSupport(timeSupport);
	    }
	    if (timeSpacing != null) {
		try {
		    ts.setTimeSpacing(Float.parseFloat(timeSpacing));
		} catch (NumberFormatException ex) {
		    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
		}
	    }
	    JAXBElement<TimeScale> timeScale = JAXBWML.getInstance().getFactory().createVariableInfoTypeTimeScale(ts);

	    ret.setTimeScale(timeScale);
	    ret.setSpeciation("");
	    return ret;

	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_RES_SET_MAPPER_ERROR);
	}

    }
}
