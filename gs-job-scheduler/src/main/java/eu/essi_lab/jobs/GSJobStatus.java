package eu.essi_lab.jobs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.essi_lab.model.configuration.GSJSONSerializable;
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class GSJobStatus extends GSJSONSerializable {

    private String jobkey;

    private String jobtype;

    private String executionid;

    private Date startDate;

    private Date endDate;

    private String esStartDate;

    private String esEndDate;

    private Boolean isRecovering;
    private String lastPhase;
    private String result;
    private String exception;
    private String resultStorage;

    /**
     * The job key is the identifier of a job (e.g. a Harvester job has the source id as key)
     *
     * @return
     */
    public String getJobkey() {
	return jobkey;
    }

    public void setJobkey(String jobkey) {
	this.jobkey = jobkey;
    }

    /**
     * The job type is used to group jobs by their type (usually the job type is the package of the job
     *
     * @return
     */
    public String getJobtype() {
	return jobtype;
    }

    public void setJobtype(String jobtype) {
	this.jobtype = jobtype;
    }

    /**
     * The id of a particular execution of the job.
     *
     * @return
     */
    public String getExecutionid() {
	return executionid;
    }

    public void setExecutionid(String executionid) {
	this.executionid = executionid;
    }

    public Date getStartDate() {
	return startDate;
    }

    public void setStartDate(Date startDate) {
	this.startDate = startDate;
	setEsStartDate(getESTimeStamp(this.startDate.getTime()));
    }

    public Date getEndDate() {
	return endDate;
    }

    public void setEndDate(Date endDate) {
	this.endDate = endDate;

	setEsEndDate(getESTimeStamp(this.endDate.getTime()));
    }

    public Boolean getRecovering() {
	return isRecovering;
    }

    public void setRecovering(Boolean recovering) {
	isRecovering = recovering;
    }

    public void setLastPhase(String lastPhase) {
	this.lastPhase = lastPhase;
    }

    public String getLastPhase() {
	return lastPhase;
    }

    public void setResult(String result) {
	this.result = result;
    }

    public String getResult() {
	return result;
    }

    public void setException(String exception) {
	this.exception = exception;
    }

    public String getException() {
	return exception;
    }

    private static final String ES_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static String getESTimeStamp(Long timestamp) {

	SimpleDateFormat sdf = new SimpleDateFormat(ES_FORMAT);

	return sdf.format(new Date(timestamp));

    }

    public String getEsEndDate() {
	return esEndDate;
    }

    public void setEsEndDate(String esEndDate) {
	this.esEndDate = esEndDate;
    }

    public String getEsStartDate() {
	return esStartDate;
    }

    public void setEsStartDate(String esStartDate) {
	this.esStartDate = esStartDate;
    }

    public void setResultStorage(String resultStorage) {
	this.resultStorage = resultStorage;
    }
    
    public String getResultStorage() {
        return resultStorage;
    }

    // TODO
    // add information about execution node, this could be dona by simply adding the docker container id (from
    // docker-generated env
    // variable $HOSTNAME) and/or using the new ECS agent and reading the file pointed by $ECS_CONTAINER_METADATA_FILE
    // (see, https://docs
    // .aws.amazon.com/AmazonECS/latest/developerguide/container-metadata.html) and/or from non AWS environments using
    // ad-hoc solutions
    // TODO
    // add also environment informaiton? (rtest, preproduction, production)
}
