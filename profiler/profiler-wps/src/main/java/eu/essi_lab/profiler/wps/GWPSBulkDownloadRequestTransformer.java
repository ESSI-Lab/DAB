package eu.essi_lab.profiler.wps;

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

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.w3c.dom.Node;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.BulkDownloadMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationException;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataReference;
import eu.essi_lab.model.resource.data.DataReferences;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class GWPSBulkDownloadRequestTransformer extends WebRequestTransformer<BulkDownloadMessage> {

    /**
     * 
     */
    private static final String GWPS_BULK_DOWNLOAD_EXECUTOR_VALIDATION_ERROR = "GWPS_BULK_DOWNLOAD_EXECUTOR_VALIDATION_ERROR";
    /**
     * 
     */
    private static final String GWPS_BULK_DOWNLOAD_EXECUTOR_REFINE_MESSAGE_ERROR = "GWPS_BULK_DOWNLOAD_EXECUTOR_REFINE_MESSAGE_ERROR";

    /**
     * @param url
     * @return
     */
    private boolean canConnect(String url) {

	try {
	    return HttpConnectionUtils.checkConnectivity(url);
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	try {
	    InputStream stream = request.getBodyStream().clone();
	    XMLDocumentReader reader = new XMLDocumentReader(stream);
	    stream.close();
	    Node[] nodes = reader.evaluateNodes("//*:Input");

	    ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	    IDiscoveryExecutor executor = loader.iterator().next();

	    TaskListExecutor<ValidationMessage> barrier = new TaskListExecutor<ValidationMessage>(nodes.length);
	    for (int i = 0; i < nodes.length; i++) {
		final Node node = nodes[i];
		barrier.addTask(new Callable<ValidationMessage>() {

		    @Override
		    public ValidationMessage call() throws Exception {
			ValidationMessage ret = new ValidationMessage();
			ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
			String title = reader.evaluateString(node, "*:Title");
			String linkage = reader.evaluateString(node, "*:Reference/@*:href");

			if (linkage.contains("gwps/dataset/")) {
			    String onlineId = linkage.substring(linkage.indexOf("gwps/dataset/"), linkage.indexOf("?"))
				    .replace("gwps/dataset/", "");
			    GSLoggerFactory.getLogger(getClass())
				    .info("Checking connectivity of online resource with id " + onlineId + " STARTED");

			    DiscoveryMessage message = new DiscoveryMessage();
			    message.setRequestId(request.getRequestId());
			    message.setUserBond(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, onlineId));

			    message.setQueryRegistrationEnabled(false);

			    message.setSources(ConfigurationWrapper.getAllSources());

			    Page page = new Page(1, 1);
			    message.setPage(page);

			    message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
			    message.getResourceSelector().setSubset(ResourceSubset.FULL);

			    message.setSources(ConfigurationWrapper.getHarvestedSources());
			    message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

			    GSLoggerFactory.getLogger(getClass()).info("Resource discovery STARTED");

			    ResultSet<GSResource> resultSet = executor.retrieve(message);

			    if (resultSet.getResultsList().isEmpty()) {
				ValidationException exception = new ValidationException();
				exception.setMessage("Unable to download resource: " + title);
				exception.setCode("ResourceNotFound");
				exception.setLocator(linkage);
				ret.addException(exception);
				ret.setResult(ValidationResult.VALIDATION_FAILED);
			    } else {

				GSResource resource = resultSet.getResultsList().get(0);
				DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);
				if (downloader == null) {
				    ValidationException exception = new ValidationException();
				    exception.setMessage("Unable to download resource: " + title);
				    exception.setCode("NoDataDownloaderAvailable");
				    exception.setLocator(linkage);
				    ret.addException(exception);
				    ret.setResult(ValidationResult.VALIDATION_FAILED);
				} else {

				    boolean connectivity = false;
				    try {
					connectivity = downloader.canConnect();
				    } catch (Exception e) {
					e.printStackTrace();
					GSLoggerFactory.getLogger(getClass()).error("Error checking connectivity: " + e.getMessage());
				    }
				    if (!connectivity) {
					ValidationException exception = new ValidationException();
					exception.setMessage("Unable to download resource: " + title);
					exception.setCode("ServiceNotReachable");
					exception.setLocator(linkage);
					ret.addException(exception);
					ret.setResult(ValidationResult.VALIDATION_FAILED);
				    }
				}
			    }

			 // worldcereal download
			} else if(linkage.contains("worldcereal/query")){		    
				return ret;
			} else {
			    // direct download

			    Downloader d = new Downloader();			    
			    if (linkage.contains("?")) {
				String base = linkage.substring(0, linkage.indexOf('?') + 1);
				boolean result = canConnect(base);
				if (result) {
				    return ret;
				}
			    }

			    boolean result = canConnect(linkage);
			    if (!result) {
				ValidationException exception = new ValidationException();
				exception.setMessage("Unable to download resource: " + title);
				exception.setCode("ServiceNotReachable");
				exception.setLocator(linkage);
				ret.addException(exception);
				ret.setResult(ValidationResult.VALIDATION_FAILED);
			    }
			}

			return ret;
		    }
		});
	    }

	    List<Future<ValidationMessage>> futures = barrier.executeAndWait(10); // waits maximum 10 seconds, because
										  // GEO portal will wait 15 seconds
	    ValidationMessage ret = new ValidationMessage();
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	    for (int i = 0; i < nodes.length; i++) {
		Node node = nodes[i];
		String title = reader.evaluateString(node, "*:Title");
		String linkage = reader.evaluateString(node, "*:Reference/@*:href");
		Future<ValidationMessage> future = futures.get(i);
		try {
		    ValidationMessage result = future.get();
		    if (result.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
			ret.setResult(ValidationResult.VALIDATION_FAILED);
			List<ValidationException> exceptions = result.getExceptions();
			if (exceptions != null) {
			    for (ValidationException exception : exceptions) {
				ret.addException(exception);
			    }
			}
		    }
		} catch (Exception e) {
		    ValidationException exception = new ValidationException();
		    exception.setMessage("Timeout during connectivity check for resource: " + title);
		    exception.setCode("TimeoutDuringCheck");
		    exception.setLocator(linkage);
		    ret.addException(exception);
		    ret.setResult(ValidationResult.VALIDATION_FAILED);
		}
	    }

	    return ret;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GWPS_BULK_DOWNLOAD_EXECUTOR_VALIDATION_ERROR);
	}
    }

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    @Override
    public String getProfilerType() {

	return null;
    }

    @Override
    protected BulkDownloadMessage refineMessage(BulkDownloadMessage message) throws GSException {

	DataReferences references = new DataReferences();

	try {
	    WebRequest request = message.getWebRequest();
	    InputStream stream = request.getBodyStream().clone();

	    XMLDocumentReader reader = new XMLDocumentReader(stream);
	    stream.close();
	    Node[] nodes = reader.evaluateNodes("//*:Input");

	    for (Node node : nodes) {

		DataReference reference = new DataReference();

		String title = reader.evaluateString(node, "*:Title");
		reference.setTitle(title);
		String linkage = reader.evaluateString(node, "*:Reference/@*:href");
		reference.setLinkage(linkage);

		references.addReference(reference);
	    }

	    message.setDataReferences(references);
	    message.setUserJobResultId("bulk-download-" + System.currentTimeMillis() + ".zip");

	    Boolean storeExecResponse = reader.evaluateString("//*:ResponseDocument/@storeExecuteResponse").equals("true");
	    message.setStoreExecuteResponse(storeExecResponse);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GWPS_BULK_DOWNLOAD_EXECUTOR_REFINE_MESSAGE_ERROR);
	}

	return message;
    }

    @Override
    protected BulkDownloadMessage createMessage() {

	BulkDownloadMessage ret = new BulkDownloadMessage();

	return ret;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return null;
    }
}
