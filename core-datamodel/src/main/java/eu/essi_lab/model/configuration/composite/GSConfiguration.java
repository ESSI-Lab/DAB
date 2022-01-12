package eu.essi_lab.model.configuration.composite;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;

public class GSConfiguration extends AbstractGSconfigurableComposed implements IGSConfigurableComposed {

    public static final String GS_ROOT_USER_OPTION_KEY = "GS_ROOT_USER_OPTION_KEY";
    public static final String BROKERED_SOURCES_KEY = "BROKERED_SOURCES_KEY";
    public static final String DATABASE_MAIN_COMPONENT_KEY = "DATABASE_MAIN_COMPONENT_KEY";
    public static final String BATCH_JOBS_KEY = "BATCH_JOBS_KEY";
    public static final String PROFILERS_KEY = "PROFILERS_KEY";
    public static final String GS_SOURCE_OPTION_KEY = "GS_SOURCE_OPTION_KEY";
    public static final String QUARTZ_MAIN_COMPONENT_KEY = "QUARTZ_MAIN_COMPONENT_KEY";
    private static final String GS_TITLE_KEY = "GS_TITLE_KEY";
    public static final String HARVESTED_ACCESSOR_KEY = "brokered:harvested";
    public static final String HARVESTER_JOB_KEY = "harvesterJob_";

    long timeStamp;    private static final long serialVersionUID = 7099472406275682768L;
    private static final Object START_DATE_KEY = "START_DATE_KEY";
    private static final String POSPONED_DATE = "6666-06-06T06:06:06Z";
    private Map<String, GSConfOption<?>> supportedOptions = new HashMap<>();

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public GSConfiguration() {

	super();

	ServiceLoader<IGSMainConfigurable> mainConfigurables = ServiceLoader.load(IGSMainConfigurable.class);

	for (IGSMainConfigurable mc : mainConfigurables)
	    getConfigurableComponents().put(mc.getKey(), mc);

    }

    /**
     * @return
     */
    public static GSConfiguration createDefaultConfiguration() {

	GSConfiguration conf = new GSConfiguration();

	conf.setKey("gi-suite-configuration");

	GSConfOptionString opt = new GSConfOptionString();

	opt.setKey(GSConfiguration.GS_ROOT_USER_OPTION_KEY);
	opt.setMandatory(true);
	opt.setLabel("Root User");

	GSConfOptionString title = new GSConfOptionString();

	title.setKey(GS_TITLE_KEY);
	title.setMandatory(false);
	title.setLabel("GI-suite Title");

	conf.getSupportedOptions().put(title.getKey(), title);
	conf.getSupportedOptions().put(opt.getKey(), opt);

	GSConfOptionString defTitle = new GSConfOptionString();

	defTitle.setKey(GS_TITLE_KEY);
	try {
	    defTitle.setValue("GI-suite Brokering Framework");
	    defTitle.validate();
	    conf.setOption(defTitle);

	} catch (GSException e) {
	    GSLoggerFactory.getLogger(GSConfiguration.class).error(e.getMessage(), e);
	}

	return conf;

    }

    public void setTimeStamp(long ts) {

	timeStamp = ts;

    }

    public long getTimeStamp() {
	return timeStamp;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) {
	// Nothing to do on set option action
    }

    private void onFlushPrivate() {
	// Nothing to do on flush action
    }

    /**
     * Invokes the onFlush on each configurable in this configuration. The configuration tree is navigated according to
     * the following
     * strategy.
     * <ul>
     * <li>1) All first level configurables are loaded.
     * <li>2) For each configurable: first the onFlush is invoked then if the configurable has children.
     * <ul>
     * <li>2.1) then all its children are loaded and step 1) recursively executed.
     * <li>2.2) else terminate.
     * </ul>
     * </ul>
     * This navigationstratety is tested in GSConfigurationTest.testOnFlushInvokation
     */
    @Override
    public void onFlush() throws GSException {

	onFlushPrivate();

	onFlush(getConfigurableComponents());

    }

    @Override
    public void onStartUp() throws GSException {

	onStartUp(getConfigurableComponents());

    }

    public void onStartUp(Map<String, IGSConfigurable> configurables) throws GSException {
	Iterator<IGSConfigurable> it = configurables.values().iterator();

	while (it.hasNext()) {
	    IGSConfigurable next = it.next();

	    // logger.debug("onStartUp on configurable {}", next);

	    next.onStartUp();

	    if (IGSConfigurableComposed.class.isAssignableFrom(next.getClass())) {

		IGSConfigurableComposed composed = (IGSConfigurableComposed) next;

		onStartUp(composed.getConfigurableComponents());

	    }

	}
    }

