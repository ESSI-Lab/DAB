package eu.essi_lab.gssrv.rest;

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

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.configuration.*;
import eu.essi_lab.gssrv.health.*;
import eu.essi_lab.lib.utils.Chronometer.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.*;
import eu.essi_lab.pdk.*;
import eu.essi_lab.rip.*;
import eu.essi_lab.shared.driver.es.stats.*;
import jakarta.jws.*;
import jakarta.servlet.http.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.*;
import jakarta.xml.ws.*;

import java.io.*;
import java.lang.management.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
@WebService
@Path("/")
public class HealthCheckService implements RuntimeInfoProvider {

    /**
     *
     */
    private static final Integer DEFAULT_DISK_SIZE_THRESHOLD = 5;

    /**
     *
     */
    private static final Integer DEFAULT_THREADS_COUNT_THRESHOLD = 300;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check")
    public Response health(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wsContext) {

	WebRequest webRequest = new WebRequest();

	webRequest.setUriInfo(uriInfo);

	try {
	    webRequest.setServletRequest(hsr);

	} catch (IOException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	//
	//
	//

	ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	chronometer.start();

	HealthCheck healthCheck = new HealthCheck();

	boolean healthy = new HealthCheck().isHealthy();

	checkDiskSpace();

	checkThreadsCount();

	Status status = responseStatus(healthy);

	Response response = Response.//
		status(status).//
		type(MediaType.APPLICATION_JSON_TYPE).//
		entity(healthCheck.toJson(healthy, false)).build();

	String elapsedTime = chronometer.formatElapsedTime();

	GSLoggerFactory.getLogger(getClass()).trace("HealthCheck elapsed time: {}", elapsedTime);

	//
	//
	//

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(webRequest);

	if (publisher.isPresent()) {

	    try {

		publisher.get().publish(webRequest);
		publisher.get().publish(chronometer);
		publisher.get().publish(new RuntimeInfoProvider() {

		    @Override
		    public HashMap<String, List<String>> provideInfo() {
			HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
			ret.put(RuntimeInfoElement.RESPONSE_STATUS.getName(), List.of("" + status.getStatusCode()));
			return ret;
		    }

		    @Override
		    public String getName() {
			return getBaseType();
		    }
		});

		publisher.get().write();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	return response;
    }

    /**
     *
     */
    private void checkThreadsCount() {

	int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

	GSLoggerFactory.getLogger(getClass()).info("Threads count: {}", threadCount);

	Integer threshold = ConfigurationWrapper.getSystemSettings().//
		readKeyValue(SystemSetting.KeyValueOptionKeys.THREADS_COUNT_THRESHOLD).//
		map(Integer::parseInt).//
		orElse(DEFAULT_THREADS_COUNT_THRESHOLD);

	if (threadCount > threshold) {

	    GSLoggerFactory.getLogger(getClass()).error("Warning: the number of threads {} is greater than {}", threadCount, threshold);

	    Map<String, Long> byPrefix = Thread.getAllStackTraces(). //
		    keySet().//
		    stream().//
		    collect(Collectors.groupingBy(Thread::getName, Collectors.counting()));

	    ExecutionMode executionMode = ExecutionMode.get();

	    String hostname = HostNamePropertyUtils.getHostNameProperty();

	    StringBuilder stringBuilder = new StringBuilder();

	    stringBuilder.append(
		    "Warning: the number of threads "+threadCount+" is greater than " + threshold + ". " + hostname + " (" + executionMode + ")\n\n");

	    byPrefix.entrySet().//
		    stream().//
		    sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).//
		    forEach(entry -> stringBuilder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n"));

	    GSLoggerFactory.getLogger(getClass()).error(stringBuilder.toString());

	    ConfiguredSMTPClient.sendEmail(ConfiguredSMTPClient.MAIL_ALARM, "Threads count alert \n\n" + stringBuilder.toString());
	}
    }

    /**
     *
     */
    private void checkDiskSpace() {

	try {

	    java.nio.file.Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
	    FileStore fileStore = Files.getFileStore(tempDir);

	    long availableSpace = fileStore.getUsableSpace();
	    long gb = getGB(availableSpace);

	    GSLoggerFactory.getLogger(getClass()).info("Available space on temporary folder: {} GB", gb);

	    Integer threshold = ConfigurationWrapper.getSystemSettings().//
		    readKeyValue(SystemSetting.KeyValueOptionKeys.DISK_SIZE_THRESHOLD).//
		    map(Integer::parseInt).//
		    orElse(DEFAULT_DISK_SIZE_THRESHOLD);

	    if (gb < threshold) {

		GSLoggerFactory.getLogger(getClass()).error("Warning: the disk free space is below {} GB", threshold);

		if (gb < 1) {

		    ExecutionMode executionMode = ExecutionMode.get();

		    String hostname = HostNamePropertyUtils.getHostNameProperty();

		    String alarmMessage =
			    "Warning: the disk free space is below " + threshold + "GB. " + hostname + " (" + executionMode + ")";

		    GSLoggerFactory.getLogger(getClass()).error(alarmMessage);

		    ConfiguredSMTPClient.sendEmail(ConfiguredSMTPClient.MAIL_ALARM, "Disk space alert \n\n" + alarmMessage);
		}
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    /**
     * @param bytes
     * @return
     */
    private long getGB(long bytes) {
	bytes /= 1024;
	bytes /= 1024;
	bytes /= 1024;
	return bytes;
    }

    /**
     * @param healthy
     * @return
     */
    private Response.Status responseStatus(boolean healthy) {

	if (healthy) {
	    return Response.Status.OK;
	}

	return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	return new HashMap<>();
    }

    @Override
    public String getName() {

	return getClass().getSimpleName();
    }

    @Override
    public String getBaseType() {

	return "HealthCheck";
    }
}
