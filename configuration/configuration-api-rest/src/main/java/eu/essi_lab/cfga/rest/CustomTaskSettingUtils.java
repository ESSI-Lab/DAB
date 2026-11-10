package eu.essi_lab.cfga.rest;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.task.*;
import eu.essi_lab.cfga.rest.task.*;
import eu.essi_lab.cfga.setting.*;

import jakarta.ws.rs.core.Response.*;
import java.util.*;

/**
 * @author boldrini
 */
public class CustomTaskSettingUtils {

    /**
     * @param request
     * @return
     */
    public static CustomTaskSetting build(PutCustomTaskRequest request) {

	CustomTaskSetting setting = new CustomTaskSetting();

	setting.setIdentifier(request.readString(PutCustomTaskRequest.TASK_ID).get());

	update(request, setting);

	SelectionUtils.deepClean(setting);

	return setting;
    }

    /**
     * @param request
     * @param target
     * @return
     */
    public static CustomTaskSetting build(EditCustomTaskRequest request, CustomTaskSetting target) {

	CustomTaskSetting setting = SettingUtils.downCast(SelectionUtils.resetAndSelect(target, false), CustomTaskSetting.class);

	update(request, setting);

	SelectionUtils.deepClean(setting);
	SelectionUtils.deepAfterClean(setting);

	return setting;
    }

    /**
     * @param request
     * @param setting
     */
    private static void update(PutCustomTaskRequest request, CustomTaskSetting setting) {

	request.readString(PutCustomTaskRequest.TASK_NAME).ifPresent(setting::selectTaskName);

	request.readString(PutCustomTaskRequest.TASK_DESCRIPTION).ifPresent(setting::setTaskDescription);

	request.readString(PutCustomTaskRequest.TASK_OPTIONS).ifPresent(options -> {

	    if (options.isEmpty()) {

		setting.clearTaskOptions();

	    } else {

		setting.setTaskOptions(options);
	    }
	});

	if (request.read(PutCustomTaskRequest.EMAIL_RECIPIENTS).isPresent()) {

	    List<String> recipients = request.readStrings(PutCustomTaskRequest.EMAIL_RECIPIENTS).//
		    stream().//
		    filter(r -> !r.isEmpty()).//
		    toList();

	    if (recipients.isEmpty()) {

		setting.clearEmailRecipients();

	    } else {

		setting.setEmailRecipients(recipients.toArray(String[]::new));
	    }
	}
    }

    /**
     * @param taskId
     * @return
     */
    public static Optional<CustomTaskSetting> find(String taskId) {

	return ConfigurationWrapper.getCustomTaskSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(taskId)).//
		findFirst();
    }

    /**
     * @param request
     * @return
     */
    public static SettingFinder<CustomTaskSetting> getCustomTaskSettingFinder(ConfigRequest request) {

	Optional<String> optTaskId = request.readString(PutCustomTaskRequest.TASK_ID);

	if (optTaskId.isEmpty()) {

	    return new SettingFinder<>(ConfigRequest.buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing custom task identifier"));
	}

	String taskId = optTaskId.get();

	return find(taskId).//
		map(SettingFinder::new).//
		orElseGet(() -> new SettingFinder<>(
			ConfigRequest.buildErrorResponse(Status.NOT_FOUND, "Custom task with id '" + taskId + "' not found")));
    }
}
