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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.profiler.wms.extent.feature.info.DatasetFeatureInfoGenerator;
import eu.essi_lab.profiler.wms.extent.feature.info.StationFeatureInfoGenerator;
import eu.essi_lab.profiler.wms.extent.feature.info.WMSFeatureInfoGenerator;
import eu.essi_lab.profiler.wms.extent.legend.InfoLegend;

public enum WMSLayer {

    EMOD_PACE_BATHYMETRY("emod-pace", "Bathymetry", "themeCategory", "Bathymetry", new EMODPaceLegendCreator(), new DatasetFeatureInfoGenerator()), //
    EMOD_PACE_CHEMISTRY("emod-pace", "Chemistry", "themeCategory", "Chemistry", new EMODPaceLegendCreator(), new DatasetFeatureInfoGenerator()), //
    EMOD_PACE_METEOROLOGY("emod-pace", "Meteorology", "themeCategory", "Meteorology", new EMODPaceLegendCreator(), new DatasetFeatureInfoGenerator()), //
    EMOD_PACE_PHYSICS("emod-pace", "Physics", "themeCategory", "Physics", new EMODPaceLegendCreator(), new DatasetFeatureInfoGenerator()), //
    EMOD_PACE_BIOLOGY("emod-pace", "Biology", "themeCategory", "Biology", new EMODPaceLegendCreator(), new DatasetFeatureInfoGenerator()), //
    EMOD_PACE_OCEANOGRAPHY_NRT("emod-pace", "Oceanography NRT", "themeCategory", "Oceanography NRT", new EMODPaceLegendCreator(), new DatasetFeatureInfoGenerator()), //
    WHOS_ACTIVITY("whos", "activity", "whosCategory", "sensor", new WHOSActivityLegendCreator(), new DatasetFeatureInfoGenerator()), //
    WHOS_SOURCES("whos", "sources", "whosCategory", "sensor", new WHOSSourcesLegendCreator(), new DatasetFeatureInfoGenerator()),//
    ICHANGE_MONITORING_POINTS("i-change", "i-change-monitoring-points", "themeCategory", "i-change", new ICHANGESourcesLegendCreator(), new StationFeatureInfoGenerator()),
    TRIGGER_MONITORING_POINTS("trigger", "trigger-monitoring-points", "themeCategory", "trigger", new TRIGGERSourcesLegendCreator(), new StationFeatureInfoGenerator())
    ;

    private String view;

    private String value;

    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	this.value = value;
    }

    private String layerName;

    public String getLayerName() {
	return layerName;
    }

    private LegendCreator legendCreator;

    public String getView() {
	return view;
    }

    public String getProperty() {
	return property;
    }

    private String property;

    private WMSFeatureInfoGenerator generator;

    private WMSLayer(String v, String m, String p, String value, LegendCreator lc, WMSFeatureInfoGenerator g) {
	this.view = v;
	this.layerName = m;
	this.property = p;
	this.legendCreator = lc;
	this.value = value;
	this.generator = g;
    }

    public String getLayerValue(StationRecord station) {
	switch (getProperty()) {
	case "themeCategory":
	    return station.getThemeCategory();
	case "whosCategory":
	default:
	    return station.getWhosCategory();
	}
    }

    public static List<WMSLayer> decode(Optional<String> view) {
	List<WMSLayer> ret = new ArrayList<>();
	if (view.isPresent()) {
	    for (WMSLayer wv : WMSLayer.values()) {
		if (view.get().equals(wv.getView())) {
		    ret.add(wv);
		}
	    }
	}
	return ret;
    }

    public List<InfoLegend> getInfoLegend(String layers, StationRecord station) {
	return legendCreator.getLegend(layers, station);
    }

    public static int getSize(List<InfoLegend> infos) {
	int ret = 0;
	for (InfoLegend info : infos) {
	    if (info.getLabel().length() > ret) {
		ret = info.getLabel().length();
	    }
	}
	return ret;
    }

    public WMSFeatureInfoGenerator getGenerator() {
	return generator;
    }
}
