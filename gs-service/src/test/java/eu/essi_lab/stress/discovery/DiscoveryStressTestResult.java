package eu.essi_lab.stress.discovery;

import eu.essi_lab.stress.plan.IStressTest;
import eu.essi_lab.stress.plan.IStressTestResult;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTestResult implements IStressTestResult {

    private int code;

    private String request;

    private Long start;
    private Long end;

    private IStressTest test;

    private String responseFile;

    public int getCode() {
	return code;
    }

    @Override
    public void setCode(Integer code) {
	this.code = code;
    }

    public String getRequest() {
	return request;
    }

    @Override
    public void setRequest(String request) {
	this.request = request;
    }

    public Long getExecTime() {
	return end - start;
    }

    @Override
    public void setTest(IStressTest test) {
	this.test = test;
    }

    public IStressTest getTest() {
	return test;
    }

    public Long getEnd() {
	return end;
    }

    @Override
    public void setEnd(Long end) {
	this.end = end;
    }

    public Long getStart() {
	return start;
    }

    @Override
    public void setStart(Long start) {
	this.start = start;
    }

    public String getResponseFile() {
	return responseFile;
    }

    @Override
    public void setResponseFile(String responseFile) {
	this.responseFile = responseFile;
    }
}
