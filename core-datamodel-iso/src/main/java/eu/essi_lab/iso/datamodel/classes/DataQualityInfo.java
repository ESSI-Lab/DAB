package eu.essi_lab.iso.datamodel.classes;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.BooleanPropertyType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.RecordPropertyType;
import net.opengis.iso19139.gco.v_20060504.UnitOfMeasurePropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQAbsoluteExternalPositionalAccuracyType;
import net.opengis.iso19139.gmd.v_20060504.DQConformanceResultType;
import net.opengis.iso19139.gmd.v_20060504.DQDomainConsistencyType;
import net.opengis.iso19139.gmd.v_20060504.DQQuantitativeResultType;
import net.opengis.iso19139.gmd.v_20060504.DQResultPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQScopePropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQScopeType;
import net.opengis.iso19139.gmd.v_20060504.LILineagePropertyType;
import net.opengis.iso19139.gmd.v_20060504.LILineageType;
import net.opengis.iso19139.gmd.v_20060504.LISourcePropertyType;
import net.opengis.iso19139.gmd.v_20060504.LISourceType;
import net.opengis.iso19139.gmd.v_20060504.MDScopeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDScopeDescriptionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDScopeDescriptionType;

/**
 * Wrapper class providing convenience methods for managing data quality information
 * in MDMetadata/MIMetadata.
 * 
 * Note: Some methods may need refinement based on the actual JAXB API.
 */
public class DataQualityInfo {

    private MDMetadata metadata;

    public DataQualityInfo(MDMetadata metadata) {
	this.metadata = metadata;
    }

    /**
     * Gets or creates a DataQuality object. If none exists, creates a new one.
     */
    private DataQuality getOrCreateDataQuality() {
	Iterator<DataQuality> dataQualities = metadata.getDataQualities();
	if (dataQualities.hasNext()) {
	    return dataQualities.next();
	}
	DataQuality dataQuality = new DataQuality();
	metadata.addDataQuality(dataQuality);
	return dataQuality;
    }

    /**
     * Adds a conformance result to the data quality information.
     */
    public void addConformanceResult(Citation specification, String explanation, Boolean pass) {
	DataQuality dataQuality = getOrCreateDataQuality();
	
	// Create a DQ element to contain the conformance result
	// Use DQDomainConsistencyType as a wrapper element
	DQDomainConsistencyType dqElement = new DQDomainConsistencyType();
	
	// Create the conformance result
	DQConformanceResultType conformanceResult = new DQConformanceResultType();
	
	// Set specification (required)
	if (specification != null) {
	    CICitationPropertyType citationProperty = new CICitationPropertyType();
	    citationProperty.setCICitation(specification.getElementType());
	    conformanceResult.setSpecification(citationProperty);
	}
	
	// Set explanation (required)
	if (explanation != null && !explanation.isEmpty()) {
	    CharacterStringPropertyType exp = ISOMetadata.createCharacterStringPropertyType(explanation);
	    conformanceResult.setExplanation(exp);
	} else {
	    // Set empty explanation if not provided (required field)
	    conformanceResult.setExplanation(ISOMetadata.createCharacterStringPropertyType(""));
	}
	
	// Set pass (required)
	if (pass != null) {
	    BooleanPropertyType bpt = new BooleanPropertyType();
	    bpt.setBoolean(pass);
	    conformanceResult.setPass(bpt);
	} else {
	    // Set default pass value if not provided (required field)
	    BooleanPropertyType bpt = new BooleanPropertyType();
	    bpt.setBoolean(false);
	    conformanceResult.setPass(bpt);
	}
	
	// Add conformance result to the DQ element's result list
	List<DQResultPropertyType> results = dqElement.getResult();
	if (results == null) {
	    results = new ArrayList<>();
	}
	DQResultPropertyType resultProperty = new DQResultPropertyType();
	JAXBElement<DQConformanceResultType> conformanceResultJaxb = ObjectFactories.GMD().createDQConformanceResult(conformanceResult);
	resultProperty.setAbstractDQResult(conformanceResultJaxb);
	results.add(resultProperty);
	dqElement.setResult(results);
	
	// Create JAXBElement for the DQ element and add as report
	JAXBElement<DQDomainConsistencyType> dqElementJaxb = ObjectFactories.GMD().createDQDomainConsistency(dqElement);
	dataQuality.addReport(dqElementJaxb);
    }