    private void onFlush(Map<String, IGSConfigurable> configurables) throws GSException {

	Iterator<IGSConfigurable> it = configurables.values().iterator();

	while (it.hasNext()) {
	    IGSConfigurable next = it.next();

	    logger.debug("onFlush on configurable {}", next);

	    next.onFlush();

	    if (IGSConfigurableComposed.class.isAssignableFrom(next.getClass())) {

		IGSConfigurableComposed composed = (IGSConfigurableComposed) next;

		onFlush(composed.getConfigurableComponents());

	    }

	}

    }

    /**
     * @return
     */
    public Optional<String> readAdminIdentifier() {

	return Optional.ofNullable(getSupportedOptions().get(GS_ROOT_USER_OPTION_KEY).getValue().toString());
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return supportedOptions;
    }

    @JsonIgnore
    public File writeToFile() throws IOException, GSException {
	File file = File.createTempFile(getClass().getSimpleName(), ".json");
	writeToFile(file);
	return file;

    }

    @JsonIgnore
    public void writeToFile(File file) throws IOException, GSException {
	InputStream stream = serializeToInputStream();
	FileOutputStream fos = new FileOutputStream(file);
	IOUtils.copy(stream, fos);
	fos.close();
	stream.close();

	String info = "Configuration written to " + file.getAbsolutePath();
	GSLoggerFactory.getLogger(getClass()).info(info);

    }

    /**
     * Modifies the configuration to remove the quartz component
     */
    @JsonIgnore
    public void removeQuartzComponent() {
	IGSConfigurable quartz = getConfigurableComponents().get(QUARTZ_MAIN_COMPONENT_KEY);
	if (quartz != null) {
	    setTimeStamp(new Date().getTime());
	    getConfigurableComponents().remove(QUARTZ_MAIN_COMPONENT_KEY);
	    GSLoggerFactory.getLogger(getClass()).info("Removed quartz component");

	} else {
	    GSLoggerFactory.getLogger(getClass()).info("No quartz component found: nothing to remove");
	}

    }

    /**
     * Modifies the configuration to remove the quartz component
     */
    @JsonIgnore
    public void removeEmailSettings() {
	getConfigurableComponents().remove("E_MAIL");
	GSLoggerFactory.getLogger(getClass()).info("Removed email settings");
    }

    /**
     * Modifies the configuration posponing all the planned harvesting to a very future date
     * 
     * @return the number of harvesting jobs that have been posponed
     */
    @JsonIgnore
    public int posponeHarvesting() {
	GSLoggerFactory.getLogger(getClass()).info("Scanning sources to pospone harvesting");
	IGSConfigurable brokeredSources = getConfigurableComponents().get(BROKERED_SOURCES_KEY);
	int i = 0;
	if (brokeredSources != null) {
	    IGSConfigurableComposed brokeredSourcesComposed = (IGSConfigurableComposed) brokeredSources;
	    Collection<IGSConfigurable> sources = brokeredSourcesComposed.getConfigurableComponents().values();
	    for (IGSConfigurable source : sources) {
		IGSConfigurable harvestedAccessor = ((IGSConfigurableComposed) source).getConfigurableComponents()
			.get(HARVESTED_ACCESSOR_KEY);
		if (harvestedAccessor != null) {
		    IGSConfigurableComposed harvestedAccessorComposed = (IGSConfigurableComposed) harvestedAccessor;
		    Set<Entry<String, IGSConfigurable>> harvestedAccessorComponents = harvestedAccessorComposed.getConfigurableComponents()
			    .entrySet();
		    for (Entry<String, IGSConfigurable> harvestedAccessorComponent : harvestedAccessorComponents) {
			if (harvestedAccessorComponent.getKey().startsWith(HARVESTER_JOB_KEY)) {
			    IGSConfigurableComposed harvesterJobComposed = (IGSConfigurableComposed) harvestedAccessorComponent.getValue();
			    GSConfOption<Date> option = (GSConfOption<Date>) harvesterJobComposed.getSupportedOptions().get(START_DATE_KEY);
			    Date currentDate = option.getValue();
			    Optional<Date> optionalPosponedDate = ISO8601DateTimeUtils.parseISO8601ToDate(POSPONED_DATE);
			    if (optionalPosponedDate.isPresent()
				    && (currentDate == null || !currentDate.equals(optionalPosponedDate.get()))) {
				option.setValue(optionalPosponedDate.get());
				String info = "Posponed harvesting of harvest job: " + harvestedAccessorComponent.getKey();
				GSLoggerFactory.getLogger(getClass()).info(info);
				i++;
			    }
			}
		    }
		}

	    }
	}
	if (i == 0) {
	    GSLoggerFactory.getLogger(getClass()).warn("No harvesting job planned: nothing to pospone!");
	} else {
	    setTimeStamp(new Date().getTime());
	    String info = "Posponed " + i + " jobs";
	    GSLoggerFactory.getLogger(getClass()).info(info);
	}

	return i;

    }

