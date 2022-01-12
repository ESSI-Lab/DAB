package eu.essi_lab.model.configuration;

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

import java.util.Map;

import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
public interface IGSConfigurable {

    /**
     * Returns all supported options for this GI-suite component.
     *
     * @return all supported {@link GSConfOption}s for this GI-suite component.
     */
    public Map<String, GSConfOption<?>> getSupportedOptions();

    void setSupportedOptions(Map<String, GSConfOption<?>> opts);

    /**
     * Returns the human readable name of this component.
     *
     * @return
     */
    public String getLabel();

    /**
     * Sets the human readable name of this component (this seems to be needed for Jackson).
     *
     * @return
     */
    public void setLabel(String label);

    /**
     * Returns the machine readable name of this component. This is global, i.e. it must be unique throghout the configuraiton.
     *
     * @return
     */
    public String getKey();

    /**
     * Sets the machine readable name of this component. This is global, i.e. it must be unique throghout the configuraiton.
     *
     * @return
     */
    public void setKey(String key);

    /**
     * Sets the passed {@link GSConfOption}. If the option is unknown by this component, it is ignored.
     *
     * @param option
     * @return true, if the option has been set by this component, false otherwise
     * @throws {@link GSException} when the passed option is known but is not valid.
     */
    public boolean setOption(GSConfOption<?> option) throws GSException;

    /**
     * Executes actions related to the option which is being passed, e.g. adds new supported options. Note that this method is invoked even
     * if {@link IGSConfigurable#setOption(GSConfOption)} returns false.
     *
     * @param opt
     * @throws GSException
     */
    public void onOptionSet(GSConfOption<?> opt) throws GSException;

    /**
     * This method is invoked when the configuration is flushed. Each configurable might need to trigger some actions (e.g. schedule a job).
     * The method is invoked AFTER successful flush of configuration.
     *
     * @throws GSException
     */
    public void onFlush() throws GSException;

    public GSConfOption<?> read(String key);

    /**
     * @return an instance of {@link IGSConfigurationInstantiable} associated with this configurable component, null if this configurable is
     * not associated with any runtime object.
     */
    public IGSConfigurationInstantiable getInstantiableType();

    public void setInstantiableType(IGSConfigurationInstantiable instantiableType);

    /**
     * This is invoked when a node of DAB is started up
     */
    void onStartUp() throws GSException;
}