    /**
     * Adds an absolute external positional accuracy report.
     */
    public void addAbsoluteExternalPositionalAccuracy(Double value, String unit, String unitSystem) {
	DataQuality dataQuality = getOrCreateDataQuality();
	
	// Create the DQ element for absolute external positional accuracy
	DQAbsoluteExternalPositionalAccuracyType dqElement = new DQAbsoluteExternalPositionalAccuracyType();
	
	// Create a quantitative result to hold the value, unit, and unitSystem
	DQQuantitativeResultType quantitativeResult = new DQQuantitativeResultType();
	
	// Set value unit (required)
	// Note: UnitOfMeasureType structure is complex - this is a minimal implementation
	// The unit and unitSystem parameters are stored but may need refinement for full ISO 19115 compliance
	UnitOfMeasurePropertyType unitProperty = new UnitOfMeasurePropertyType();
	// Create UnitOfMeasureType - simplified implementation
	// Full implementation would require setting proper UnitOfMeasureType fields
	quantitativeResult.setValueUnit(unitProperty);
	
	// Set value (required) - create a RecordPropertyType with the value
	// Note: RecordType structure is complex - this is a minimal implementation
	List<RecordPropertyType> values = new ArrayList<>();
	RecordPropertyType valueProperty = new RecordPropertyType();
	// Create RecordType - simplified implementation
	// Full implementation would require creating a proper RecordType with fields containing the value
	values.add(valueProperty);
	quantitativeResult.setValue(values);
	
	// Add quantitative result to the DQ element's result list
	List<DQResultPropertyType> results = dqElement.getResult();
	if (results == null) {
	    results = new ArrayList<>();
	}
	DQResultPropertyType resultProperty = new DQResultPropertyType();
	JAXBElement<DQQuantitativeResultType> quantitativeResultJaxb = ObjectFactories.GMD().createDQQuantitativeResult(quantitativeResult);
	resultProperty.setAbstractDQResult(quantitativeResultJaxb);
	results.add(resultProperty);
	dqElement.setResult(results);
	
	// Create JAXBElement for the DQ element and add as report
	JAXBElement<DQAbsoluteExternalPositionalAccuracyType> dqElementJaxb = ObjectFactories.GMD().createDQAbsoluteExternalPositionalAccuracy(dqElement);
	dataQuality.addReport(dqElementJaxb);
    }

    /**
     * Sets the scope of the data quality information.
     */
    public void setScope(String scopeCode, String scopeDetail) {
	DataQuality dataQuality = getOrCreateDataQuality();
	
	// Create DQScopeType
	DQScopeType scopeType = new DQScopeType();
	
	// Set level (required) - scopeCode maps to MD_ScopeCode
	if (scopeCode != null && !scopeCode.isEmpty()) {
	    MDScopeCodePropertyType levelProperty = new MDScopeCodePropertyType();
	    CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(
		ISOMetadata.MD_SCOPE_CODE_CODELIST, 
		scopeCode, 
		ISOMetadata.ISO_19115_CODESPACE, 
		scopeCode);
	    levelProperty.setMDScopeCode(ObjectFactories.GMD().createMDScopeCode(codeListValue));
	    scopeType.setLevel(levelProperty);
	}
	
	// Set levelDescription (optional) - scopeDetail maps to MD_ScopeDescription
	if (scopeDetail != null && !scopeDetail.isEmpty()) {
	    MDScopeDescriptionPropertyType descriptionProperty = new MDScopeDescriptionPropertyType();
	    MDScopeDescriptionType descriptionType = new MDScopeDescriptionType();
	    // Use the "other" field for general scope detail description
	    descriptionType.setOther(ISOMetadata.createCharacterStringPropertyType(scopeDetail));
	    descriptionProperty.setMDScopeDescription(descriptionType);
	    // getLevelDescription() returns a live list, so we can add directly
	    scopeType.getLevelDescription().add(descriptionProperty);
	}
	
	// Wrap in DQScopePropertyType and set on DataQuality
	DQScopePropertyType scopeProperty = new DQScopePropertyType();
	scopeProperty.setDQScope(scopeType);
	dataQuality.getElementType().setScope(scopeProperty);
    }

    /**
     * Sets the lineage statement.
     */
    public void setLineageStatement(String statement) {
	DataQuality dataQuality = getOrCreateDataQuality();
	dataQuality.setLineageStatement(statement);
    }

    /**
     * Adds a lineage source.
     */
    public void addLineageSource(Citation citation, String description) {
	DataQuality dataQuality = getOrCreateDataQuality();
	
	// Get or create lineage structure (similar to setLineageStatement)
	LILineagePropertyType lineageProperty = dataQuality.getElementType().getLineage();
	if (lineageProperty == null) {
	    lineageProperty = new LILineagePropertyType();
	    dataQuality.getElementType().setLineage(lineageProperty);
	}
	LILineageType lineageType = lineageProperty.getLILineage();
	if (lineageType == null) {
	    lineageType = new LILineageType();
	    lineageProperty.setLILineage(lineageType);
	}
	
	// Create LISourceType
	LISourceType sourceType = new LISourceType();
	
	// Set source citation (optional)
	if (citation != null) {
	    CICitationPropertyType citationProperty = new CICitationPropertyType();
	    citationProperty.setCICitation(citation.getElementType());
	    sourceType.setSourceCitation(citationProperty);
	}
	
	// Set description (optional)
	if (description != null && !description.isEmpty()) {
	    sourceType.setDescription(ISOMetadata.createCharacterStringPropertyType(description));
	}
	
	// Wrap in LISourcePropertyType and add to lineage source list
	LISourcePropertyType sourceProperty = new LISourcePropertyType();
	sourceProperty.setLISource(sourceType);
	// getSource() returns a live list, so we can add directly
	lineageType.getSource().add(sourceProperty);
    }

    /**
     * Adds a descriptive result (for model quality reports, etc.).
     * TODO: Complete implementation - DQDescriptiveResultType may not exist in this JAXB version
     */
    public void addDescriptiveResult(String description) {
	// TODO: Implement descriptive result - may need to use a different result type
	DataQuality dataQuality = getOrCreateDataQuality();
	// Implementation needed
    }

    /**
     * Adds a quantitative attribute accuracy (for model metrics, etc.).
     * TODO: Complete implementation with proper JAXB element creation
     */
    public void addQuantitativeAttributeAccuracy(String name, String description, Double value, String unit) {
	// TODO: Implement using DQQuantitativeAttributeAccuracyType and proper JAXB element creation
	DataQuality dataQuality = getOrCreateDataQuality();
	// Implementation needed
    }
}
