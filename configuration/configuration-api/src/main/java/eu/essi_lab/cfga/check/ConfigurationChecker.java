package eu.essi_lab.cfga.check;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;

/**
 * @author Fabrizio
 */
public class ConfigurationChecker {

    /**
     * 
     */
    private final List<CheckResponse> responseList;

    /**
     * 
     */
    private final List<CheckMethod> methodsList;

    /**
     * 
     */
    public ConfigurationChecker() {

	responseList = new ArrayList<>();
	methodsList = new ArrayList<>();
    }

    /**
     * @param method
     */
    public void addCheckMethod(CheckMethod method) {

	methodsList.add(method);
    }

    /**
     * Executes the {@link CheckMethod}s in the order of insertion and collects the {@link CheckResponse}s
     * 
     * @see #addCheckMethod(CheckMethod)
     * @param configuration
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<CheckResponse> check(Configuration configuration) {

	for (CheckMethod method : methodsList) {

	    CheckResponse error = method.check(configuration);
	    responseList.add(error);
	}

	return new ArrayList(responseList);
    }

    /**
     * @param configuration
     * @return
     */
    public CheckResult getCheckResult(Configuration configuration) {

	return check(configuration).//
		stream().//
		filter(r -> r.getCheckResult() == CheckResult.CHECK_FAILED).//
		map(CheckResponse::getCheckResult).//
		findFirst().//
		orElse(CheckResult.CHECK_SUCCESSFUL);
    }

    /**
     * @param responseList
     * @return
     */
    public static List<String> getErrors(List<CheckResponse> responseList) {

	return responseList.//
		stream().//
		flatMap(r -> r.getMessages().stream()).//
		collect(Collectors.toList());
    }

}
