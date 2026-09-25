package eu.essi_lab.profiler.wms.extent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.profiler.wms.extent.legend.InfoLegend;

public class AggregatedTRIGGERSourcesLegendCreator extends LegendCreator {

    @Override
    public List<InfoLegend> getLegend(String layers, StationRecord station) {
	List<InfoLegend> ret = new ArrayList<>();
	if (station == null) {
	    ret.add(getInfoLegend("aggregated-trigger", "TRIGGER Aggregated Data Store"));
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
	case "aggregated-trigger":
	    return Color.blue;

	default:
	    break;
	}
	return super.getColor(layers);
    }

}