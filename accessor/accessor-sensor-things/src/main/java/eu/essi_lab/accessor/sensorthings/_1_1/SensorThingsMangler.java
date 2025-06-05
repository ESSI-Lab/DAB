package eu.essi_lab.accessor.sensorthings._1_1;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * @author Fabrizio
 */
public class SensorThingsMangler extends KVPMangler {

    /**
     * 
     */
    private static final String STREAM_IDENTIFIER = "streamId";
    /**
     * 
     */
    private static final String QUOTE_IDENTIFIERS = "quoteIds";

    /**
     * 
     */
    public SensorThingsMangler() {
	super(";");
    }

    /**
     * @param procedure
     */
    public void setStreamIdentifer(String procedure) {
	setParameter(STREAM_IDENTIFIER, procedure);
    }

    /**
     * @return
     */
    public String getStreamIdentifer() {
	return getParameterValue(STREAM_IDENTIFIER);
    }

    /**
     * @param feature
     */
    public void setQuoteIdentifiers(String feature) {
	setParameter(QUOTE_IDENTIFIERS, feature);
    }

    /**
     * @return
     */
    public String getQuoteIdentifiers() {
	return getParameterValue(QUOTE_IDENTIFIERS);
    }

}
