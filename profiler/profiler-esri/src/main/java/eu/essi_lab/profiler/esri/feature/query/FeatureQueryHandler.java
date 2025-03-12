package eu.essi_lab.profiler.esri.feature.query;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.core.Response;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.esri.feature.FeatureLayer;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;

public class FeatureQueryHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    @Override
    public Response handle(WebRequest request) throws GSException {

	FeatureQueryRequestTransformer transformer = new FeatureQueryRequestTransformer();
	DiscoveryMessage discoveryMessage = transformer.transform(request);

	Optional<String> viewId = request.extractViewId();

	String path = request.getServletRequest().getPathInfo();
	String id = path.substring(path.indexOf("FeatureServer/"));
	id = id.substring(id.indexOf("/") + 1);
	id = id.substring(0, id.indexOf("/"));
	FeatureLayer layer = FeatureLayer.getLayer(id);

	if (viewId.isPresent()) {
	    String vid = viewId.get();
	    try {
		if (CachedCollections.getInstance().isPresent(vid, layer)) {
		    SimpleFeatureCollection collection = CachedCollections.getInstance().queryCollection(vid, layer,
			    discoveryMessage.getUserBond());
		    FeatureQueryResultSetFormatterGeotools formatter = new FeatureQueryResultSetFormatterGeotools();
		    SimpleFeatureIterator iterator = collection.features();
		    List<SimpleFeature>features =new ArrayList<>();
		    while(iterator.hasNext()) {
			SimpleFeature feature = iterator.next();
			features.add(feature);
		    }
		    ResultSet<SimpleFeature>results = new ResultSet<SimpleFeature>(features);
		    
		    Response response = formatter.format(discoveryMessage, results);

		    return response;

		} else {
		    CachedCollections.getInstance().prepare(request, vid, layer);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	ServiceLoader<IDiscoveryNodeExecutor> loader = ServiceLoader.load(IDiscoveryNodeExecutor.class);
	IDiscoveryNodeExecutor executor = loader.iterator().next();

	ResultSet<Node> resultSet = null;
	ResultSet<Node> tmpResultSet = new ResultSet<>();

	Page page = discoveryMessage.getPage();
	int pageSize = page.getSize();
	do {

	    discoveryMessage.setRequestId(request.getRequestId());

	    StorageInfo storageUri = getStorageURI(discoveryMessage);

	    if (viewId.isPresent()) {
		WebRequestTransformer.setView(viewId.get(), storageUri, discoveryMessage);
		discoveryMessage.setSources(ConfigurationWrapper.getViewSources(discoveryMessage.getView().get()));
	    }else {
		discoveryMessage.setSources(ConfigurationWrapper.getAllSources());		
	    }
	    
	    discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	    tmpResultSet = executor.retrieveNodes(discoveryMessage);

	    if (resultSet == null) {
		resultSet = tmpResultSet;
	    } else {
		resultSet.getResultsList().addAll(tmpResultSet.getResultsList());
	    }
	    page.setStart(page.getStart() + pageSize);

	} while (resultSet.getResultsList().size() < resultSet.getCountResponse().getCount() && !tmpResultSet.getResultsList().isEmpty());
	FeatureQueryResultSetFormatter formatter = new FeatureQueryResultSetFormatter();

	Response response = formatter.format(discoveryMessage, resultSet);

	return response;

    }

    protected static StorageInfo getStorageURI(DiscoveryMessage message) throws GSException {
	StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();
	if (storageUri != null) {

	    message.setDataBaseURI(storageUri);

	} else {

	    GSException exception = GSException.createException(FeatureQueryHandler.class, //
		    "Data Base storage URI not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_WARNING, //
		    "DB_STORAGE_URI_NOT_FOUND");

	    message.getException().getErrorInfoList().add(exception.getErrorInfoList().get(0));

	    GSLoggerFactory.getLogger(FeatureQueryHandler.class).warn("Data Base storage URI not found");
	}
	return storageUri;
    }
}
