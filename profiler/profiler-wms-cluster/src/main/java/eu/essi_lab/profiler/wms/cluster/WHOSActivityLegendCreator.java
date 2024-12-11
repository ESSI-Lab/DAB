package eu.essi_lab.profiler.wms.cluster;

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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.profiler.wms.cluster.legend.InfoLegend;

public class WHOSActivityLegendCreator extends LegendCreator {

    public enum WHOSColor {
	WHOS_COLOR_LAST_HOUR("active during last hour", Color.green), //
	WHOS_COLOR_LAST_DAY("active during last day", Color.yellow), //
	WHOS_COLOR_LAST_WEEK("active during last week", Color.orange), //
	WHOS_COLOR_LAST_MONTH("active during last month", Color.red), //
	WHOS_COLOR_OTHERS("others", Color.gray);//

	String label;
	Color color;

	WHOSColor(String label, Color color) {
	    this.label = label;
	    this.color = color;
	}

	public String getLabel() {
	    return label;
	}

	public Color getColor() {
	    return color;
	}

	public InfoLegend getInfoLegend() {
	    return new InfoLegend(color, label);
	}
    }

    @Override
    public List<InfoLegend> getLegend(String layers, StationRecord station) {
	List<InfoLegend> ret = new ArrayList<>();
	if (station == null) {
	    ret.add(WHOSColor.WHOS_COLOR_LAST_HOUR.getInfoLegend());
	    ret.add(WHOSColor.WHOS_COLOR_LAST_DAY.getInfoLegend());
	    ret.add(WHOSColor.WHOS_COLOR_LAST_WEEK.getInfoLegend());
	    ret.add(WHOSColor.WHOS_COLOR_LAST_MONTH.getInfoLegend());
	    ret.add(WHOSColor.WHOS_COLOR_OTHERS.getInfoLegend());
	} else {
	    Date lastObservation = station.getLastObservation();
	    Date now = new Date();
	    if (new Date(now.getTime() - TimeUnit.HOURS.toMillis(1)).before(lastObservation)) {
		ret.add(WHOSColor.WHOS_COLOR_LAST_HOUR.getInfoLegend());
	    } else if (new Date(now.getTime() - TimeUnit.DAYS.toMillis(1)).before(lastObservation)) {
		ret.add(WHOSColor.WHOS_COLOR_LAST_DAY.getInfoLegend());
	    } else if (new Date(now.getTime() - TimeUnit.DAYS.toMillis(7)).before(lastObservation)) {
		ret.add(WHOSColor.WHOS_COLOR_LAST_WEEK.getInfoLegend());
	    } else if (new Date(now.getTime() - TimeUnit.DAYS.toMillis(31)).before(lastObservation)) {
		ret.add(WHOSColor.WHOS_COLOR_LAST_MONTH.getInfoLegend());
	    } else {
		ret.add(WHOSColor.WHOS_COLOR_OTHERS.getInfoLegend());
	    }
	}
	return ret;
    }

}
