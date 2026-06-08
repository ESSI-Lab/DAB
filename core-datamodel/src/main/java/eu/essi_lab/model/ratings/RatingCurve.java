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

    /**
     * Replaces the current points with a list generated from the {@link Formula}, sampling {@code count} water
     * levels evenly from {@code 0} up to twice the formula's top range value, and evaluating the discharge at each.
     * Levels for which the formula cannot be evaluated are skipped.
     *
     * @param count the number of sample points (e.g. 100)
     */
    public void generatePoints(int count) {

	points.clear();
	if (formula == null || formula.isEmpty() || count < 2) {
	    return;
	}
	double top = formula.getTopRangeValue();
	if (Double.isNaN(top) || top <= 0) {
	    return;
	}
	double max = top * 2;
	for (int i = 0; i < count; i++) {
	    double level = max * i / (count - 1);
	    double discharge = formula.evaluate(level);
	    if (Double.isFinite(discharge)) {
		points.add(new RatingCurvePoint(//
			new BigDecimal(level, MathContext.DECIMAL64), //
			new BigDecimal(discharge, MathContext.DECIMAL64)));
	    }
	}
    }
}
