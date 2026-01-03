package eu.essi_lab.shared.serializer;

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

import java.util.Iterator;
import java.util.ServiceLoader;

import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author ilsanto
 */
public class SharedContentSerializers {

    /**
     * @param type
     * @return
     */
    public static SharedContentSerializer getSerializer(SharedContentType type) {

	ServiceLoader<SharedContentSerializer> serviceLoader = ServiceLoader.load(SharedContentSerializer.class);

	Iterator<SharedContentSerializer> iterator = serviceLoader.iterator();

	while (iterator.hasNext()) {

	    SharedContentSerializer next = iterator.next();

	    if (next.supports(type)) {
		return next;
	    }
	}

	return null;
    }
}
