package eu.essi_lab.configuration.sync;

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

import java.util.concurrent.TimeUnit;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.GSException;

public interface IConfigurationSync {
    GSConfiguration getConfiguration() throws GSException;
    
    
   /** Returns a clone of the current local {@link GSConfiguration}.
    * 
    * @return a clone of the current local {@link GSConfiguration}.
    * @throws GSException
    */
   public GSConfiguration getClonedConfiguration() throws GSException;
    
    
    /**
     * Use this method to set the update frequency with which the local configuration is updated from the remote storage.
     * @param frequency
     * @param unit
     * @throws GSException
     */
    void setUpdateFrequency(int frequency, TimeUnit unit) throws GSException;

    /**
     * This method can be used to read actual remote configuration. Note that this method does not update local configuration.
     *
     * @return the current remote {@link GSConfiguration}.
     * @throws GSException
     */
    GSConfiguration fetchRemote() throws GSException;

    IGSConfigurationStorage getDBGISuiteConfiguration();

    void setDBGISuiteConfiguration(IGSConfigurationStorage c);


}
