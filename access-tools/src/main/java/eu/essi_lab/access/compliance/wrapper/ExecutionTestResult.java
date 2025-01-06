package eu.essi_lab.access.compliance.wrapper;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
public class ExecutionTestResult {

    @XmlElement(name = "executionResult", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String executionResult;

    @XmlElement(required = false, name = "validationError", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String validationError;

    @XmlElement(name = "executionTimeLong", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private long executionTimeLong;

    @XmlElement(name = "executionTime", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String executionTime;

    /**
     * @return
     */
    @XmlTransient
    public String getExecutionResult() {

	return executionResult;
    }

    /**
     * @param executionResult
     */
    public void setExecutionResult(String executionResult) {

	this.executionResult = executionResult;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getValidationError() {

	return validationError;
    }

    /**
     * @param validationError
     */
    public void setValidationError(String validationError) {

	this.validationError = validationError;
    }

    /**
     * @return the executionTimeLong
     */
    @XmlTransient
    public long getExecutionTimeLong() {

	return executionTimeLong;
    }

    /**
     * @param executionTimeLong
     */
    public void setExecutionTimeLong(long executionTimeLong) {

	this.executionTimeLong = executionTimeLong;
    }

    /**
     * @return the executionTime
     */
    @XmlTransient
    public String getExecutionTime() {

	return executionTime;
    }

    /**
     * @param executionTime
     */
    public void setExecutionTime(String executionTime) {

	this.executionTime = executionTime;
    }

}
