package eu.essi_lab.adk;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.configuration.GSSourceAccessor;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
public class GSAccessor extends AbstractGSconfigurableComposed implements IGSAccessorConfigurable {

    /**
     *
     */
    private static final long serialVersionUID = 2972611622881124298L;

    @JsonIgnore
    private transient IHarvestedAccessor oai;

    @JsonIgnore
    private transient IDistributedAccessor distributed;

    @JsonIgnore
    private transient List<GSAccessor> mixedAccessors;

    private static final String OAI_ACCESSOR_KEY = "OAI_ACCESSOR_KEY";

    private static final String DISTRIBUTED_ACCESSOR_KEY = "DISTRIBUTED_ACCESSOR_KEY";

    private static final String MIXED_ACCESSOR_KEY = "MIXED_ACCESSOR_KEY";

    private Map<String, GSConfOption<?>> accessorOptions = new HashMap<>();

    public GSAccessor() {
	//nothing to do here
    }

    @JsonIgnore
    public void setOAIPMH(IHarvestedAccessor accessor) {

	oai = accessor;

	oai.setKey(OAI_ACCESSOR_KEY);

	getConfigurableComponents().put(oai.getKey(), oai);

    }

    @JsonIgnore
    public void setMixed(List<GSAccessor> mixed) {
	mixedAccessors = mixed;

	int i = 0;

	for (GSAccessor acc : mixedAccessors) {

	    acc.setKey(MIXED_ACCESSOR_KEY + ":" + i + ":" + acc.getKey());

	    getConfigurableComponents().put(acc.getKey(), acc);

	    i++;
	}
    }

    @JsonIgnore
    public void setDistributed(IDistributedAccessor accessor) {

	distributed = accessor;

	distributed.setKey(DISTRIBUTED_ACCESSOR_KEY);

	getConfigurableComponents().put(distributed.getKey(), distributed);

    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return accessorOptions;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	//nothing to do here
    }

    @Override
    public void onFlush() throws GSException {
	//nothing to do here
    }

    @Override
    public List<BrokeringStrategy> getSupportedBrokeringStrategies() {

	List<BrokeringStrategy> list = new ArrayList<>();

	if (distributed != null)
	    list.add(BrokeringStrategy.DISTRIBUTED);

	if (oai != null)
	    list.add(BrokeringStrategy.HARVESTED);

	if (mixedAccessors != null && !mixedAccessors.isEmpty())
	    list.add(BrokeringStrategy.MIXED);

	return list;
    }

    @Override
    @JsonIgnore
    public IDistributedAccessor getDistributedAccessor() {

	if (distributed == null && getConfigurableComponents() != null) {

	    for (IGSConfigurable comp : getConfigurableComponents().values()) {

		if (comp.getKey().startsWith(DISTRIBUTED_ACCESSOR_KEY))
		    distributed = (IDistributedAccessor) comp;

	    }
	}

	return distributed;
    }

    @Override
    @JsonIgnore
    public IHarvestedAccessor getHarvestedAccessor() {
	return oai;
    }

    @Override
    @JsonIgnore
    public List<GSAccessor> getMixedAccessor() {
	return mixedAccessors;
    }

    public void setGSSource(GSSource source) throws GSException {

	GSSourceAccessor it = new Deserializer().deserialize(source.serialize(), GSSourceAccessor.class);

	it.setBrokeringStrategy(BrokeringStrategy.MIXED);

	it.setConfigurableAccessor(this);
	setInstantiableType(it);

    }
}
