package eu.essi_lab.api.database;

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

import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.exceptions.GSException;
public interface SourceStorage extends DatabaseConsumer, IGSConfigurable {

    /**
     *
     */
    static final String MARK_DELETED_RECORDS_KEY = "MARK_DELETED_RECORDS_KEY";
    /**
     *
     */
    static final String TEST_ISO_COMPLIANCE_KEY = "TEST_ISO_COMPLIANCE_KEY";
    /**
     *
     */
    static final String RECOVER_TAGS_KEY = "RECOVER_TAGS_KEY";
    /**
    *
    */
   static final String FORCE_OVERWRITE_TAGS_KEY = "FORCE_OVERWRITE_TAGS_KEY";

   /**
    * @return
    */
   static GSConfOptionBoolean createMarkDeletedOption() {

	GSConfOptionBoolean option = new GSConfOptionBoolean();
	option.setLabel("Mark deleted records");
	option.setKey(MARK_DELETED_RECORDS_KEY);
	option.setValue(false);

	return option;
   }
   
   /**
    * @return
    */
   static GSConfOptionBoolean createForceOverwriteOption() {

	GSConfOptionBoolean option = new GSConfOptionBoolean();
	option.setLabel("Force overwrite of last harvesting records");
	option.setKey(FORCE_OVERWRITE_TAGS_KEY);
	option.setValue(false);

	return option;
   }

    /**
     * @return
     */
    static GSConfOptionBoolean createISOComplianceOption() {

	GSConfOptionBoolean option = new GSConfOptionBoolean();
	option.setLabel("Test ISO compliance");
	option.setKey(TEST_ISO_COMPLIANCE_KEY);
	option.setValue(false);

	return option;
    }

    /**
     * @return
     */
    static GSConfOptionBoolean createRecoverTagsOption() {

	GSConfOptionBoolean option = new GSConfOptionBoolean();
	option.setLabel("Recover resource tags");
	option.setKey(RECOVER_TAGS_KEY);
	option.setValue(true);

	return option;
    }

    /**
     * Notifies that the harvesting of <code>source</code> is started
     *
     * @param source the harvested source
     * @param strategy the strategy used to harvest the source
     */
    void harvestingStarted(GSSource source, HarvestingStrategy strategy, boolean recovery) throws GSException;

    /**
     * Notifies that the harvesting of <code>source</code> is started
     *
     * @param source the harvested source
     * @param strategy the strategy used to harvest the source
     */
    void harvestingEnded(GSSource source, HarvestingStrategy strategy) throws GSException;

    /**
     * Retrieve a properties file which provide information about the harvesting of the supplied <code>source</code>
     *
     * @param source the harvested source
     * @return a {@link HarvestingProperties} with information about the harvesting of the supplied
     *         <code>source</code>
     */
    HarvestingProperties retrieveHarvestingProperties(GSSource source) throws GSException;

    /**
     * @param source
     * @param properties
     * @throws GSException
     */
    void storeHarvestingProperties(GSSource source, HarvestingProperties properties) throws GSException;

    /**
     * @return
     */
    List<String> getStorageReport(GSSource source) throws GSException;

    /**
     * @param source
     * @param report
     * @throws GSException
     */
    void updateErrorsAndWarnReport(GSSource source, String report) throws GSException;

    /**
     * @param source
     * @return
     * @throws GSException
     */
    public List<String> retrieveErrorsReport(GSSource source) throws GSException;
    
    /**
     * @param source
     * @return
     * @throws GSException
     */
    public List<String> retrieveWarnReport(GSSource source) throws GSException;
}
