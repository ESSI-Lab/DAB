package eu.essi_lab.accessor.canada;

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

import java.text.ParseException;
import java.util.Date;
import java.util.TreeSet;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;


public class Measurement implements Comparable<Measurement> {
    Date date;
    Double value;
    
    public Measurement() {
	
    }

    public Measurement(Date date, Double value) {
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    // this way a tree set iterator iterates from first date to last
    @Override
    public int compareTo(Measurement o) {
        return this.date.compareTo(o.getDate());
    }
    
    public static void main(String[] args) throws ParseException {
	TreeSet<Measurement> measurements = new TreeSet<Measurement>();
	measurements.add(new Measurement(ISO8601DateTimeUtils.parseISO8601("2012-01-01T23:23:23Z"), 2.0));
	measurements.add(new Measurement(ISO8601DateTimeUtils.parseISO8601("2016-01-01T23:23:23Z"), 2.0));
	measurements.add(new Measurement(ISO8601DateTimeUtils.parseISO8601("2010-01-01T23:23:23Z"), 2.0));
	for (Measurement measurement : measurements) {
	    System.out.println(measurement.getDate());
	}
    }

}
