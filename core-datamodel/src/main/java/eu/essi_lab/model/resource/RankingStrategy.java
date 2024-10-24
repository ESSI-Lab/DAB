package eu.essi_lab.model.resource;

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

import java.io.Serializable;
import java.lang.reflect.Field;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.index.IndexedElement;

/**
 * A strategy based on a configurable set of weights used to define a ranking by which order the {@link ResultSet}
 * results (see {@link
 * ResultSet#getResultsList()}
 *
 * @author Fabrizio
 */
public class RankingStrategy implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1955823856293860562L;

    /**
     * Quality weight set to 0 since docs quality is now a GIResource extension
     */
    public static final int QUALITY_WEIGHT = 0;

    /**
     * The maximum value for the variables isGDC, ev, mdq, aq
     */
    public static final int MAX_VARIABLE_VALUE = 10;

    /**
     * The max allowed value for a MarkLogic range and word query weight
     */
    private static final double MAX_ML_WEIGHT = 64;

    private String scoreMethod;
    private String maxAssignableWeight;
    private String titleWeight;
    private String abstractWeight;
    private String boundingBoxWeight;
    private String subjectWeight;
    private String anyTextWeight;
    private String isGDCWeight;
    private String mdqWeight;
    private String evWeight;
    private String aqWeight;

    private static final String DEFAULT_SCORE_METHOD = "score-simple";
    private static final String DEFAULT_MAX_ASSIGNABLE_WEIGHT = "100";
    private static final String DEFAULT_TITLE_WEIGHT = "90";
    private static final String DEFAULT_ABSTRACT_WEIGHT = "40";
    private static final String DEFAULT_BOUNDING_BOX_WEIGHT = "30";
    private static final String DEFAULT_SUBJECT_WEIGHT = "70";
    private static final String DEFAULT_ANY_TEXT_WEIGHT = "10";
    private static final String DEFAULT_IS_GDC_WEIGHT = "40";
    private static final String DEFAULT_MDQ_WEIGHT = "20";
    private static final String DEFAULT_EV_WEIGHT = "20";
    private static final String DEFAULT_AQ_WEIGHT = "80";

    public RankingStrategy() {

	scoreMethod = DEFAULT_SCORE_METHOD;
	maxAssignableWeight = DEFAULT_MAX_ASSIGNABLE_WEIGHT;
	titleWeight = DEFAULT_TITLE_WEIGHT;
	abstractWeight = DEFAULT_ABSTRACT_WEIGHT;
	boundingBoxWeight = DEFAULT_BOUNDING_BOX_WEIGHT;
	subjectWeight = DEFAULT_SUBJECT_WEIGHT;
	anyTextWeight = DEFAULT_ANY_TEXT_WEIGHT;
	isGDCWeight = DEFAULT_IS_GDC_WEIGHT;
	mdqWeight = DEFAULT_MDQ_WEIGHT;
	evWeight = DEFAULT_EV_WEIGHT;
	aqWeight = DEFAULT_AQ_WEIGHT;
    }

    /**
     * @param el
     * @param value
     * @return
     */
    public int computeRangeWeight(IndexedElement el, int value) {

	double maxRangeVarWeight = getMaximumRangeVariableWeight();

	double weight = Double.valueOf(getPropertyValue(el.getElementName())) * value;

	return (int) ((weight / maxRangeVarWeight) * MAX_ML_WEIGHT);
    }

    /**
     * @param propertyName
     * @return
     */
    public int computePropertyWeight(Queryable el) {

	return computePropertyWeight(el.getName());
    }

    /**
     * @param area
     * @param percent
     * @param areaWeight
     * @return
     */
    public double computeBoundingBoxWeight(double area, int percent) {

	int areaWeight = Integer.parseInt(boundingBoxWeight);

	int value = computeAreaVariableValue(areaWeight, percent);

	return (int) normalizeBoundingBoxWeight(value, areaWeight);
    }

    /**
     * @param scoreMethod
     */
    public void setScoreMethod(String scoreMethod) {

	this.scoreMethod = scoreMethod;
    }

    /**
     * @return
     */
    public String getScoreMethod() {

	return scoreMethod;
    }

    public void setMaxAssignableWeight(String maxAssignableWeight) {

	this.maxAssignableWeight = maxAssignableWeight;
    }

    public void setTitleWeight(String title) {

	this.titleWeight = title;
    }

    public void setAbstractWeight(String weight) {

	this.abstractWeight = weight;
    }

    public void setBoundingBoxWeight(String weight) {

	this.boundingBoxWeight = weight;
    }

    public void setSubjectWeight(String weight) {

	this.subjectWeight = weight;
    }

    public void setAnyTextWeight(String weight) {

	this.anyTextWeight = weight;
    }

    public void setIsGDCWeight(String weight) {

	this.isGDCWeight = weight;
    }

    public void setMetadataQualityWeight(String weight) {

	this.mdqWeight = weight;
    }

    public void setEssentialVariablesWeight(String weight) {

	this.evWeight = weight;
    }

    public void setAccessQualityWeight(String weight) {

	this.aqWeight = weight;
    }

    /**
     * 
     */
    public void setZeroWeightAndScore() {
    
        setAbstractWeight("0");
        setAccessQualityWeight("0");
        setAnyTextWeight("0");
        setBoundingBoxWeight("0");
        setEssentialVariablesWeight("0");
        setIsGDCWeight("0");
        setMetadataQualityWeight("0");
        setSubjectWeight("0");
        setTitleWeight("0");
        setScoreMethod("score-zero");
    }

    /**
     * @param propertyName
     * @return
     */
    private int computePropertyWeight(String propertyName) {

	String value = getPropertyValue(propertyName);
	if (value == null) {
	    return 0;
	}

	double weight = Double.parseDouble(value);

	return (int) ((weight / readMaxAssignableWeight()) * MAX_ML_WEIGHT);
    }

    private String getPropertyValue(String property) {

	if (property.equals(MetadataElement.TITLE.getName())) {
	    return titleWeight;
	}

	if (property.equals(MetadataElement.ABSTRACT.getName())) {
	    return abstractWeight;
	}

	if (property.equals(MetadataElement.BOUNDING_BOX.getName())) {
	    return boundingBoxWeight;
	}

	if (property.equals(MetadataElement.SUBJECT.getName())) {
	    return subjectWeight;
	}

	if (property.equals(MetadataElement.ANY_TEXT.getName())) {
	    return anyTextWeight;
	}

	if (property.equals(ResourceProperty.IS_GEOSS_DATA_CORE.getName())) {
	    return isGDCWeight;
	}

	if (property.equals(ResourceProperty.MEDATADATA_QUALITY.getName())) {
	    return mdqWeight;
	}

	if (property.equals(ResourceProperty.ESSENTIAL_VARS_QUALITY.getName())) {
	    return evWeight;
	}

	if (property.equals(ResourceProperty.ACCESS_QUALITY.getName())) {
	    return aqWeight;
	}

	return null;
    }

    private double percent(double target, int value) {

	return (target / 100) * value;
    }

    private double normalizeBoundingBoxWeight(int value, int areaWeight) {

	double weight = value * (double) areaWeight;

	return (weight / getMaximumRangeVariableWeight()) * MAX_ML_WEIGHT;
    }

    private double getMaximumRangeVariableWeight() {

	return readMaxAssignableWeight() * MAX_VARIABLE_VALUE;
    }

    private double readMaxAssignableWeight() {

	return Double.valueOf(maxAssignableWeight);
    }

    private int computeAreaVariableValue(double area, int percentValue) {

	return (int) normalizeAreaValue(percent(area, percentValue), area, 10);
    }

    private double normalizeAreaValue(double target, double max, int treshold) {

	return (target / max) * treshold;
    }

    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder();

	Field[] allFields = getClass().getDeclaredFields();
	for (int i = 0; i < allFields.length; i++) {
	    Field field = allFields[i];
	    try {
		builder.append(field.getName() + ": " + field.get(this) + ", ");
	    } catch (Exception e) {
	    }
	}
	return builder.toString();
    }
}
