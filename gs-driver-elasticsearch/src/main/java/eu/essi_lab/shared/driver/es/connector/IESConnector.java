package eu.essi_lab.shared.driver.es.connector;

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

import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.model.SharedContentType;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
public interface IESConnector {

    StorageUri getEsStaorageUri();

    void setEsStaorageUri(StorageUri esStaorageUri);

    boolean testConnection();

    void initializePersistentStorage() throws GSException;

    void write(String identifier, SharedContentType type, InputStream stream) throws GSException;

    Long count(SharedContentType type) throws GSException;

    Optional<InputStream> get(String identifier, SharedContentType type) throws GSException;

    List<InputStream> query(SharedContentType type, JSONObject query) throws GSException;
}
