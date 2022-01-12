package eu.essi_lab.messages.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.model.Queryable;

/**
 * @author Fabrizio
 */
public class ResponseItem {

    public static final String ITEMS_RANGE_SEPARATOR = "#";

    @XmlAttribute(name = "groupedBy", required = false)
    private String groupedBy;
    @XmlElement(name = "Min", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComputationResult> min;
    @XmlElement(name = "Max", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComputationResult> max;
    @XmlElement(name = "Sum", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComputationResult> sum;
    @XmlElement(name = "Avg", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComputationResult> avg;
    @XmlElement(name = "CountDistinct", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComputationResult> countDistinct;
    @XmlElement(name = "BboxUnion", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ComputationResult bboxUnion;
    @XmlElement(name = "TemporalExtentUnion", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ComputationResult tmpExtentUnion;
    @XmlElement(name = "Frequency", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComputationResult> frequency;

    /**
     * 
     */
    public ResponseItem() {

	this(null);
    }

    /**
     * @param sourceId
     */
    public ResponseItem(String value) {

	min = new ArrayList<>();
	max = new ArrayList<>();
	sum = new ArrayList<>();
	avg = new ArrayList<>();
	countDistinct = new ArrayList<>();
	frequency = new ArrayList<>();

	setGroupedBy(value);
    }

    /**
     * @return
     */
    @XmlTransient
    public Optional<String> getGroupedBy() {
	return Optional.ofNullable(groupedBy);
    }

    /**
     * @param value
     */
    public void setGroupedBy(String value) {
	this.groupedBy = value;
    }

    /**
     * @return
     */
    @XmlTransient
    public ComputationResult getBBoxUnion() {
	return bboxUnion;
    }

    /**
     * @param bboxUnion
     */
    public void setBBoxUnion(ComputationResult bboxUnion) {
	this.bboxUnion = bboxUnion;
    }

    /**
     * @return
     */
    @XmlTransient
    public ComputationResult getTempExtentUnion() {
	return tmpExtentUnion;
    }

    /**
     * @param bboxUnion
     */
    public void setTempExtentUnion(ComputationResult tmpExtentUnion) {
	this.tmpExtentUnion = tmpExtentUnion;
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComputationResult> getMin() {
	return min;
    }

    /**
     * @param target
     * @return
     */
    public Optional<ComputationResult> getMin(Queryable target) {
	return min.stream().//
		filter(r -> r.getTarget().equals(target.getName())).//
		findFirst();
    }

    /**
     * @param min
     */
    public void addMin(ComputationResult min) {
	this.min.add(min);
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComputationResult> getMax() {
	return max;
    }

    /**
     * @param target
     * @return
     */
    public Optional<ComputationResult> getMax(Queryable target) {
	return max.stream().//
		filter(r -> r.getTarget().equals(target.getName())).//
		findFirst();
    }

    /**
     * @param max
     */
    public void addMax(ComputationResult max) {
	this.max.add(max);
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComputationResult> getSum() {
	return sum;
    }

    /**
     * @param target
     * @return
     */
    public Optional<ComputationResult> getSum(Queryable target) {
	return sum.stream().//
		filter(r -> r.getTarget().equals(target.getName())).//
		findFirst();
    }

    /**
     * @param sum
     */
    public void addSum(ComputationResult sum) {
	this.sum.add(sum);
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComputationResult> getAvg() {
	return avg;
    }

    /**
     * @param target
     * @return
     */
    public Optional<ComputationResult> getAvg(Queryable target) {
	return avg.stream().//
		filter(r -> r.getTarget().equals(target.getName())).//
		findFirst();
    }

    /**
     * @param avg
     */
    public void addFrequency(ComputationResult avg) {
	this.frequency.add(avg);
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComputationResult> getFrequency() {
	return frequency;
    }

    /**
     * @param target
     * @return
     */
    public Optional<ComputationResult> getFrequency(Queryable target) {
	return frequency.stream().//
		filter(r -> r.getTarget().equals(target.getName())).//
		findFirst();
    }

    /**
     * @param avg
     */
    public void addAvg(ComputationResult avg) {
	this.avg.add(avg);
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComputationResult> getCountDistinct() {
	return countDistinct;
    }

    /**
     * @param target
     * @return
     */
    public Optional<ComputationResult> getCountDistinct(Queryable target) {
	return countDistinct.stream().//
		filter(r -> r.getTarget().equals(target.getName())).//
		findFirst();
    }

    /**
     * @param countDistinct
     */
    public void addCountDistinct(ComputationResult countDistinct) {
	this.countDistinct.add(countDistinct);
    }

}
