package eu.essi_lab.accessor.wof.stress;

import java.util.ArrayList;
import java.util.List;

public class SourceReport {
    private String sourceName;
    private String sourceURL;

    private List<StationReport> stationReports = new ArrayList<StationReport>();

    public List<StationReport> getStationReports() {
	return stationReports;
    }

    public void setStationReports(List<StationReport> stationReports) {
	this.stationReports = stationReports;
    }

    private Long meanTime;

    public Long getMeanTime() {
	return meanTime;
    }

    public void setMeanTime(Long meanTime) {
	this.meanTime = meanTime;
    }

    public SourceReport() {

    }

    public String getSourceName() {
	return sourceName;
    }

    public void setSourceName(String sourceName) {
	this.sourceName = sourceName;
    }

    public String getSourceURL() {
	return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
	this.sourceURL = sourceURL;
    }

}