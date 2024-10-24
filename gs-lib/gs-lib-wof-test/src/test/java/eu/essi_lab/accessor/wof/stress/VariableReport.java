package eu.essi_lab.accessor.wof.stress;

import java.util.Date;

public class VariableReport {
    private String name;
    private String code;
    private Date begin;
    private Date end;

    private Date testedBegin;
    private Date testedEnd;

    private boolean succeeded = false;

    public Date getTestedBegin() {
	return testedBegin;
    }

    public void setTestedBegin(Date testedBegin) {
	this.testedBegin = testedBegin;
    }

    public Date getTestedEnd() {
	return testedEnd;
    }

    public void setTestedEnd(Date testedEnd) {
	this.testedEnd = testedEnd;
    }

    public boolean isSucceeded() {
	return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
	this.succeeded = succeeded;
    }

    private Long time;

    public Long getTime() {
	return time;
    }

    public void setTime(Long time) {
	this.time = time;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    public Date getBegin() {
	return begin;
    }

    public void setBegin(Date begin) {
	this.begin = begin;
    }

    public Date getEnd() {
	return end;
    }

    public void setEnd(Date end) {
	this.end = end;
    }

}
