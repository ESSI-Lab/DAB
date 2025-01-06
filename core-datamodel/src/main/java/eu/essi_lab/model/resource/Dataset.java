package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

@XmlRootElement(name = "Dataset", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class Dataset extends GSResource {

    public Dataset() {

	super(ResourceType.DATASET);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     * @throws ClassCastException
     */
    public static Dataset create(InputStream stream) throws JAXBException, ClassCastException {

	return (Dataset) GSResource.create(stream);
    }
}
