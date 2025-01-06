package eu.essi_lab.accessor.wof.client.datamodel;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.InputStream;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

public abstract class GetValuesRequest {

    protected XMLDocumentReader reader;
    protected XMLDocumentWriter writer;

    protected GetValuesRequest(InputStream stream) {
	try {
	    this.reader = new XMLDocumentReader(stream);
	    this.writer = new XMLDocumentWriter(reader);
	} catch (Exception e) {
	    // this should not happen, as this constructor should be used only by "controlled classes" in this package
	    // (protected visibility)

	    GSLoggerFactory.getLogger(GetValuesRequest.class).error("Error initializing GetValuesRequest", e);

	}

    }

    public abstract String getLocation();

    public abstract String getVariable();

    public abstract String getStartDate();

    public abstract String getEndDate();

    public abstract void setLocation(String location);

    public abstract void setVariable(String variable);

    public abstract void setStartDate(String timeBegin);

    public abstract void setEndDate(String timeEnd);

    public XMLDocumentReader getReader() {
	return reader;
    }
}
