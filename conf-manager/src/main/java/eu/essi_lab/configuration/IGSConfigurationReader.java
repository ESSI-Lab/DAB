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
import java.util.Observer;

import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
public interface IGSConfigurationReader extends Observer {

    /**
     * Reads the {@link GSConfOption} with identifier optionKey from the component passed as parameter.
     *
     * @param component
     * @param optionKey
     * @return Returns the {@link GSConfOption} with identifier key. If no option is available for the identifier optionKey, null is
     * returned.
     */
    public GSConfOption<?> readOption(IGSConfigurable component, String optionKey) throws GSException;

    public long readTimeStamp() throws GSException;

    public GSConfiguration getConfiguration() throws GSException;

    /**
     * Reads the {@link IGSConfigurable} with identifier componentKey from the Configuration.
     *
     * @param componentKey
     * @return Returns the {@link IGSConfigurable} component with identifier componentKey. If none is available, null is returned.
     */
    public IGSConfigurable readComponent(ConfigurableKey componentKey) throws GSException;

    /**
     * Reads to {@link Source} with identifier = uniqueIdentifier in the list of configured Brokered sources. If none has identifier =
     * uniqueIdentifier, null is returned
     *
     * @param uniqueIdentifier
     * @return the source with identifier = uniqueIdentifier, null if no source has uniqueIdentifier.
     */
    public Source readSource(String uniqueIdentifier);

    /**
     * Reads to {@link GSSource} with identifier = uniqueIdentifier in the list of configured Brokered sources. If none has identifier =
     * uniqueIdentifier, null is returned
     *
     * @param uniqueIdentifier
     * @return the source with identifier = uniqueIdentifier, null if no source has uniqueIdentifier.
     */
    public GSSource readGSSource(String uniqueIdentifier) throws GSException;

    /**
     * Reads all the {@link GSSource}s.
     *
     * @return a list of {@link GSSource}s. If none is present, an empty list is returned.
     * @throws GSException
     */
    public List<GSSource> readGSSources() throws GSException;

    /**
     * Reads from current configuration an object identified by identifier and instantiable as Class. Uses the provided {@link Deserializer}
     * to clone objects.
     *
     * @param <T>
     * @param clazz
     * @param identifier
     * @param deserializer
     * @return a cloned instance of the identifed instance, or null if no configuration object matches.
     * @throws GSException if errors occour during cloning operation.
     */
    public <T> T readInstantiableType(Class<T> clazz, String identifier, Deserializer deserializer) throws GSException;

    /**
     * Like {@link #readInstantiableType(Class, String, Deserializer)}, with no filter on object identifiers. Uses the provided {@link
     * Deserializer} to clone objects.
     *
     * @param <T>
     * @param c
     * @param deserializer
     * @return A list of cloned instances of matching objects, or an empty list if none matches.
     * @throws GSException if errors occour during cloning operation.
     */
    public <T> List<T> readInstantiableType(Class<T> c, Deserializer deserializer) throws GSException;

    /**
     * This method creates on-the-fly the {@link ConfigurableKey} for the provided configurable.
     *
     * @param configurable
     * @return The generated {@link ConfigurableKey}
     */
    ConfigurableKey getConfigurableKey(IGSConfigurable configurable) throws GSException;
}
