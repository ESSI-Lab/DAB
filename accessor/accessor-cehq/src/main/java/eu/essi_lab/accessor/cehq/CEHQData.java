package eu.essi_lab.accessor.cehq;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

public class CEHQData {

    private List<SimpleEntry<Date, BigDecimal>> values;

    private Double latitude;

    public Double getLatitude() {
	return latitude;
    }

    public void setLatitude(Double latitude) {
	this.latitude = latitude;
    }

    private Double longitude;

    public Double getLongitude() {
	return longitude;
    }

    public void setLongitude(Double longitude) {
	this.longitude = longitude;
    }

    public List<SimpleEntry<Date, BigDecimal>> getValues() {
	return values;
    }

    public void setValues(List<SimpleEntry<Date, BigDecimal>> values) {
	this.values = values;

    }

    public Date getFirstDate() {
	if (values.isEmpty()) {
	    return null;
	}
	return values.get(0).getKey();
    }

    public Date getLastDate() {
	if (values.isEmpty()) {
	    return null;
	}
	return values.get(values.size() - 1).getKey();
    }

}
