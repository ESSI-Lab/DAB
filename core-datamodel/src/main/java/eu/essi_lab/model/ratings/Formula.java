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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A piece-wise rating-curve formula: discharge {@code Q} expressed as a set of sub-formulas, each valid over a
 * water-level range (see {@link FormulaRange}).
 *
 * @author boldrini
 */
public class Formula {

    private final List<FormulaRange> ranges = new ArrayList<>();

    public void addRange(FormulaRange range) {
	ranges.add(range);
    }

    public List<FormulaRange> getRanges() {
	return Collections.unmodifiableList(ranges);
    }

    public boolean isEmpty() {
	return ranges.isEmpty();
    }

    /**
     * @return the largest finite bound used by any range (the "top" water level), or {@link Double#NaN} if no
     *         finite bound exists
     */
    public double getTopRangeValue() {

	double top = Double.NaN;
	for (FormulaRange range : ranges) {
	    top = maxFinite(top, range.getLowerBound());
	    top = maxFinite(top, range.getUpperBound());
	}
	return top;
    }

    private static double maxFinite(double current, double candidate) {

	if (!Double.isFinite(candidate)) {
	    return current;
	}
	return Double.isNaN(current) ? candidate : Math.max(current, candidate);
    }

    /**
     * Evaluates the discharge for the given water level, selecting the range that contains it (or, if none does,
     * the range whose bounds are closest).
     *
     * @return discharge, or {@link Double#NaN} if the formula has no ranges or cannot be evaluated
     */
    public double evaluate(double level) {

	FormulaRange selected = null;
	for (FormulaRange range : ranges) {
	    if (range.contains(level)) {
		selected = range;
		break;
	    }
	}
	if (selected == null) {
	    selected = closestRange(level);
	}
	return selected == null ? Double.NaN : selected.evaluate(level);
    }

    private FormulaRange closestRange(double level) {

	FormulaRange best = null;
	double bestDistance = Double.POSITIVE_INFINITY;
	for (FormulaRange range : ranges) {
	    double distance;
	    if (level < range.getLowerBound()) {
		distance = range.getLowerBound() - level;
	    } else if (level > range.getUpperBound()) {
		distance = level - range.getUpperBound();
	    } else {
		distance = 0;
	    }
	    if (distance < bestDistance) {
		bestDistance = distance;
		best = range;
	    }
	}
	return best;
    }

    @Override
    public String toString() {

	StringBuilder sb = new StringBuilder();
	for (FormulaRange range : ranges) {
	    sb.append("    ").append(range).append("\n");
	}
	return sb.toString();
    }
}
