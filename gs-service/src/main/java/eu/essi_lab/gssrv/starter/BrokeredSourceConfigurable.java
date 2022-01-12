package eu.essi_lab.gssrv.starter;

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

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.GSAccessor;
import eu.essi_lab.adk.IGSAccessorConfigurable;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.harvester.HarvesterFactory;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.Subcomponent;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionBrokeringStrategy;
import eu.essi_lab.model.configuration.option.GSConfOptionGSSource;
import eu.essi_lab.model.configuration.option.GSConfOptionSource;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrokeredSourceConfigurable extends AbstractGSconfigurableComposed {

    public static final String GS_BROKERING_STRATEGY_OPTION_KEY = "GS_BROKERING_STRATEGY_OPTION_KEY";
    public static final String GS_MIXED_BROKERING_STRATEGY_OPTION_KEY = "GS_MIXED_BROKERING_STRATEGY_OPTION_KEY";
    public static final String UNSUPPORTED_SOURCE_USER_MESSAGE = "The provided source seems to be unsuppported at the moment, please check the URL and type (if any).";
    private static final long serialVersionUID = 1034637682587059352L;
    private static final String ERR_ID_UNSUPPORTED_SOURCE = "ERR_ID_UNSUPPORTED_SOURCE";
    private static final String DISTRIBUTED_ACCESSOR_KEY = "brokered:distributed";
    private static final String HARVESTED_ACCESSOR_KEY = "brokered:harvested";
    private Map<String, GSConfOption<?>> supported = new HashMap<>();

    public BrokeredSourceConfigurable() {

	setLabel("Brokered GSSource Configuration");
	GSConfOptionGSSource sourceOption = new GSConfOptionGSSource();

	sourceOption.setKey(GSConfiguration.GS_SOURCE_OPTION_KEY);
	sourceOption.setLabel("GSSource");
	sourceOption.setMandatory(true);

	sourceOption.setValue(new GSSource());

	getSupportedOptions().put(GSConfiguration.GS_SOURCE_OPTION_KEY, sourceOption);
    }

    @Override
    public void onOptionSet(GSConfOption<?> option) throws GSException {

	if (option instanceof GSConfOptionSource) {

	    Source source = (Source) option.getValue();

	    IGSAccessorConfigurable accessor = createAccessor(source, new AccessorFactory());

	    if (accessor == null)
		throw createUnSupportedSourceMessage();

	    executeOnGSSourceOptionSet(accessor);

	}

	if (option instanceof GSConfOptionBrokeringStrategy) {

	    BrokeringStrategy strategy = ((GSConfOptionBrokeringStrategy) option).getValue();

	    Source source = ((GSConfOptionSource) getSupportedOptions().get(AccessorFactory.GS_SOURCE_OPTION_KEY)).getValue();

	    executeOnStartegyOptionSet(strategy, source);

	}

	if (option.getKey().equalsIgnoreCase(GS_MIXED_BROKERING_STRATEGY_OPTION_KEY)) {

	    Subcomponent subcomponent = ((GSConfOptionSubcomponent) option).getValue();

	    Source source = ((GSConfOptionSource) getSupportedOptions().get(AccessorFactory.GS_SOURCE_OPTION_KEY)).getValue();

	    executeOnMixedAccessorOptionSet(subcomponent, source);

	}

    }

    public void executeOnMixedAccessorOptionSet(Subcomponent subcomponent, Source source) {

	getConfigurableComponents().clear();

	String requestedMixedAccessorId = subcomponent.getValue();

	IGSAccessorConfigurable confAcc = createAccessor(source, new AccessorFactory());

	List<GSAccessor> mixedList = confAcc.getMixedAccessor();

	for (GSAccessor mixed : mixedList) {

	    if (mixed.getKey().equalsIgnoreCase(requestedMixedAccessorId)) {

		getConfigurableComponents().clear();

		IGSConfigurableComposed hconfigurable = HarvesterFactory.getInstance().getHarvesterConfigurable(mixed);

		getConfigurableComponents().put(hconfigurable.getKey(), hconfigurable);

		break;

	    }

	}

    }

    @Override
    public void onFlush() throws GSException {

	//nothing to do here

    }

    private GSException createUnSupportedSourceMessage() {

	GSException ex = new GSException();

	ErrorInfo ei = new ErrorInfo();

	ei.setSeverity(ErrorInfo.SEVERITY_WARNING);

	ei.setContextId(this.getClass().getName());
	ei.setErrorId(ERR_ID_UNSUPPORTED_SOURCE);

	ei.setUserErrorDescription(UNSUPPORTED_SOURCE_USER_MESSAGE);

	ex.addInfo(ei);

	return ex;
    }

    public void executeOnStartegyOptionSet(BrokeringStrategy strategy, Source source) throws GSException {

	IGSAccessorConfigurable confAcc = createAccessor(source, new AccessorFactory());

	switch (strategy) {

	case DISTRIBUTED:

	    getConfigurableComponents().clear();

	    IDistributedAccessor dist = confAcc.getDistributedAccessor();

	    dist.setKey(DISTRIBUTED_ACCESSOR_KEY);

	    getConfigurableComponents().put(dist.getKey(), dist);

	    break;
	case HARVESTED:

	    getConfigurableComponents().clear();

	    IHarvestedAccessor harv = confAcc.getHarvestedAccessor();

	    IGSConfigurableComposed hconfigurable = HarvesterFactory.getInstance().getHarvesterConfigurable(harv);

	    hconfigurable.setKey(HARVESTED_ACCESSOR_KEY);

	    getConfigurableComponents().put(hconfigurable.getKey(), hconfigurable);

	    break;

	case MIXED:

	    executeOnMixedStrategyOptionSet(confAcc.getMixedAccessor());

	    break;

	default:
	    break;
	}
    }

    public void executeOnMixedStrategyOptionSet(List<GSAccessor> mixedList) {

	GSConfOptionSubcomponent option = new GSConfOptionSubcomponent();

	option.setKey(GS_MIXED_BROKERING_STRATEGY_OPTION_KEY);
	option.setLabel("Select Accessor");
	option.setMandatory(true);

	for (GSAccessor acc : mixedList)
	    option.getAllowedValues().add(new Subcomponent(acc.getLabel(), acc.getKey()));

	getSupportedOptions().put(GS_MIXED_BROKERING_STRATEGY_OPTION_KEY, option);

    }

    public void executeOnGSSourceOptionSet(IGSAccessorConfigurable accessor) {

	GSConfOptionBrokeringStrategy strategy = new GSConfOptionBrokeringStrategy(accessor.getSupportedBrokeringStrategies());

	strategy.setKey(GS_BROKERING_STRATEGY_OPTION_KEY);
	strategy.setLabel("Brokering Strategy");
	strategy.setMandatory(true);

	getSupportedOptions().put(GS_BROKERING_STRATEGY_OPTION_KEY, strategy);

    }

    public IGSAccessorConfigurable createAccessor(Source source, AccessorFactory factory) {

	return factory.getConfigurableAccessor(source);

    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return supported;
    }

}
