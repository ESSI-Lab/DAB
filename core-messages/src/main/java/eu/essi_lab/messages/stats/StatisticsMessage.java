/**
 * 
 */
package eu.essi_lab.messages.stats;

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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.QueryInitializerMessage;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.Queryable;

/**
 * @author Fabrizio
 */
public class StatisticsMessage extends QueryInitializerMessage {

    private static final String MIN = "MIN";
    private static final String MAX = "MAX";
    private static final String SUM = "SUM";
    private static final String AVG = "AVG";
    private static final String BBOX_UNION = "BBOX_UNION";
    private static final String QUERY_BBOX_UNION = "QUERY_BBOX_UNION";
    private static final String TEMP_EXTENT_UNION = "TEMP_EXTENT_UNION";
    private static final String QUERY_TEMP_EXTENT_UNION = "QUERY_TEMP_EXTENT_UNION";
    private static final String FREQUENCY = "FREQUENCY";
    private static final String FREQUENCY_MAX_ITEMS = "FREQUENCY_MAX_ITEMS";
    private static final String COUNT_DISTINCT = "COUNT_DISTINCT";
    private static final String GROUP_BY_QUERYABLE = "GROUP_BY_QUERYABLE";
    private static final String GROUP_BY_PERIOD = "GROUP_BY_PERIOD";

    /**
     * @author Fabrizio
     */
    public static class GroupByPeriod {

	private Queryable target;
	private long period;
	private double fraction;
	private long startTime;
	private String interval;

	/**
	 * @return
	 */
	public Queryable getTarget() {
	    return target;
	}

	/**
	 * @param target
	 */
	public void setTarget(Queryable target) {
	    this.target = target;
	}

	/**
	 * @return
	 */
	public long getPeriod() {
	    return period;
	}

	/**
	 * @param period
	 */
	public void setPeriod(long period) {
	    this.period = period;
	}

	/**
	 * @return
	 */
	public double getFraction() {
	    return fraction;
	}

	/**
	 * @param fraction
	 */
	public void setFraction(double fraction) {
	    this.fraction = fraction;
	}

	/**
	 * @return
	 */
	public long getStartTime() {
	    return this.startTime;
	}

	/**
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
	    this.startTime = startTime;
	}

	/**
	 * @param interval e.g. 2d for 2 days
	 */
	public void setInterval(String interval) {
	    this.interval = interval;

	}

	public String getInterval() {
	    return interval;

	}
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2588502390274546072L;

    /**
     * @param period
     * @param fraction
     */
    public void groupBy(GroupByPeriod groupByPeriod) {

	getPayload().add(new GSProperty<GroupByPeriod>(GROUP_BY_PERIOD, groupByPeriod));
    }

    /**
     * @return
     */
    public Optional<GroupByPeriod> getGroupByPeriod() {

	return Optional.ofNullable(getPayload().get(GROUP_BY_PERIOD, GroupByPeriod.class));
    }

    /**
     * @param queryable
     */
    public void groupBy(Queryable queryable) {

	getPayload().add(new GSProperty<Queryable>(GROUP_BY_QUERYABLE, queryable));
    }

    /**
     * @return
     */
    public Optional<Queryable> getGroupByTarget() {

	return Optional.ofNullable(getPayload().get(GROUP_BY_QUERYABLE, Queryable.class));
    }

    /**
     * 
     */
    public void computeBboxUnion() {

	getPayload().add(new GSProperty<Boolean>(BBOX_UNION, true));
    }

    /**
     * @return
     */
    public boolean isBboxUnionComputationSet() {

	Optional<Boolean> optional = Optional.ofNullable(getPayload().get(BBOX_UNION, Boolean.class));
	if (optional.isPresent()) {
	    return optional.get();
	}

	return false;
    }

    /**
     * 
     */
    public void computeQueryBboxUnion() {

	getPayload().add(new GSProperty<Boolean>(QUERY_BBOX_UNION, true));
    }

    /**
     * @return
     */
    public boolean isQueryBboxUnionComputationSet() {

	Optional<Boolean> optional = Optional.ofNullable(getPayload().get(QUERY_BBOX_UNION, Boolean.class));
	if (optional.isPresent()) {
	    return optional.get();
	}

	return false;
    }

    /**
     * 
     */
    public void computeTempExtentUnion() {

	getPayload().add(new GSProperty<Boolean>(TEMP_EXTENT_UNION, true));
    }

    /**
     * 
     */
    public void computeQueryTempExtentUnion() {

	getPayload().add(new GSProperty<Boolean>(QUERY_TEMP_EXTENT_UNION, true));
    }

    /**
     * @return
     */
    public boolean isTempExtentUnionComputationSet() {

	Optional<Boolean> optional = Optional.ofNullable(getPayload().get(TEMP_EXTENT_UNION, Boolean.class));
	if (optional.isPresent()) {
	    return optional.get();
	}

	return false;
    }

    /**
     * @return
     */
    public boolean isQueryTempExtentUnionComputationSet() {

	Optional<Boolean> optional = Optional.ofNullable(getPayload().get(QUERY_TEMP_EXTENT_UNION, Boolean.class));
	if (optional.isPresent()) {
	    return optional.get();
	}

	return false;
    }

    /**
     * @param queryables
     */
    public void countDistinct(List<Queryable> queryables) {

	getPayload().add(new GSProperty<List<Queryable>>(COUNT_DISTINCT, queryables));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Queryable>> getCountDistinctTargets() {

	return Optional.ofNullable(getPayload().get(COUNT_DISTINCT, List.class));
    }

    /**
     * @param queryables
     */
    public void computeMin(List<Queryable> queryables) {

	getPayload().add(new GSProperty<List<Queryable>>(MIN, queryables));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Queryable>> getMinTargets() {

	return Optional.ofNullable(getPayload().get(MIN, List.class));
    }

    /**
     * @param queryables
     */
    public void computeMax(List<Queryable> queryables) {

	getPayload().add(new GSProperty<List<Queryable>>(MAX, queryables));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Queryable>> getMaxTargets() {

	return Optional.ofNullable(getPayload().get(MAX, List.class));
    }

    /**
     * @param queryables
     */
    public void computeSum(List<Queryable> queryables) {

	getPayload().add(new GSProperty<List<Queryable>>(SUM, queryables));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Queryable>> getSumTargets() {

	return Optional.ofNullable(getPayload().get(SUM, List.class));
    }

    /**
     * @param queryables
     */
    public void computeAvg(List<Queryable> queryables) {

	getPayload().add(new GSProperty<List<Queryable>>(AVG, queryables));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Queryable>> getAvgTargets() {

	return Optional.ofNullable(getPayload().get(AVG, List.class));
    }

    /**
     * @param queryables
     */
    public void computeFrequency(List<Queryable> queryables) {

	computeFrequency(queryables, 10);
    }

    /**
     * @param queryables
     * @param maxItems
     */
    public void computeFrequency(List<Queryable> queryables, int maxItems) {

	getPayload().add(new GSProperty<List<Queryable>>(FREQUENCY, queryables));
	getPayload().add(new GSProperty<Integer>(FREQUENCY_MAX_ITEMS, maxItems));
    }

    /**
     * @return
     */
    public Optional<Integer> getMaxFrequencyItems() {

	return Optional.ofNullable(getPayload().get(FREQUENCY_MAX_ITEMS, Integer.class));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Queryable>> getFrequencyTargets() {

	return Optional.ofNullable(getPayload().get(FREQUENCY, List.class));
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	return super.provideInfo();
    }

    @Override
    public String getName() {

	return "STATISTICS_MESSAGE";
    }
}
