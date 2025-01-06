//package eu.essi_lab.profiler.esri.feature.identifiers;

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
//
//import java.io.ByteArrayInputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.ServiceLoader;
//
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import eu.essi_lab.configuration.ConfigurationUtils;
//import eu.essi_lab.lib.utils.ExpiringCache;
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.messages.DiscoveryMessage;
//import eu.essi_lab.messages.Page;
//import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
//import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
//import eu.essi_lab.messages.ResultSet;
//import eu.essi_lab.messages.ValidationMessage;
//import eu.essi_lab.messages.ValidationMessage.ValidationResult;
//import eu.essi_lab.messages.count.CountSet;
//import eu.essi_lab.messages.web.WebRequest;
//import eu.essi_lab.model.StorageUri;
//import eu.essi_lab.model.exceptions.ErrorInfo;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.model.resource.GSResource;
//import eu.essi_lab.model.resource.MetadataElement;
//import eu.essi_lab.pdk.handler.WebRequestHandler;
//import eu.essi_lab.pdk.validation.WebRequestValidator;
//import eu.essi_lab.pdk.wrt.WebRequestTransformer;
//import eu.essi_lab.profiler.esri.feature.ESRIFieldType;
//import eu.essi_lab.profiler.esri.feature.FeatureLayer;
//import eu.essi_lab.profiler.esri.feature.query.ESRIRequest;
//import eu.essi_lab.request.executor.IDiscoveryExecutor;
//
///**
// * @author boldrini
// */
//public class FeatureQueryIdentifiersHandler implements WebRequestHandler, WebRequestValidator {
//
//    public static final String BNHS_SEPARATOR = "\t";
//
//    private static ExpiringCache<JSONObject> cache = new ExpiringCache<JSONObject>();
//    static {
//	cache.setDuration(1000 * 60 * 20l);
//    }
//
//    public static final boolean FAST = true;
//
//    @Override
//    public synchronized Response handle(WebRequest request) throws GSException {
//	JSONObject response = new JSONObject();
//
//	String path = request.getServletRequest().getPathInfo();
//
//	String id = path.substring(path.indexOf("FeatureServer/"));
//	id = id.substring(id.indexOf("/") + 1);
//	id = id.substring(0, id.indexOf("/"));
//	FeatureLayer layer = FeatureLayer.getLayer(id);
//
//	if (layer == null) {
//	    JSONObject error = new JSONObject();
//	    error.put("code", 500);
//	    error.put("message", "Layer not found");
//	    JSONArray details = new JSONArray();
//	    error.put("details", details);
//	    response.put("error", error);
//	} else if (FAST) {
//	    // FAST
//
//	    response = new JSONObject();
//
//	    response.put("objectIdFieldName", layer.getField(ESRIFieldType.OID).getName());
//
//	    ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
//	    IDiscoveryExecutor executor = loader.iterator().next();
//
//	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
//	    discoveryMessage.setRequestId(request.getRequestId());
//
//	    discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
//	    discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);
//	    discoveryMessage.getResourceSelector().setIncludeOriginal(false);
//	    
//	    StorageUri storageUri = getStorageURI(discoveryMessage);
//
//	    Optional<String> viewId = request.extractViewId();
//
//	    if (viewId.isPresent()) {
//
//		WebRequestTransformer.setView(viewId.get(), storageUri, discoveryMessage);
//	    }
//
//	    discoveryMessage.setPage(new Page(1, 1));
//
//	    discoveryMessage.setSources(ConfigurationUtils.getAllSources());
//	    discoveryMessage.setDataBaseURI(ConfigurationUtils.getStorageURI());
//	    discoveryMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
//
//	    discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
//
//	    CountSet count = executor.count(discoveryMessage);
//
//	    JSONArray idArray = new JSONArray();
//
//	    for (int i = 0; i < count.getCount(); i++) {
//		idArray.put("" + i);
//	    }
//
//	    response.put("objectIds", idArray);
//
//	} else {
//
//	    // SLOW
//
//	    response = cache.get(path);
//
//	    if (response == null) {
//		response = new JSONObject();
//
//		ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
//		IDiscoveryExecutor executor = loader.iterator().next();
//
//		List<String> identifiers = new ArrayList<String>();
//
//		int total = 0;
//		int retrieved = 0;
//		int page = 0;
//		do {
//		    page++;
//		    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
//		    discoveryMessage.setRequestId(request.getRequestId());
//
//		    discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
//		    discoveryMessage.getResourceSelector().setSubset(ResourceSubset.NO_CORE);
//		    discoveryMessage.getResourceSelector().addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
//
//		    StorageUri storageUri = getStorageURI(discoveryMessage);
//
//		    Optional<String> viewId = request.extractViewId();
//
//		    if (viewId.isPresent()) {
//
//			WebRequestTransformer.setView(viewId.get(), storageUri, discoveryMessage);
//		    }
//
//		    discoveryMessage.setPage(new Page(1, 100));
//		    discoveryMessage.setIteratedWorkflow();
//
//		    discoveryMessage.setSources(ConfigurationUtils.getAllSources());
//		    discoveryMessage.setDataBaseURI(ConfigurationUtils.getStorageURI());
//		    discoveryMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
//
//		    discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
//
//		    ResultSet<GSResource> resourcesResults = executor.retrieve(discoveryMessage);
//		    total = resourcesResults.getCountResponse().getCount();
//		    List<GSResource> resources = resourcesResults.getResultsList();
//		    retrieved += resources.size();
//		    response.put("objectIdFieldName", layer.getField(ESRIFieldType.OID).getName());
//
//		    for (GSResource resource : resources) {
//
//			// Optional<String> optionalBNHS = resource.getExtensionHandler().getBNHSInfo();
//			// HashMap<String, String> bnhsMap = new HashMap<>();
//			// if (optionalBNHS.isPresent()) {
//			// String bnhs = optionalBNHS.get();
//			// String[] split = bnhs.split(BNHS_SEPARATOR);
//			// for (int i = 0; i < split.length - 2; i += 2) {
//			// String column = split[i];
//			// String value = split[i + 1];
//			// bnhsMap.put(column, value);
//			// }
//			// }
//
//			Optional<String> optionalId = resource.getExtensionHandler().getUniquePlatformIdentifier();
//			if (optionalId.isPresent()) {
//			    identifiers.add(optionalId.get());
//			}
//
//		    }
//
//		} while (retrieved < total && page < 200);
//
//		JSONArray idArray = new JSONArray();
//		for (String idd : identifiers) {
//		    idArray.put(idd);
//		}
//		response.put("objectIds", idArray);
//
//		cache.put(path, response);
//	    }
//
//	}
//
//	ESRIRequest esriRequest = new ESRIRequest(request);
//
//	String callback = esriRequest.getParameter("callback");
//
//	if (callback == null) {
//	    callback = "";
//	} else {
//	    callback = callback + "(";
//	}
//
//	String str = callback + response.toString();
//
//	if (!callback.isEmpty()) {
//	    str += ")";
//	}
//
//	ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
//	return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();
//
//    }
//
//    protected StorageUri getStorageURI(DiscoveryMessage message) throws GSException {
//	StorageUri storageUri = ConfigurationUtils.getStorageURI();
//	if (storageUri != null) {
//
//	    message.setDataBaseURI(storageUri);
//
//	} else {
//
//	    GSException exception = GSException.createException(getClass(), //
//		    "Data Base storage URI not found", //
//		    null, //
//		    ErrorInfo.ERRORTYPE_INTERNAL, //
//		    ErrorInfo.SEVERITY_WARNING, //
//		    "DB_STORAGE_URI_NOT_FOUND");
//
//	    message.getException().addInfo(exception.getErrorInfoList().get(0));
//
//	    GSLoggerFactory.getLogger(this.getClass()).warn("Data Base storage URI not found");
//	}
//	return storageUri;
//    }
//
//    @Override
//    public ValidationMessage validate(WebRequest request) throws GSException {
//	ValidationMessage ret = new ValidationMessage();
//	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
//	return ret;
//    }
//
//}
