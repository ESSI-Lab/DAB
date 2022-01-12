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

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
public interface IGSConfigurationWriter {

    /**
     * Adds a {@link GSSource}.
     *
     * @param source
     * @throws GSException if the provided {@link GSSource} has an identifier which already exists in configuration.
     */
    IGSConfigurable addSource(Source source) throws GSException;

    /**
     * Sets the option. If the option is unknown by the referenced component, it is ignored.
     *
     * @param componentKey, the key which identifies the component where this option must be set.
     * @param option
     * @throws {@link GSException} when the option value is inconsistent, not permitted, etc. or when the option key/component key is
     * unknown or null.
     */
    IGSConfigurable setOption(ConfigurableKey componentKey, GSConfOption<?> option) throws GSException;

    /**
     * Flushes current {@link GSConfiguration} to remote storage. The method first checks if the remote {@link GSConfiguration} time stamp
     * is == the time stamp of local configuration instance. If this condition is met, this method updates the local configuration time
     * stamp with current millis and stores local configuration to remote storage {@link IGSConfigurationStorage#transactionUpdate(GSConfiguration)}.
     * IMPORTANT: this leaves some space for possible conflicts (two machines executing this method in parallel would call {@link
     * IGSConfigurationStorage#transactionUpdate(GSConfiguration)} at the same time, but only one will succeed).
     *
     * @throws GSException when the remote {@link GSConfiguration} time stamp is > than the time stamp of local configuration instance.
     */
    void flush() throws GSException;

    /**
     * Flushes the input {@link GSConfiguration} to remote storage. The method first checks if the remote {@link GSConfiguration} time stamp
     * is == the time stamp of input configuration instance. If this condition is met, this method updates the local configuration time
     * stamp with current millis and stores input configuration to remote storage {@link IGSConfigurationStorage#transactionUpdate(GSConfiguration)}.
     * IMPORTANT: this leaves some space for possible conflicts (two machines executing this method in parallel would call {@link
     * IGSConfigurationStorage#transactionUpdate(GSConfiguration)} at the same time, but only one will succeed).
     *
     * @param conf the {@link GSConfiguration} to store.
     * @throws GSException when the remote {@link GSConfiguration} time stamp is > than the time stamp of local configuration instance.
     */
    void flush(GSConfiguration conf) throws GSException;

}
