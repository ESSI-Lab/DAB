package eu.essi_lab.cfga.gui.components.grid;

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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;

import eu.essi_lab.cfga.setting.Setting;

/**
 * <code>
 * 	SettingService service = new SettingService(list.stream().map(c -> c.getSetting()).collect(Collectors.toList()));

	CallbackDataProvider<Setting, Void> provider = DataProvider

		.fromCallbacks(query -> service.fetch(query), //
			query -> service.count(query));
			
			</code>
 * 
 * @author Fabrizio
 */
public class SettingService {

    private List<Setting> list;

    /**
     * @param list
     */
    public SettingService(List<Setting> list) {

	this.list = list;
    }

    /**
     * 
     */
    public Stream<Setting> fetch(Query<Setting, Void> query) {

	Optional<Comparator<Setting>> sortingComparator = query.getSortingComparator();
	if (sortingComparator.isPresent()) {

	    Comparator<Setting> comparator = sortingComparator.get();

	    System.out.println(comparator);

	}

	List<QuerySortOrder> sortOrders = query.getSortOrders();
	System.out.println(sortOrders);

	int offset = query.getOffset();

	int limit = Math.min(query.getRequestedRangeEnd(), list.size());

	List<Setting> sublist = list.subList(offset, limit);

	return sublist.stream();
    }

    /**
    * 
    */
    public int count(Query<Setting, Void> query) {

	return this.list.size();
    }

}
