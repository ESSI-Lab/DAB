package eu.essi_lab.lib.utils;

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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class XMLGregorianCalendarUtils {

    public static XMLGregorianCalendar createGregorianCalendar(int year, int month, int day) throws DatatypeConfigurationException {

	TimeZone timeZone = TimeZone.getTimeZone("UTC");
	Calendar calendar = Calendar.getInstance(timeZone);
	calendar.set(year, month - 1, day);

	return createGregorianCalendar(calendar.getTime());
    }

    public static XMLGregorianCalendar createGregorianCalendar(Date date) throws DatatypeConfigurationException {

	GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	calendar.setTime(date);

	XMLGregorianCalendar ret = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

	ret = ret.normalize();
	ret.setFractionalSecond(null);

	return ret;
    }

    public static XMLGregorianCalendar createGregorianCalendar() throws DatatypeConfigurationException {

	return createGregorianCalendar(new Date());
    }

}
