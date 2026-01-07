package eu.essi_lab.cfga.gs.task;

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

import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public abstract class AbstractCustomTask implements CustomTask {

    /**
     * 
     */
    protected Optional<S3TransferWrapper> manager;
    private GSSource source;

    /**
     * @param source
     */
    @Override
    public void setSource(GSSource source) {

	this.source = source;
    }

    /**
     * @return
     */
    @Override
    public Optional<GSSource> getSource() {

	return Optional.ofNullable(source);
    }

    /**
     * @param status
     * @param message
     */
    protected void log(SchedulerJobStatus status, String message) {

	log(status, message, true);
    }

    /**
     * @param status
     * @param message
     */
    protected void log(SchedulerJobStatus status, String message, boolean printLog) {

	if (printLog) {
	    GSLoggerFactory.getLogger(getClass()).debug(message);
	}

	status.addInfoMessage(message);
    }

    /**
     * @return
     */
    protected Optional<S3TransferWrapper> getS3TransferManager() {

	if (this.manager == null) {

	    Optional<S3TransferWrapper> optional = ConfigurationWrapper.getS3TransferWrapper();

	    this.manager = optional;
	}

	return this.manager;
    }
}
