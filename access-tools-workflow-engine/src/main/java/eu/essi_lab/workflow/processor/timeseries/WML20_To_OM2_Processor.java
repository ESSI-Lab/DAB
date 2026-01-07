package eu.essi_lab.workflow.processor.timeseries;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;

public class WML20_To_OM2_Processor extends DataProcessor {

    private static final String WML_20_TO_OM2_PROCESSOR_ERROR = "WML_20_TO_OM2_PROCESSOR_ERROR";

    public WML20_To_OM2_Processor() {
    }

    @Override
    public DataObject process(GSResource resource,DataObject dataObject, TargetHandler handler) throws Exception {

	InputStream stream = new FileInputStream(dataObject.getFile());
	InputStream result = convert(stream);
	stream.close();
	DataObject ret = new DataObject();
	ret.setFileFromStream(result, getClass().getSimpleName());
	if (result != null)
	    result.close();
	return ret;
    }

    public InputStream convert(InputStream wml20Stream) throws GSException {

	CollectionType collection;
	try {
	    collection = JAXBWML2.getInstance().unmarshalCollection(wml20Stream);
	    wml20Stream.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw getGSException("Error unmarshalling WML 2.0");
	}
	try {

	    OMObservationType observation = collection.getObservationMember().get(0).getOMObservation();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBWML2.getInstance().marshal(observation, baos);
	    baos.close();

	    return new ByteArrayInputStream(baos.toByteArray());

	} catch (Exception e) {
	    e.printStackTrace();
	    throw getGSException("Error writing Observation");
	}

    }

    private GSException getGSException(String message) {

	return GSException.createException(//
		getClass(), //
		message, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		WML_20_TO_OM2_PROCESSOR_ERROR);

    }

}