    @JsonIgnore
    public IGSConfigurableComposed getSources() {
	IGSConfigurable brokeredSources = getConfigurableComponents().get(BROKERED_SOURCES_KEY);
	if (brokeredSources != null) {
	    return (IGSConfigurableComposed) brokeredSources;
	}
	return null;
    }

    @JsonIgnore
    public void setSources(IGSConfigurableComposed sources) {
	getConfigurableComponents().put(BROKERED_SOURCES_KEY, sources);
	setTimeStamp(new Date().getTime());
    }

    public void removeBatchJobs() {
	IGSConfigurable jobs = getConfigurableComponents().get(BATCH_JOBS_KEY);
	if (jobs != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Removing batch jobs");
	    IGSConfigurableComposed jobsComposed = (IGSConfigurableComposed) jobs;
	    jobsComposed.getConfigurableComponents().clear();
	    setTimeStamp(new Date().getTime());
	}

    }

    @JsonIgnore
    public HashMap<String, Date> getPlannedHarvests() {
	logger.info("Scanning sources to get planned harvests");
	HashMap<String, Date> ret = new HashMap<>();
	IGSConfigurable brokeredSources = getConfigurableComponents().get(BROKERED_SOURCES_KEY);
	if (brokeredSources != null) {
	    IGSConfigurableComposed brokeredSourcesComposed = (IGSConfigurableComposed) brokeredSources;
	    Collection<IGSConfigurable> sources = brokeredSourcesComposed.getConfigurableComponents().values();
	    for (IGSConfigurable source : sources) {
		IGSConfigurable harvestedAccessor = ((IGSConfigurableComposed) source).getConfigurableComponents()
			.get(HARVESTED_ACCESSOR_KEY);
		if (harvestedAccessor != null) {
		    IGSConfigurableComposed harvestedAccessorComposed = (IGSConfigurableComposed) harvestedAccessor;
		    Set<Entry<String, IGSConfigurable>> harvestedAccessorComponents = harvestedAccessorComposed.getConfigurableComponents()
			    .entrySet();
		    for (Entry<String, IGSConfigurable> harvestedAccessorComponent : harvestedAccessorComponents) {
			if (harvestedAccessorComponent.getKey().startsWith(HARVESTER_JOB_KEY)) {
			    IGSConfigurableComposed harvesterJobComposed = (IGSConfigurableComposed) harvestedAccessorComponent.getValue();
			    GSConfOption<Date> option = (GSConfOption<Date>) harvesterJobComposed.getSupportedOptions().get(START_DATE_KEY);
			    Date currentDate = option.getValue();
			    String id = source.getKey().substring(0, source.getKey().lastIndexOf(':'));
			    ret.put(id, currentDate);
			}
		    }
		}

	    }
	}
	return ret;
    }

    public void printIdentifiers() {
	IGSConfigurableComposed sources = getSources();
	Map<String, IGSConfigurable> components = sources.getConfigurableComponents();
	Set<Entry<String, IGSConfigurable>> entries = components.entrySet();
	HashMap<String, String> ids= new HashMap<>();
	for (Entry<String, IGSConfigurable> entry : entries) {
	    String key = entry.getKey();
	    IGSConfigurable value = entry.getValue();
	    GSConfOption<?> sourceOption = value.getSupportedOptions().get("GS_SOURCE_OPTION_KEY");
	    Object source = sourceOption.getValue();
	    String label = value.getLabel();
	    if (source instanceof Source) {
		Source s = (Source) source;
		label = s.getLabel();
	    }	    
	    key  =key.substring(0,key.indexOf(":"));
	    ids.put(label, key);
	}
	
	InputStream geossLabels = GSConfiguration.class.getClassLoader().getResourceAsStream("geoss/labels");
	String string;
	try {
	    string = IOUtils.toString(geossLabels,StandardCharsets.UTF_8);
	    String[] split = string.split("\n");
		for (String s : split) {
		    String id = ids.get(s);
		    if (id==null) {
			id = "";
		    }
		    System.out.println(id);
		}

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	
    }

}
