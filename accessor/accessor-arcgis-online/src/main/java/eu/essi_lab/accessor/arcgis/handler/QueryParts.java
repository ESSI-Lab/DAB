package eu.essi_lab.accessor.arcgis.handler;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author ilsanto
 */
public class QueryParts {

    private List<String> list = new ArrayList<>();

    public void add(String queryClause) {

	list.add(queryClause);
    }

    public Optional<String> removeLast() {

	if (!isEmpty())
	    return Optional.of(list.remove(list.size() - 1));

	return Optional.empty();
    }

    public boolean isEmpty() {
	return list.isEmpty();
    }

    public Stream<String> stream() {
	return list.stream();
    }
}