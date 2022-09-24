package eu.essi_lab.gssrv.starter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cdk.IDriverConnector;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.ommdk.IResourceMapper;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.csw.profile.CSWProfile;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class ServiceLoaderChecker {
    private static final String ERR_ID_SERVICE_LOADER_CHECKER = "ERR_ID_SERVICE_LOADER_CHECKER";

    public ServiceLoaderChecker() throws GSException {
	GSLoggerFactory.getLogger(this.getClass()).info("Checking loading of common services.");
	checkService(IGSMainConfigurable.class);
	checkService(IResourceMapper.class);
	checkService(IHarvestedQueryConnector.class);
	checkService(IDriverConnector.class);
	checkService(IGSConfigurationStorage.class);
	checkService(DatabaseProvider.class);
	checkService(DatabaseReader.class);
	checkService(DatabaseWriter.class);
	checkService(SourceStorage.class);
	checkService(IDiscoveryExecutor.class);
	// checkService(igsconfiguration.class);
	checkService(Profiler.class);
	checkService(MessageResponseFormatter.class);
	checkService(DiscoveryRequestTransformer.class);
	checkService(CSWProfile.class);

    }

    private void checkService(Class<?> clazz) throws GSException {
	GSLoggerFactory.getLogger(this.getClass()).info("Checking service type: " + clazz.getCanonicalName());
	ServiceLoader<?> services = ServiceLoader.load(clazz);
	try {
	    for (Object service : services) {
		GSLoggerFactory.getLogger(this.getClass()).info(clazz.getSimpleName() + " " + service.getClass().getCanonicalName() + " loaded.");
	    }
	} catch (ServiceConfigurationError e) {
	    System.err.println(e.getMessage());
	    throw GSException.createException(this.getClass(), "Service Loader Checker", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_FATAL, ERR_ID_SERVICE_LOADER_CHECKER, e);
	}
	GSLoggerFactory.getLogger(this.getClass()).info("");

    }
}
