package eu.essi_lab.configuration;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.essi_lab.configuration.reader.GSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.MetadataDataDabaseStoragiURI;
import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.UserJobResultsStorageURI;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
public class ConfigurationUtils {

    private static List<GSSource> allSources;
    private static StorageUri storageUri;
    private static SharedRepositoryInfo sharedRepositoryInfo;
    private static StorageUri userJobStorageUri;

    private ConfigurationUtils() {
    }

    /**
     * @return
     * @throws GSException
     */
    public static GSConfigurationReader createConfigurationReader() throws GSException {

	ConfigurationSync sync = ConfigurationSync.getInstance();

	return new GSConfigurationReader(sync.getConfiguration());
    }

    /**
     * This utility method can be used to implement the {@link #validate(WebRequest)} method. It checks whether a
     * {@link GSSource} with the
     * supplied <code>sourceIdentifier</code> exists
     *
     * @param sourceIdentifier the identifier of the {@link GSSource} to retrieve
     * @throws GSException if there is no {@link GSSource} with the supplied <code>sourceId</code> or if problem occur
     *         during the check
     */
    public static boolean checkSource(String sourceIdentifier) throws GSException {

	return getSource(sourceIdentifier) != null;
    }

    public static StorageUri getUserJobStorageURI() throws GSException {

	if (userJobStorageUri != null) {
	    return userJobStorageUri;
	}

	GSConfigurationReader reader = createConfigurationReader();
	List<UserJobResultsStorageURI> storageUris = reader.readInstantiableType(UserJobResultsStorageURI.class, new Deserializer());

	if (!storageUris.isEmpty()) {

	    return storageUris.get(0);
	}

	return null;

    }

    /**
     * Retrieves all the {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getAllSources() throws GSException {

	if (allSources != null) {

	    return allSources;
	}

	GSConfigurationReader reader = createConfigurationReader();

	return reader.readInstantiableType(GSSource.class, new Deserializer());
    }

    /**
     * Retrieves all the brokered {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getBrokeredSources() throws GSException {

	return getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.HARVESTED).//
		collect(Collectors.toList());
    }

    /**
     * Retrieves all the distributed {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getDistributedSources() throws GSException {

	return getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.DISTRIBUTED).//
		collect(Collectors.toList());
    }

    /**
     * @param sourceIdentifier
     * @return
     * @throws GSException
     */
    public static GSSource getSource(String sourceIdentifier) throws GSException {

	GSConfigurationReader reader = createConfigurationReader();

	GSSource s = reader.readInstantiableType(GSSource.class, sourceIdentifier, new Deserializer());

	return s;
    }

    /**
     * @return
     * @throws GSException
     */
    public static SharedRepositoryInfo getSharedRepositoryInfo() throws GSException {

	if (sharedRepositoryInfo != null) {

	    return sharedRepositoryInfo;
	}

	GSConfigurationReader reader = createConfigurationReader();

	List<SharedRepositoryInfo> repositoryInfos = reader.readInstantiableType(SharedRepositoryInfo.class, new Deserializer());

	if (!repositoryInfos.isEmpty()) {

	    return repositoryInfos.get(0);
	}

	return null;
    }

    /**
     * @return
     * @throws GSException
     */
    public static StorageUri getStorageURI() throws GSException {

	if (storageUri != null) {

	    return storageUri;
	}

	GSConfigurationReader reader = createConfigurationReader();
	GSConfiguration configuration = reader.getConfiguration();
	if (configuration == null) {
	    return null;
	}

	List<MetadataDataDabaseStoragiURI> storageUris = reader.readInstantiableType(MetadataDataDabaseStoragiURI.class,
		new Deserializer());

	if (!storageUris.isEmpty()) {

	    return storageUris.get(0);
	}

	return null;
    }

    /**
     * For test purposes
     * 
     * @param sources
     */
    public static void setSharedRepositoryInfo(SharedRepositoryInfo info) {

	sharedRepositoryInfo = info;
    }

