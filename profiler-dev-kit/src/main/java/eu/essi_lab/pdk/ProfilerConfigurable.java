package eu.essi_lab.pdk;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
public class ProfilerConfigurable extends AbstractGSconfigurable {

    /**
     *
     */
    private static final long serialVersionUID = 3936592994628159714L;

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    private static final String NAME_OPTION_KEY = "NAME_OPTION_KEY";
    private static final String PATH_OPTION_KEY = "PATH_OPTION_KEY";
    private static final String TYPE_OPTION_KEY = "TYPE_OPTION_KEY";
    private static final String VERSION_OPTION_KEY = "VERSION_OPTION_KEY";
    private static final String ENABLED_OPTION_KEY = "ENABLED_OPTION_KEY";

    public ProfilerConfigurable() {
    }

    public ProfilerConfigurable(Profiler profiler) {

	ProfilerInfo info = profiler.getProfilerInfo();

	setKey(profiler.getClass().getCanonicalName());

	String name = info.getServiceName();

	GSConfOptionString nameOption = new GSConfOptionString();
	nameOption.setKey(NAME_OPTION_KEY);
	nameOption.setLabel("Name");
	nameOption.setValue(name);

	getSupportedOptions().put(nameOption.getKey(), nameOption);

	String serviceType = info.getServiceType();

	GSConfOptionString typeOption = new GSConfOptionString();
	typeOption.setKey(TYPE_OPTION_KEY);
	typeOption.setLabel("Type");
	typeOption.setValue(serviceType);

	getSupportedOptions().put(typeOption.getKey(), typeOption);

	String path = info.getServicePath();

	GSConfOptionString pathOption = new GSConfOptionString();
	pathOption.setKey(PATH_OPTION_KEY);
	pathOption.setLabel("Path");
	pathOption.setValue(path);

	getSupportedOptions().put(pathOption.getKey(), pathOption);

	String version = info.getServiceVersion();

	GSConfOptionString versionOption = new GSConfOptionString();
	versionOption.setKey(VERSION_OPTION_KEY);
	versionOption.setLabel("Version");
	versionOption.setValue(version);

	getSupportedOptions().put(versionOption.getKey(), versionOption);

	GSConfOptionBoolean enabledOption = new GSConfOptionBoolean();
	enabledOption.setKey(ENABLED_OPTION_KEY);
	enabledOption.setLabel("Enabled");
	enabledOption.setValue(true);

	getSupportedOptions().put(enabledOption.getKey(), enabledOption);
    }

    @JsonIgnore
    public String getProfilerPath() {

	Optional<GSConfOption<?>> first = getSupportedOptions().//
		values().//
		stream().//
		filter(o -> o.getKey().equals(PATH_OPTION_KEY)).//
		findFirst();

	if (first.isPresent())
	    return first.get().getValue().toString();

	return null;
    }

    @JsonIgnore
    public boolean isProfilerEnabled() {

	Optional<GSConfOption<?>> first = getSupportedOptions().//
		values().//
		stream().//
		filter(o -> o.getKey().equals(ENABLED_OPTION_KEY)).//
		findFirst();

	if (first.isPresent())
	    return Boolean.valueOf(first.get().getValue().toString());

	return false;

    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	//nothing to do here
    }

    @Override
    public void onFlush() throws GSException {
	//nothing to do here
    }

}
