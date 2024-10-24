package eu.essi_lab.shared.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.messages.SharedContentQuery;

/**
 * @author ilsanto
 */
public interface ISharedRepositoryDriver<T extends DriverSetting> extends Configurable<T> {

    /**
     * Reads {@link SharedContent} with id identifier and of the provided type. If no element is found with the given
     * identifier, null is
     * returned.
     *
     * @param identifier
     * @return
     * @throws GSException
     */
    @SuppressWarnings("rawtypes")
    SharedContent read(String identifier, SharedContentType type) throws GSException;

    @SuppressWarnings("rawtypes")
    List<SharedContent> read(SharedContentType type, SharedContentQuery query) throws GSException;

    @SuppressWarnings("rawtypes")
    void store(SharedContent sharedContent) throws GSException;

    Long count(SharedContentType type) throws GSException;
    
    SharedContentCategory getCategory();
}
