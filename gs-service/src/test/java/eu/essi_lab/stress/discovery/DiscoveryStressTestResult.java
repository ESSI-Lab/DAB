package eu.essi_lab.stress.discovery;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTestResult {

    private int code;

    private String request;

    private Long execTime;

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
	return execTime;
    }

    public void setExecTime(Long execTime) {
	this.execTime = execTime;
    }
}
