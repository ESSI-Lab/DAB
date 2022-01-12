package eu.essi_lab.cdk.harvest;

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
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;

/**
 * @author Fabrizio
 */
public abstract class AbstractHarvestedQueryConnector extends AbstractGSconfigurable implements IHarvestedQueryConnector {

    /**
     * 
     */
    private static final long serialVersionUID = 8651460915863000938L;
    private String sourceURL;
    private Map<String, GSConfOption<?>> options;

    @JsonIgnore
    public static final String MAX_RECORDS_KEY = "MAX_RECORDS_KEY";

    public AbstractHarvestedQueryConnector() {

	options = new HashMap<>();
	// this forces the label to be set
	setLabel(getLabel());

	if (enableMaxRecordsOption()) {

	    GSConfOptionInteger maxRecords = new GSConfOptionInteger();
	    maxRecords.setLabel("Max records (0=unlimited)");
	    maxRecords.setKey(MAX_RECORDS_KEY);
	    maxRecords.setValue(0);

	    getSupportedOptions().put(MAX_RECORDS_KEY, maxRecords);
	}
    }

    /**
     * Returns <code>true</code> (default) to show the max records option, <code>false</code> otherwise
     */
    public boolean enableMaxRecordsOption() {

	return true;
    }

    /**
     * @return
     */
    @JsonIgnore
    public Optional<Integer> getMaxRecords() {

	if (enableMaxRecordsOption()) {

	    return Optional.ofNullable((Integer) (getSupportedOptions().get(MAX_RECORDS_KEY).getValue()));
	}

	return Optional.empty();
    }

    /**
     * @param maxRecords
     */
    @JsonIgnore
    public void setMaxRecords(int maxRecords) {

	if (enableMaxRecordsOption()) {

	    ((GSConfOptionInteger) getSupportedOptions().get(MAX_RECORDS_KEY)).setValue(maxRecords);
	}
    }

    /**
     * @return
     */
    @JsonIgnore
    public boolean isMaxRecordsUnlimited() {
	Optional<Integer> mr = getMaxRecords();
	return !mr.isPresent() || (mr.isPresent() && mr.get() <= 0);
    }

    /**
     * Forces the subclasses to retrieve the connector label!
     */
    @Override
    public abstract String getLabel();

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return false;
    }

    @Override
    public String getSourceURL() {

	return sourceURL;
    }

    protected String getQueryStringURL() {

	return sourceURL.endsWith("?") ? sourceURL : sourceURL + "?";
    }

    @Override
    public void setSourceURL(String sourceURL) {

	this.sourceURL = sourceURL;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
    }

    @Override
    public void onFlush() throws GSException {
    }

    @JsonIgnore
    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

}
