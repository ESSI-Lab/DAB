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

public class TRIGGERSourcesLegendCreator extends LegendCreator {

    @Override
    public List<InfoLegend> getLegend(String layers, StationRecord station) {
	List<InfoLegend> ret = new ArrayList<>();
	if (station == null) {
	    ret.add(getInfoLegend("trigger-aux","TRIGGER Auxiliary Data Store (AUX)"));
	} else {
	    String sourceId = station.getSourceIdentifier();
	    ret.add(getInfoLegend(sourceId));
	}
	return ret;
    }

    private InfoLegend getInfoLegend(String sourceId, String label) {
  	return new InfoLegend(getColor(sourceId), label);
      }
    
    private InfoLegend getInfoLegend(String sourceId) {
	return new InfoLegend(getColor(sourceId), sourceId);
    }

    @Override
    public Color getColor(String layers) {
	switch (layers) {
	case "trigger-aux":
	    return Color.green;


	default:
	    break;
	}
	return super.getColor(layers);
    }

}
