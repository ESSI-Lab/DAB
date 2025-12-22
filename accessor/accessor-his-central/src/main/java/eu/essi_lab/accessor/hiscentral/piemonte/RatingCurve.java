package eu.essi_lab.accessor.hiscentral.piemonte;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RatingCurve {

    private final LocalDate beginDate;
    private final LocalDate endDate;
    private final List<RatingCurvePoint> points = new ArrayList<>();

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
}
