package eu.essi_lab.services.keyvalue;

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

import java.util.*;

/**
 * @author Fabrizio
 */
public interface ReadableKeyValueStore {

    /**
     * @param serviceId
     * @return
     */
    List<Map.Entry<String, String>> get(String serviceId);

    /**
     * @param serviceId
     * @param key
     * @return
     */
    default Optional<Map.Entry<String, String>> get(String serviceId, String key){

        return get(serviceId).stream().filter(e -> e.getKey().equals(key)).findFirst();
    }
}
