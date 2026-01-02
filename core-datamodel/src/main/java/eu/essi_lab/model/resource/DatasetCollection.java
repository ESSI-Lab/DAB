/**
 * 
 */
package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
@XmlRootElement(name = "DatasetCollection", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class DatasetCollection extends GSResource {

    public DatasetCollection() {

	super(ResourceType.DATASET_COLLECTION);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     * @throws ClassCastException
     */
    public static DatasetCollection create(InputStream stream) throws JAXBException, ClassCastException {

	return (DatasetCollection) GSResource.create(stream);
    }
}
