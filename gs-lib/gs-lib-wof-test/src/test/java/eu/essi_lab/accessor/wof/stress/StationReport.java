package eu.essi_lab.accessor.wof.stress;

import java.util.ArrayList;
import java.util.List;

public class StationReport {

    private String code;

    private String name;

    private Long meanTime = 0l;

    public Long getMeanTime() {
	return meanTime;
    }

    public void setMeanTime(Long meanTime) {
	this.meanTime = meanTime;
    }

    private List<VariableReport> variableReports = new ArrayList<>();

    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public List<VariableReport> getVariableReports() {
	return variableReports;
    }

    public void setVariableReports(List<VariableReport> variableReports) {
	this.variableReports = variableReports;
    }

}
