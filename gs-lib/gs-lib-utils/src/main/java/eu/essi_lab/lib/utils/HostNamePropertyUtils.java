package eu.essi_lab.lib.utils;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.PropertyConfigurator;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author Fabrizio
 */
public class HostNamePropertyUtils {

    private static String hostname;

    /**
     * 
     */
    public static void setHostNameProperty() {

	String hostName = getHostNameProperty();

	System.out.println("Setting HostName property: " + hostName);

	System.setProperty("HostName", hostName);

	PropertyConfigurator.configure(GSLoggerFactory.class.getClassLoader().getResource("log4j.properties"));
    }

    /**
     * @return
     */
    public static String getHostNameProperty() {

	if (hostname == null) {

	    try {
		hostname = InetAddress.getLocalHost().getHostName();
	    } catch (UnknownHostException e) {

		hostname = "unknown";

		GSLoggerFactory.getLogger(HostNamePropertyUtils.class).error(e.getMessage());
	    }
	}

	return hostname;
    }


    public static Optional<String> getAWSTaskId() {

	try {
	    String metadataUri = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
	    if (metadataUri == null || metadataUri.isBlank()) {
		return Optional.empty();
	    }

	    URL url = new URL(metadataUri + "/task");

	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");

	    String json = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode root = mapper.readTree(json);

	    String taskArn = root.get("TaskARN").asText();

	    String taskId = taskArn.substring(taskArn.lastIndexOf('/') + 1);

	    return Optional.of(taskId);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(HostNamePropertyUtils.class).debug("ECS task id not available: {}", e.getMessage());
	}
	return Optional.empty();
    }

    private static final String ECS_HARVEST_CLUSTER = "GSServiceProductionHarvestCluster";
    private static final String ECS_REGION = "us-east-1";

    /**
     * @param taskId
     * @return
     */
    public static Optional<String> getEcsTaskLogsUrl(String taskId) {

	if (taskId == null || taskId.isBlank()) {
	    return Optional.empty();
	}

	return Optional.of("https://" + ECS_REGION + ".console.aws.amazon.com/ecs/v2/clusters/" + ECS_HARVEST_CLUSTER + "/tasks/"
		+ taskId + "/logs?region=" + ECS_REGION);
    }
}
