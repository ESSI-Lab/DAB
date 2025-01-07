package eu.essi_lab.request.executor.discover;

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

import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;

/**
 * The default source comparator puts harvested sources before distributed sources, and
 * alphabetically sorts each group
 * 
 * @author boldrini
 */
public class DefaultSourceComparator implements Comparator<GSSource> {

    @Override
    public int compare(GSSource s1, GSSource s2) {
	Integer strategy1 = getValue(s1.getBrokeringStrategy());
	Integer strategy2 = getValue(s2.getBrokeringStrategy());
	if (strategy1.equals(strategy2)) {
	    String label1 = s1.getLabel();
	    String label2 = s2.getLabel();
	    return label1.compareTo(label2);
	} else {
	    return strategy1.compareTo(strategy2);
	}

    }

    private Integer getValue(BrokeringStrategy strategy) {
	if (strategy == null) {
	    return 4;
	}
	switch (strategy) {
	case HARVESTED:
	    return 1;
	case DISTRIBUTED:
	    return 2;
	default:
	    return 3;
	}
    }

}
