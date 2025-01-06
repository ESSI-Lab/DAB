package eu.essi_lab.profiler.oaipmh;

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

import eu.essi_lab.messages.web.KeyValueParser;

/**
 * @author Fabrizio
 */
public class OAIPMHRequestReader {

    private KeyValueParser parser;

    public OAIPMHRequestReader(KeyValueParser parser) {

	this.parser = parser;
    }

    public String getVerb() {

	return parser.getValue("verb",false);
    }

    public String getIdentifier() {

	return parser.getValue("identifier",false);
    }

    public String getFrom() {

	return parser.getValue("from",false);
    }

    public String getUntil() {

	return parser.getValue("until",false);
    }

    public String getSet() {

	return parser.getValue("set",false);
    }

    public String getResumptionToken() {

	return parser.getValue("resumptionToken",false);
    }

    public String getMetadataPrefix() {

	return parser.getValue("metadataPrefix",false);
    }

}
