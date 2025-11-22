package eu.essi_lab.cfga.check;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class CheckResponse {

    /**
     * 
     */
    private final Set<String> messages;

    /**
     * 
     */
    private final String methodName;

    /**
     * 
     */
    private final List<Setting> settings;

    /**
     * 
     */
    private CheckResult checkResult;

    /**
     * @author Fabrizio
     */
    public enum CheckResult {
	/**
	 * 
	 */
	CHECK_FAILED,
	/**
	 * 
	 */
	CHECK_SUCCESSFUL
    }

    /**
     * 
     */
    public CheckResponse(String methodName) {

	this.messages = new HashSet<>();
	this.settings = new ArrayList<>();

	this.methodName = methodName;

	setCheckResult(CheckResult.CHECK_SUCCESSFUL);
    }

    /**
     * @param checkResult
     */
    public void setCheckResult(CheckResult checkResult) {

	this.checkResult = checkResult;
    }

    /**
     * @return
     */
    public CheckResult getCheckResult() {

	return checkResult;
    }

    /**
     * @return
     */
    public Set<String> getMessages() {

	return messages;
    }

    /**
     * @return
     */
    public String getMethodName() {

	return methodName;
    }

    /**
     * @return
     */
    public List<Setting> getSettings() {

	return settings;
    }

}
