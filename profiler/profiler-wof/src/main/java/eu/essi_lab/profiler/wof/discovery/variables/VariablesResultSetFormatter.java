package eu.essi_lab.profiler.wof.discovery.variables;

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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariablesResponseType;
import org.cuahsi.waterml._1.VariablesResponseType.Variables;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.wof.HydroServerProfiler;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFResultSetFormatter;

/**
 * @author boldrini
 */
public abstract class VariablesResultSetFormatter extends WOFResultSetFormatter<VariableInfoType> {

    /**
     * The encoding name of {@link #HYDRO_SERVER_VARIABLES_FORMATTING_ENCODING}
     */
    public static final String HYDRO_SERVER_VARIABLES_ENCODING = "HYDRO_SERVER_VARIABLES_ENCODING";
    /**
     * The encoding version of {@link #HYDRO_SERVER_VARIABLES_FORMATTING_ENCODING}
     */
    public static final String HYDRO_SERVER_VARIABLES_ENCODING_VERSION = "1.1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding HYDRO_SERVER_VARIABLES_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	HYDRO_SERVER_VARIABLES_FORMATTING_ENCODING.setEncoding(HYDRO_SERVER_VARIABLES_ENCODING);
	HYDRO_SERVER_VARIABLES_FORMATTING_ENCODING.setEncodingVersion(HYDRO_SERVER_VARIABLES_ENCODING_VERSION);
	HYDRO_SERVER_VARIABLES_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    @Override
    protected JAXBElement getResult(String url, WOFRequest request, ResultSet<VariableInfoType> mappedResultSet) throws Exception {
	VariablesResponseType vrt = new VariablesResponseType();

	vrt.setQueryInfo(HydroServerProfiler.getQueryInfo(url, request));

	List<VariableInfoType> variableRecords = mappedResultSet.getResultsList();

	Variables vars = new Variables();
	vrt.setVariables(vars);
	vars.getVariable().addAll(variableRecords);

	Marshaller marshaller = JAXBWML.getInstance().getMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
	for (int i = 0; i < variableRecords.size(); i++) {
	    VariableInfoType variableRecord = variableRecords.get(i);
	    variableRecord.getVariableCode().get(0).setVariableID(i);
	}
	String wmlNS = "http://www.cuahsi.org/waterML/1.1/";
	JAXBElement<VariablesResponseType> jaxbElement = new JAXBElement<VariablesResponseType>(new QName(wmlNS, "variablesResponse"),
		VariablesResponseType.class, vrt);
	return jaxbElement;
    }
}
