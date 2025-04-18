package eu.essi_lab.stress.plan;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class StressTestResult {

    private int code;

    private String request;
    private String requestId;

    private Long start;
    private Long end;

    private IStressTest test;

    private String responseFile;
    private List<String> responseMetrics = new ArrayList<>();

    private List<String> serverMetrics = new ArrayList<>();

    public int getCode() {
	return code;
    }

    public void setCode(Integer code) {
	this.code = code;
    }

    public String getRequest() {
	return request;
    }

    public void setRequest(String request) {
	this.request = request;
    }

    public Long getExecTime() {
	return end - start;
    }

    public void setTest(IStressTest test) {
	this.test = test;
    }

    public IStressTest getTest() {
	return test;
    }

    public Long getEnd() {
	return end;
    }

    public void setEnd(Long end) {
	this.end = end;
    }

    public Long getStart() {
	return start;
    }

    public void setStart(Long start) {
	this.start = start;
    }

    public String getResponseFile() {
	return responseFile;
    }

    public void setResponseFile(String responseFile) {
	this.responseFile = responseFile;
    }

    public List<String> getResponseMetrics() {
	return responseMetrics;
    }

    public Long readMetric(String metric, IStressTest test) {
	return test.readMetric(metric, responseFile);
    }

    public List<String> getServerMetrics() {
	return serverMetrics;
    }

    public String getRequestId() {
	return requestId;
    }

    public void setRequestId(String requestId) {
	this.requestId = requestId;
    }
}
