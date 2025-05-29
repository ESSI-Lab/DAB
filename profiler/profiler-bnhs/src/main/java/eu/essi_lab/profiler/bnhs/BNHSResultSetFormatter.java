package eu.essi_lab.profiler.bnhs;

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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * @author boldrini
 */
public class BNHSResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #BNHS_ENCODING}
     */
    public static final String BNHS_ENCODING = "BNHS_ENCODING";
    /**
     * The encoding version of {@link #BNHS_ENCODING_VERSION}
     */
    public static final String BNHS_ENCODING_VERSION = "1.0.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding BNHS_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	BNHS_FORMATTING_ENCODING.setEncoding(BNHS_ENCODING);
	BNHS_FORMATTING_ENCODING.setEncodingVersion(BNHS_ENCODING_VERSION);
	BNHS_FORMATTING_ENCODING.setMediaType(new MediaType("text", "csv"));
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	List<String> seriesRecords = mappedResultSet.getResultsList();

	String ret = "";

	if (!seriesRecords.isEmpty()) {
	    String headers = seriesRecords.get(0);

	    // Adding the headers
	    String[] split = headers.split(BNHSResultSetMapper.SEPARATOR);
	    for (int i = 0; i < (split.length - 1); i += 2) {
		String header = split[i];
		ret += header + BNHSResultSetMapper.SEPARATOR;
	    }
	    ret = ret.substring(0, ret.length() - 1) + "\n";

	    // Adding the values
	    for (String series : seriesRecords) {
		String values = "";
		split = series.split(BNHSResultSetMapper.SEPARATOR);
		for (int i = 0; i < (split.length - 1); i += 2) {
		    String value = split[i + 1];
		    if (value == null) {
			value = "";
		    }
		    // replacing possible tabs appearing in the value
		    value = value.replace(BNHSResultSetMapper.SEPARATOR, " ");
		    values += value + BNHSResultSetMapper.SEPARATOR;
		}
		values = values.substring(0, values.length() - 1) + "\n";
		ret += values;
	    }
	}

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(ret).encoding("UTF-8").type(new MediaType("application", "csv", "UTF-8")).header("Content-Disposition",
		"attachment;filename=\"BNHS.csv\"");

	return builder.build();

    }

    @Override
    public FormattingEncoding getEncoding() {

	return BNHS_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

}