    /**
     * For test purposes
     * 
     * @param sources
     */
    public static void setUserJobStorageUri(StorageUri uri) {

	userJobStorageUri = uri;
    }

    /**
     * For test purposes
     * 
     * @param sources
     */
    public static void setStorageUri(StorageUri uri) {

	storageUri = uri;
    }

    /**
     * For test purposes
     * 
     * @param sources
     */
    public static void setAllSources(List<GSSource> sources) {

	allSources = sources;
    }

    public static MailConfiguration getMailConfiguration() {
	MailConfiguration ret = new MailConfiguration();
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable emailComponent = components.get("E_MAIL");
	    Map<String, GSConfOption<?>> options = emailComponent.getSupportedOptions();
	    ret.seteMailRecipients(readOption(options, "E_MAIL_RECIPIENTS"));
	    ret.setSmtpPort(readOption(options, "SMTP_PORT"));
	    ret.setSmtpHost(readOption(options, "SMTP_HOST"));
	    ret.setSmtpUser(readOption(options, "SMTP_USER"));
	    ret.setSmtpPassword(readOption(options, "SMTP_PASSWORD"));

	} catch (GSException e) {
	    e.printStackTrace();
	}

	return ret;
    }

    public static String getGIProxyEndpoint() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable proxyComponent = components.get("GI_PROXY");
	    Map<String, GSConfOption<?>> options = proxyComponent.getSupportedOptions();
	    return readOption(options, "GI_PROXY_ENDPOINT");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getGIStatsEndpoint() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable proxyComponent = components.get("GI_STATS");
	    Map<String, GSConfOption<?>> options = proxyComponent.getSupportedOptions();
	    return readOption(options, "GI_STATS_ENDPOINT");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getGIStatsDbname() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable proxyComponent = components.get("GI_STATS");
	    Map<String, GSConfOption<?>> options = proxyComponent.getSupportedOptions();
	    return readOption(options, "GI_STATS_DB_NAME");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getGIStatsUser() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable proxyComponent = components.get("GI_STATS");
	    Map<String, GSConfOption<?>> options = proxyComponent.getSupportedOptions();
	    return readOption(options, "GI_STATS_USER");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getGIStatsPassword() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable proxyComponent = components.get("GI_STATS");
	    Map<String, GSConfOption<?>> options = proxyComponent.getSupportedOptions();
	    return readOption(options, "GI_STATS_PASSWORD");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getInumetUser() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "INUMET_USER");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
    

    
    public static String getInumetPassword() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "INUMET_PASSWORD");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    
    public static String getDinaguaUser() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "DINAGUA_USER");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    public static String getDinaguaUserToken() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "DINAGUA_USER_TOKEN");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    public static String getDinaguaPassword() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "DINAGUA_PASSWORD");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    public static String getNVEToken() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "NVE_TOKEN");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getSentinelDownloaderToken() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "SENTINEL_DOWNLOADER_TOKEN");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getSentinelUser() {
	try {
	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
	    return readOption(options, "SENTINEL_USER");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
    
    public static String getSentinelPassword() {
 	try {
 	    Map<String, IGSConfigurable> components = createConfigurationReader().getConfiguration().getConfigurableComponents();
 	    IGSConfigurable credentialComponent = components.get("CREDENTIALS");
 	    Map<String, GSConfOption<?>> options = credentialComponent.getSupportedOptions();
 	    return readOption(options, "SENTINEL_PASSWORD");
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}

 	return null;
     }

    private static String readOption(Map<String, GSConfOption<?>> options, String key) {
	GSConfOption<?> option = options.get(key);
	if (option != null) {
	    if (option instanceof GSConfOptionString) {
		GSConfOptionString str = (GSConfOptionString) option;
		return str.getValue();
	    }
	}
	return null;
    }
}
