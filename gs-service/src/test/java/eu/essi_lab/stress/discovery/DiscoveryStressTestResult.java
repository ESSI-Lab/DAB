package eu.essi_lab.stress.discovery;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTestResult {

    private int code;

    private String request;

    private Long start;
    private Long end;

    private DiscoveryStressTest test;

    private String responseFile;

    public int getCode() {
	return code;
    }

    public void setCode(int code) {
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

    public void setTest(DiscoveryStressTest test) {
	this.test = test;
    }

    public DiscoveryStressTest getTest() {
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
}
