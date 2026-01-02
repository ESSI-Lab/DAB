package eu.essi_lab.profiler.om;

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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.om.resultwriter.JSONFeatureResultWriter;
import eu.essi_lab.profiler.om.resultwriter.JSONObservationResultWriter;
import eu.essi_lab.profiler.om.resultwriter.ResultWriter;

public class FeaturesHandler extends OMHandler {

	private DatabaseExecutor executor;

	public FeaturesHandler() {
		StorageInfo uri = ConfigurationWrapper.getStorageInfo();
		try {
			this.executor = DatabaseProviderFactory.getExecutor(uri);
		} catch (GSException e) {
			e.printStackTrace();
		}
	}

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

	protected ResultSet<String> exec(DiscoveryMessage message) throws GSException {
		try {
			return executor.discoverDistinctStrings(message);
		} catch (Exception e) {
			e.printStackTrace();
			throw GSException.createException();
		}

	}

	
	public ResultWriter getJSONResultWriter(OutputStreamWriter writer, String viewId, boolean expandFeatures) {
		return new JSONFeatureResultWriter(writer, viewId);
	}

	protected boolean getDistinctStations() {
		return true;
	}

}
