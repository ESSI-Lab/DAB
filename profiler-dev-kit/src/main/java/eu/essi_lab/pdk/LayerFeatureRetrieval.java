package eu.essi_lab.pdk;

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

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.pdk.rsm.access.AccessQueryUtils;
import eu.essi_lab.request.executor.IAccessExecutor;

public class LayerFeatureRetrieval {

    private static LayerFeatureRetrieval instance = null;
    private static final Provider PROVIDER = new ESSILabProvider();

    private LayerFeatureRetrieval() {
    }

    public static LayerFeatureRetrieval getInstance() {
	if (instance == null) {
	    instance = new LayerFeatureRetrieval();
	}
	return instance;
    }

    public String getFeature(String layerName) {
	try {

	    DataDescriptor targetDescriptor = new DataDescriptor();	    
	    targetDescriptor.setDataType(DataType.VECTOR);
	    targetDescriptor.setCRS(CRS.EPSG_4326());
	    targetDescriptor.setDataFormat(DataFormat.WKT());
	    ServiceLoader<IAccessExecutor> accessLoader = ServiceLoader.load(IAccessExecutor.class);
	    IAccessExecutor accessExecutor = accessLoader.iterator().next();
	    ResultSet<GSResource> resource = AccessQueryUtils.findResource(UUID.randomUUID().toString(), Optional.empty(), layerName);
	    ResultSet<DataObject> retrieved = accessExecutor.retrieve(resource.getResultsList().get(0), layerName, targetDescriptor);
	    try {
		File ff = retrieved.getResultsList().get(0).getFile();
		byte[] bytes = Files.readAllBytes(ff.toPath());
		String str = new String(bytes);
		ff.delete();
		return str;
	    } catch (Exception e) {
		throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	    }

	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error retrieving WKT for layer {}: {}", layerName, e.getMessage());
	    return null;
	}
    }
}
