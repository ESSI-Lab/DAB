package eu.essi_lab.model.ratings;

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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RatingCurve {

    private final LocalDate beginDate;
    private final LocalDate endDate;
    private final List<RatingCurvePoint> points = new ArrayList<>();
    private Formula formula;
    private double minLevel = Double.NaN;
    private double maxLevel = Double.NaN;
    private int numberOfPoints = 0;

    public RatingCurve() {
	this(null, null);
    }

    public RatingCurve(LocalDate beginDate, LocalDate endDate) {
	this.beginDate = beginDate;
	this.endDate = endDate;
    }

    public void addPoint(RatingCurvePoint point) {
	points.add(point);
    }

    public LocalDate getBeginDate() {
	return beginDate;
    }

    public LocalDate getEndDate() {
	return endDate;
    }

    public List<RatingCurvePoint> getPoints() {
	return Collections.unmodifiableList(points);
    }

    public Formula getFormula() {
	return formula;
    }

    public void setFormula(Formula formula) {
	this.formula = formula;
    }

    public double getMinLevel() {
	return minLevel;
    }

    public void setMinLevel(double minLevel) {
	this.minLevel = minLevel;
    }

    public double getMaxLevel() {
	return maxLevel;
    }

    public void setMaxLevel(double maxLevel) {
	this.maxLevel = maxLevel;
    }

    public int getNumberOfPoints() {
	return numberOfPoints;
    }

    public void setNumberOfPoints(int numberOfPoints) {
	this.numberOfPoints = numberOfPoints;
    }

    /**
     * Generates the points using the configured {@link #getNumberOfPoints() number of points} over the
     * {@code [minLevel, maxLevel]} range. No-op if those are not set.
     */
    public void generatePoints() {

	generatePoints(numberOfPoints, minLevel, maxLevel);
    }

    /**
     * Replaces the current points with a list generated from the {@link Formula}, sampling {@code count} water
     * levels evenly from {@code minLevel} to {@code maxLevel} and evaluating the discharge at each. Levels for
     * which the formula cannot be evaluated are skipped.
     *
     * @param count    the number of sample points
     * @param minLevel the lowest water level to sample
     * @param maxLevel the highest water level to sample
     */
    public void generatePoints(int count, double minLevel, double maxLevel) {

	points.clear();
	if (formula == null || formula.isEmpty() || count < 1 || Double.isNaN(minLevel) || Double.isNaN(maxLevel)) {
	    return;
	}
	for (int i = 0; i < count; i++) {
	    double level = count == 1 ? minLevel : minLevel + (maxLevel - minLevel) * i / (count - 1);
	    double discharge = formula.evaluate(level);
	    if (Double.isFinite(discharge)) {
		points.add(new RatingCurvePoint(//
			new BigDecimal(level, MathContext.DECIMAL64), //
			new BigDecimal(discharge, MathContext.DECIMAL64)));
	    }
	}
    }
}
