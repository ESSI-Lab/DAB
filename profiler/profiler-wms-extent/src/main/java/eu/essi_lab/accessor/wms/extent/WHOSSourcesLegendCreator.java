package eu.essi_lab.accessor.wms.extent;

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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.accessor.wms.extent.legend.InfoLegend;

public class WHOSSourcesLegendCreator extends LegendCreator {

    @Override
    public List<InfoLegend> getLegend(String layers, StationRecord station) {
	List<InfoLegend> ret = new ArrayList<>();
	if (station == null) {
	    ret.add(getInfoLegend("brazil-ana"));
	    ret.add(getInfoLegend("brazil-ana-sar"));
	    ret.add(getInfoLegend("brazil-inmet"));
	    ret.add(getInfoLegend("brazil-inmet-plata"));
	    ret.add(getInfoLegend("argentina-ina"));
	    ret.add(getInfoLegend("uruguay-dinagua"));
	    ret.add(getInfoLegend("uruguay-inumet"));
	    ret.add(getInfoLegend("paraguay-dmh"));
	} else {
	    String sourceId = station.getSourceIdentifier();
	    ret.add(getInfoLegend(sourceId));
	}
	return ret;
    }

    private InfoLegend getInfoLegend(String sourceId) {
	return new InfoLegend(getColor(sourceId), sourceId);
    }

    @Override
    public Color getColor(String layers) {
	switch (layers) {
	case "brazil-ana":
	    return Color.green;
	case "brazil-ana-sar":
	    return Color.blue;
	case "brazil-inmet":
	    return Color.red;
	case "brazil-inmet-plata":
	    return Color.cyan;
	case "argentina-ina":
	    return Color.magenta;
	case "uruguay-dinagua":
	    return Color.yellow;
	case "uruguay-inumet":
	    return Color.pink;
	case "paraguay-dmh":
	    return Color.orange;

	default:
	    break;
	}
	return super.getColor(layers);
    }

}
