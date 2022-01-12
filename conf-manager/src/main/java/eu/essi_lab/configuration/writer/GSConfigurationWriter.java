package eu.essi_lab.configuration.writer;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Date;

import org.slf4j.Logger;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.configuration.ConfigurableKey;
import eu.essi_lab.configuration.IGSConfigurationReader;
import eu.essi_lab.configuration.IGSConfigurationWriter;
import eu.essi_lab.configuration.sync.IConfigurationSync;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class GSConfigurationWriter implements IGSConfigurationWriter {

    public static final String ERR_ID_NO_DB_WRITER = "ERR_ID_NO_DB_WRITER";
    public static final String ERR_ID_NO_CONF_READER = "ERR_ID_NO_CONF_READER";
    public static final String ERR_ID_CONFIGURATION_CONFLICT = "ERR_ID_CONFIGURATION_CONFLICT";
    public static final String ERR_ID_UNKNOWN_OPTION_KEY = "ERR_ID_UNKNOWN_OPTION_KEY";
    private static final String ERR_ID_NO_CONF_SYNC = "ERR_ID_NO_CONF_SYNC";
    private static final String ERR_ID_UNKNOWN_COMPONENT = "ERR_ID_UNKNOWN_COMPONENT";
    private static final String ERR_ID_SOURCE_ID_ALREADY_EXISTS = "ERR_ID_SOURCE_ID_ALREADY_EXISTS";

    private IGSConfigurationReader reader;
    private IGSConfigurationStorage dbwriter;
    private IConfigurationSync sync;

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public GSConfigurationWriter(IConfigurationSync s, IGSConfigurationReader r) throws GSException {

	sync = s;

	if (sync == null) {

	    GSException e = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorDescription("No Configuration Sync is provided");
	    ei.setErrorId(ERR_ID_NO_CONF_SYNC);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorCorrection("Provide a non-null IConfigurationSync implementation");
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    e.addInfo(ei);

	    throw e;
	}

	dbwriter = s.getDBGISuiteConfiguration();

	if (dbwriter == null) {

	    GSException e = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorDescription("No DB writer is provided");
	    ei.setErrorId(ERR_ID_NO_DB_WRITER);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorCorrection("Provide a non-null DB writer");
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    e.addInfo(ei);

	    throw e;
	}

	if (r == null) {

	    GSException e = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorDescription("No configuraiton reader is provided");
	    ei.setErrorId(ERR_ID_NO_CONF_READER);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorCorrection("Provide a non-null DB writer");
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    e.addInfo(ei);

	    throw e;
	}

	reader = r;

    }

    public GSConfigurationWriter(GSConfiguration c, IGSConfigurationReader r) throws GSException {

	dbwriter = new IGSConfigurationStorage() {

	    @Override
	    public boolean validate(StorageUri url) {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public void transactionUpdate(GSConfiguration conf) throws GSException {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public boolean supports(StorageUri url) {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public void setStorageUri(StorageUri url) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public GSConfiguration read() throws GSException {

		return c;
	    }

	    @Override
	    public StorageUri getStorageUri() {
		// TODO Auto-generated method stub
		return null;
	    }
	};

	if (r == null) {

	    GSException e = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorDescription("No configuraiton reader is provided");
	    ei.setErrorId(ERR_ID_NO_CONF_READER);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorCorrection("Provide a non-null DB writer");
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    e.addInfo(ei);

	    throw e;
	}

	reader = r;

    }

    @Override
    public void flush() throws GSException {

	logger.debug("Start flushing configuration from reader [reader class{}]", reader.getClass().getName());

	flush(reader.getConfiguration());

	logger.debug("Done flushing configuration from reader");

    }

    @Override
    public void flush(GSConfiguration configuration) throws GSException {

	logger.debug("Fetching configuration from sync");

	GSConfiguration remoteConf = sync.fetchRemote();

	logger.debug("Remote timestamp   [{}]: {}", remoteConf.getTimeStamp(),
		ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(remoteConf.getTimeStamp())));

	logger.debug("To flush timestamp [{}]: {}", configuration.getTimeStamp(),
		ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(configuration.getTimeStamp())));

	if (remoteConf.getTimeStamp() > configuration.getTimeStamp()) {

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_CONFIGURATION_CONFLICT);
	    ei.setErrorDescription("Remote Configuration has time stamp > local configuration");
	    ei.setUserErrorDescription("A conflict was detected while updating configuration (a more recent configuration was found).");
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	    GSException ex = new GSException();
	    ex.addInfo(ei);

	    throw ex;

	}

	configuration.setTimeStamp(System.currentTimeMillis());

	try {

	    dbwriter.transactionUpdate(configuration);

	} catch (GSException gsex) {

	    ErrorInfo ei = new ErrorInfo();

	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_SERVICE);

	    gsex.addInfo(ei);

	    throw gsex;

	}

    }

    @Override
    public IGSConfigurable addSource(Source source) throws GSException {

	Source existing = reader.readSource(source.getUniqueIdentifier());

	if (existing != null) {

	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();
	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_SOURCE_ID_ALREADY_EXISTS);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    ei.setErrorDescription(
		    "A source with identifier " + source.getUniqueIdentifier() + " already exists (" + existing.getLabel() + ")");

	    ex.addInfo(ei);

	    throw ex;
	}

	GSConfOptionSource sourceOption = new GSConfOptionSource();
	sourceOption.setKey(GSConfiguration.GS_SOURCE_OPTION_KEY);

	sourceOption.setValue(source);

	ConfigurableKey k = new ConfigurableKey();

	k.setOnRoot();

	k.addLevel(GSConfiguration.BROKERED_SOURCES_KEY);

	IGSConfigurable component = reader.readComponent(k);

	component.onOptionSet(sourceOption);

	return component;

    }

    @Override
    public IGSConfigurable setOption(ConfigurableKey componentKey, GSConfOption<?> option) throws GSException {

	IGSConfigurable component = reader.readComponent(componentKey);

	if (component == null) {
	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();
	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_UNKNOWN_COMPONENT);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    ei.setErrorDescription("Cannot find component with id " + componentKey);

	    ex.addInfo(ei);

	    throw ex;
	}

	GSConfOption existingOpt = reader.readOption(component, option.getKey());

	if (existingOpt != null) {
	    try {
		option.validate();
		existingOpt.setValue(option.getValue());

	    } catch (GSException ex) {

		ErrorInfo ei = new ErrorInfo();

		ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

		ex.addInfo(ei);

		throw ex;

	    }
	} else {

	    component.setOption(option);

	}

	component.onOptionSet(option);

	return component;

    }
}
