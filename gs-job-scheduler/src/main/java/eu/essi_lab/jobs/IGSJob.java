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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.jobs.scheduler.GS_JOB_INTERVAL_PERIOD;
import eu.essi_lab.model.exceptions.GSException;
public interface IGSJob extends Serializable {

    Date getStartDate();

    int getInterval();

    boolean completed();

    void completed(boolean value);

    GS_JOB_INTERVAL_PERIOD getIntervalPeriod();

    String getGroup();

    void setGroup(String jobGroup);

    String getId();

    String getInstantiableClass();

    void run(Map<String, Object> jobDataMap, Boolean isRecovering, Optional<GSJobStatus> jobStatus) throws GSException;

    /**
     * This method is used to double-check that an {@link IGSJob} is valid, i.e. it is not a "zombie" scheduling (e.g. a job which had been
     * scheduled with an old configuration).
     *
     * @return
     */
    GSJobValidationResult isValid(Map<String, Object> jobDataMap);

    void setId(String id);

}
