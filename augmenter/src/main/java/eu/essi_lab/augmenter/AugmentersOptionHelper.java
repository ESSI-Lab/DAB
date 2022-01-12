package eu.essi_lab.augmenter;

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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SourcesOptionHelper;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.Subcomponent;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponentList;
import eu.essi_lab.model.configuration.option.SubComponentList;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class AugmentersOptionHelper {

    private static final String AUGMENTERS_OPTION_KEY = "AUGMENTERS_OPTION_KEY";
    private static final String AUGMENTER_CREATION_ERROR = "AUGMENTER_CREATION_ERROR";

    private AbstractGSconfigurableComposed configurable;
    private SourcesOptionHelper sourcesHelper;

    /**
     * Creates a new instance of AugmentersListHelper working on the supplied <code>configurable</code>
     *
     * @param configurable
     */
    public AugmentersOptionHelper(AbstractGSconfigurableComposed configurable) {

	this.configurable = configurable;
    }

    /**
     * Creates a new instance of AugmentersListHelper working on the supplied <code>configurable</code> and
     * <code>sourcesHelper</code> and
     *
     * @param configurable
     * @param sourcesHelper
     */
    public AugmentersOptionHelper(AbstractGSconfigurableComposed configurable, SourcesOptionHelper sourcesHelper) {

	this.configurable = configurable;
	this.sourcesHelper = sourcesHelper;
    }

    /**
     * Put in the supplied {@link AbstractGSconfigurableComposed} a {@link GSConfOptionSubcomponentList} having as allowed value a {@link
     * SubComponentList} where each component is given by an {@link Augmenter} label/augmenter class, while the value is an empty list (no
     * Augmenter is pre-selected)
     *
     * @throws GSException
     */
    public void putAugmenterOption() {

	ServiceLoader<Augmenter> loader = ServiceLoader.load(Augmenter.class);

	GSConfOptionSubcomponentList option = new GSConfOptionSubcomponentList();

	option.setLabel("Select augmenter");
	option.setKey(AUGMENTERS_OPTION_KEY);

	SubComponentList subList = new SubComponentList();

	for (Augmenter augmenter : loader) {

	    Subcomponent subcomponent = new Subcomponent();
	    subcomponent.setLabel(augmenter.getLabel());
	    subcomponent.setValue(augmenter.getKey());

	    subList.add(subcomponent);
	}

	option.setEmptyList(); // set an empty list as value
	option.getAllowedValues().add(subList);// set only allowed values

	configurable.getSupportedOptions().put(option.getKey(), option);
    }

    /**
     * @param opt
     * @return
     */
    public static boolean isAugmentersOptionSet(GSConfOption<?> opt) {

	return opt.getKey().equals(AugmentersOptionHelper.AUGMENTERS_OPTION_KEY);
    }

    /**
     * This method must be called inside the {@link AbstractGSconfigurableComposed#onOptionSet(GSConfOption)} method if the method {@link
     * #isAugmentersOptionSet(GSConfOption)} returns <code>true</code>.<br> Puts in the supplied {@link AbstractGSconfigurableComposed} the
     * configurable components related to the selected {@link Augmenter}/s
     *
     * @param opt
     */
    public void handleOnOptionSet(GSConfOption<?> opt) {

	handleOnOptionSet(opt, null);
    }

    /**
     * This method must be called inside the {@link AbstractGSconfigurableComposed#onOptionSet(GSConfOption)} method if the method {@link
     * #isAugmentersOptionSet(GSConfOption)} returns <code>true</code>.<br> This method must be called only if the constructor {@link
     * #AugmentersOptionHelper(AbstractGSconfigurableComposed, SourcesOptionHelper)} has been used, and needs to provide also the list of
     * {@link GSSource} to handle.<br> Puts in the supplied {@link AbstractGSconfigurableComposed} the configurable components related to
     * the selected {@link GSSource}/s and {@link Augmenter}/s
     *
     * @param opt
     * @param sources
     */
    public void handleOnOptionSet(GSConfOption<?> opt, List<GSSource> sources) {

	GSConfOptionSubcomponentList option = (GSConfOptionSubcomponentList) opt;

	SubComponentList subComponentList = option.getValue();
	List<Subcomponent> list = subComponentList.getList();

	if (sourcesHelper != null) {

	    if (configurable.getSupportedOptions().get(SourcesOptionHelper.SOURCES_OPTION_KEY) == null) {

		sourcesHelper.putSourcesOption(sources);

	    } else if (list.isEmpty()) {

		sourcesHelper.clear();
	    }
	}

	ArrayList<String> selectedAugmentersKeys = Lists.newArrayList();

	for (Subcomponent subcomponent : list) {

	    String augmenterClass = subcomponent.getValue();

	    try {
		Augmenter augmenter = (Augmenter) Class.forName(augmenterClass).newInstance();

		selectedAugmentersKeys.add(augmenter.getKey());

		if (!configurable.getConfigurableComponents().containsKey(augmenter.getKey())) {

		    augmenter.initPriorityOption();

		    configurable.getConfigurableComponents().put(//
			    augmenter.getKey(), //
			    getComponent(augmenter));
		}

	    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

		GSLoggerFactory.getLogger(getClass()).error("Can't add {}", subcomponent.getLabel(), e);

		GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			AUGMENTER_CREATION_ERROR, //
			e);
	    }
	}

	// --------------------------------------------------------------------------
	//
	// removes from the configurable components the augmenters that are no longer
	// checked by the conf client widget (augmentersKeys has only the keys of the checked augmenters)
	//
	ServiceLoader<Augmenter> loader = ServiceLoader.load(Augmenter.class);

	List<String> allAugmentersKeys = StreamUtils.iteratorToStream(loader.iterator()).//
		map(a -> a.getKey()).//
		collect(Collectors.toList());

	List<String> currentAugmentersKeys = configurable.//
		getConfigurableComponents().//
		keySet().//
		stream().//
		filter(key -> allAugmentersKeys.contains(key)).//
		collect(Collectors.toList());

	currentAugmentersKeys.removeAll(selectedAugmentersKeys);

	currentAugmentersKeys.forEach(key -> configurable.getConfigurableComponents().remove(key));
    }

    /**
     * @param augmenter
     * @return
     */
    protected AbstractGSconfigurable getComponent(Augmenter augmenter) {

	return augmenter;
    }
}
