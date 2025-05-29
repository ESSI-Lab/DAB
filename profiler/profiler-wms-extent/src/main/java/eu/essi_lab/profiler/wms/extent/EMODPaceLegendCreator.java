package eu.essi_lab.profiler.wms.extent;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.profiler.wms.extent.legend.InfoLegend;

public class EMODPaceLegendCreator extends LegendCreator {

    @Override
    public List<InfoLegend> getLegend(String layers, StationRecord station) {
	List<InfoLegend> ret = new ArrayList<>();
	ret.add(new InfoLegend(getColor(layers), layers));
	return ret;
    }

    public Color getColor(String layers) {
	switch (layers) {
	case "Chemistry":
	    return Color.decode("#00FF00");
	case "Oceanography NRT":
	    return Color.LIGHT_GRAY;
	case "Bathymetry":
	    return new Color(255, 0, 0, 127);
	case "Biology":
	    return new Color(0, 255, 255, 127);
	case "Physics":
	    return Color.decode("#FFFF00");
	case "Meteorology":
	    return Color.decode("#0000FF");

	default:
	    return super.getColor(layers);
	}

    }

}
