/**
 * 
 */
package eu.essi_lab.iso.datamodel;

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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import net.opengis.iso19139.gco.v_20060504.DatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.CIDatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIDateType;

/**
 * @author Fabrizio
 */
public class MD_MetadataPatch {

    /**
     * @param metadata
     */
    public static void applyAll(MDMetadata metadata) {

	applyDateTimePatch(metadata);
    }

    /**
     * See GIP-412
     * 
     * @param metadata
     */
    public static void applyDateTimePatch(MDMetadata metadata) {

	DatePropertyType dateStamp = metadata.getElementType().getDateStamp();

	if (Objects.nonNull(dateStamp)) {
	    applyPatch(dateStamp);
	}

	Iterator<DataIdentification> identifications = metadata.getDataIdentifications();
	identifications.forEachRemaining(id -> {
	    try {

		CICitationPropertyType citation = id.getElementType().getCitation();
		CICitationType ciCitation = citation.getCICitation();
		List<CIDatePropertyType> citationDates = ciCitation.getDate();

		for (CIDatePropertyType ciDatePropertyType : citationDates) {

		    CIDateType ciDate = ciDatePropertyType.getCIDate();
		    applyPatch(ciDate.getDate());
		}
	    } catch (Exception ex) {
	    }
	});
    }

    /**
     * @param dateStamp
     */
    private static void applyPatch(DatePropertyType dateStamp) {

	String date = dateStamp.getDate();
	XMLGregorianCalendar dateTime = dateStamp.getDateTime();

	if (Objects.isNull(date) && Objects.nonNull(dateTime)) {

	    String dateTimeString = dateTime.toString();
	    if (dateTimeString.contains("T")) {
		dateStamp.setDateTime(null);

		dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("T"));
		dateStamp.setDate(dateTimeString);
	    }
	}
    }
}
