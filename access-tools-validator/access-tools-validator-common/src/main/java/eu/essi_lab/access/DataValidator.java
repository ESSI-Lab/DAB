package eu.essi_lab.access;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;

/**
 * @author Fabrizio
 */
public interface DataValidator extends Pluggable {

    /**
     * @param dataObject, containing the stream and its descriptor
     * @return a validation message, specifying if validation succeeded and if not why not.
     * @throws GSException in case some unexpected event occurred
     */
    public ValidationMessage validate(DataObject dataObject) throws GSException;
    
    public DataDescriptor readDataAttributes(DataObject dataObject);

    /**
     * @return
     */
    public DataFormat getFormat();
    
    /**
     * @return
     */
    public DataType getType();

}
