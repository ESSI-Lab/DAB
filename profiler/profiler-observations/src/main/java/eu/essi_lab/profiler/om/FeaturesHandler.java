package eu.essi_lab.profiler.om;

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

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONObject;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

public class FeaturesHandler extends OMHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new FeaturesTransformer();
    }

    protected void addIdentifier(OutputStreamWriter writer) throws IOException {
	
    }
    
    protected String getSetName() {
	return "results";
    }
    
    public void writeFeature(OutputStreamWriter writer, JSONObject feature) throws IOException {

//	JSONObject properties = feature.getJSONObject("properties");

	JSONObject monitoringPoint = feature.getJSONObject("featureOfInterest");
	monitoringPoint.remove("type");
	
	JSONObject shape = monitoringPoint.getJSONObject(getGeometryName());
	monitoringPoint.put(getGeometryName(), shape);

	writer.write(monitoringPoint.toString());

	writer.flush();

    }
    


    public String getObject() {
	return "feature";
    }

}
