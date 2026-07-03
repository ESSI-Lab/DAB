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

import org.jspecify.annotations.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.*;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ecs.*;
import software.amazon.awssdk.services.ecs.model.*;
import software.amazon.awssdk.services.ecs.paginators.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class ECSExternalLinkFinder implements Closeable {

    /**
     * @param args
     */
    public static void main(String[] args) {

	try (ECSExternalLinkFinder finder = new ECSExternalLinkFinder(Region.US_EAST_1)) {

	    List<ExternalLink> links = finder.getExternalLinks(List.of(

		    "GSServiceProductionCluster", //
		    "GSServiceProductionHarvestCluster",//
		    "GSServiceProductionIntensiveCluster",//
		    "GSServiceProductionAccessCluster",//
		    "GSServiceProductionAugmentCluster"//
	    ));

	    links.forEach(System.out::println);

	    System.exit(0);
	}
    }

    private final EcsClient ecsClient;
    private final Ec2Client ec2Client;

    /**
     * @param region
     */
    public ECSExternalLinkFinder(Region region) {

	this.ecsClient = EcsClient.builder(). //
		region(region).//
		build();//

	this.ec2Client = Ec2Client.builder().//
		region(region).//
		build();//
    }

    /**
     * @param clusterNames
     * @return
     */
    public List<ExternalLink> getExternalLinks(List<String> clusterNames) {

	List<ExternalLink> links = new ArrayList<>();

	clusterNames.parallelStream().forEach(clusterName -> {

	    ClusterContext ctx = loadClusterContext(clusterName);

	    links.addAll(buildExternalLinks(ctx, clusterName));
	});

	Collections.sort(links);

	return links;
    }

    @Override
    public void close() {

	ecsClient.close();
	ec2Client.close();
    }

    /**
     * @param clusterName
     * @return
     */
    private ClusterContext loadClusterContext(String clusterName) {

	ClusterContext ctx = new ClusterContext();

	//
	// Task ARN
	//
	List<String> taskArns = listRunningTaskArns(clusterName);

	if (taskArns.isEmpty()) {

	    return ctx;
	}

	//
	// DescribeTasks
	//
	DescribeTasksResponse describeTasks = ecsClient.describeTasks( //

		DescribeTasksRequest.builder().//
			cluster(clusterName).//
			tasks(taskArns).//
			build()//
	);

	ctx.tasks.addAll(describeTasks.tasks());

	//
	// ContainerInstance ARN
	//
	Set<String> containerInstanceArns = describeTasks.//
		tasks().//
		stream().//
		map(Task::containerInstanceArn).//
		filter(Objects::nonNull).//
		collect(Collectors.toSet());

	if (containerInstanceArns.isEmpty()) {

	    return ctx;
	}

	//
	// DescribeContainerInstances
	//
	DescribeContainerInstancesResponse describeContainerInstances = ecsClient.describeContainerInstances(//

		DescribeContainerInstancesRequest.builder().//
			cluster(clusterName).//
			containerInstances(containerInstanceArns).//
			build());

	Set<String> ec2InstanceIds = new HashSet<>();

	for (ContainerInstance ci : describeContainerInstances.containerInstances()) {

	    ctx.containerInstanceToEc2.put(ci.containerInstanceArn(), ci.ec2InstanceId());

	    ec2InstanceIds.add(ci.ec2InstanceId());
	}

	if (ec2InstanceIds.isEmpty()) {

	    return ctx;
	}

	//
	// DescribeInstances
	//
	DescribeInstancesResponse describeInstances = ec2Client.describeInstances(//

		DescribeInstancesRequest.builder().//
			instanceIds(ec2InstanceIds).//
			build()//
	);

	for (Reservation reservation : describeInstances.reservations()) {

	    for (Instance instance : reservation.instances()) {

		ctx.ec2ToPublicIp.put(instance.instanceId(), instance.publicIpAddress());
	    }
	}

	return ctx;
    }

    /**
     * @param clusterName
     * @return
     */
    private List<String> listRunningTaskArns(String clusterName) {

	List<String> taskArns = new ArrayList<>();

	ListTasksRequest request = ListTasksRequest.builder(). //
		cluster(clusterName).//
		desiredStatus(DesiredStatus.RUNNING).//
		build();

	ListTasksIterable iterable = ecsClient.listTasksPaginator(request);

	for (ListTasksResponse response : iterable) {

	    taskArns.addAll(response.taskArns());
	}

	return taskArns;
    }

    /**
     * @param ctx
     * @return
     */
    private List<ExternalLink> buildExternalLinks(ClusterContext ctx, String clusterName) {

	Set<ExternalLink> links = new HashSet<>();

	for (Task task : ctx.tasks) {

	    String ec2InstanceId = ctx.containerInstanceToEc2.get(task.containerInstanceArn());

	    if (ec2InstanceId == null) {

		continue;
	    }

	    String publicIp = ctx.ec2ToPublicIp.get(ec2InstanceId);

	    if (publicIp == null || publicIp.isBlank()) {

		continue;
	    }

	    for (Container container : task.containers()) {

		if (container.networkBindings() == null) {

		    continue;
		}

		for (NetworkBinding binding : container.networkBindings()) {

		    Integer hostPort = binding.hostPort();

		    if (hostPort != null && hostPort > 0) {

			links.add(new ExternalLink(//
				task.taskArn().substring(task.taskArn().lastIndexOf("/") + 1), //
				clusterName, //
				task.group().replace("service:", ""), //
				publicIp + ":" + hostPort) //
			);
		    }
		}
	    }
	}

	return new ArrayList<>(links);
    }

    /**
     * @param taskName
     * @param cluster
     * @param service
     * @param externalLink
     * @author Fabrizio
     */
    public record ExternalLink(String taskName, String cluster, String service, String externalLink) implements Comparable<ExternalLink> {

	@Override
	public boolean equals(Object obj) {

	    return false;
	}

	@Override
	public int hashCode() {

	    return toString().hashCode();
	}

	@Override
	public @NonNull String toString() {

	    return "Task name: " + taskName + "\nCluster: " + cluster + "\nService: " + service + "\nExternal link: " + externalLink + "\n";
	}

	@Override
	public int compareTo(ECSExternalLinkFinder.ExternalLink o) {

	    return o.cluster.compareTo(this.cluster);
	}
    }

    /**
     * @author Fabrizio
     */
    private static class ClusterContext {

	private final List<Task> tasks = new ArrayList<>();

	/**
	 * containerInstanceArn -> ec2InstanceId
	 */
	private final Map<String, String> containerInstanceToEc2 = new HashMap<>();

	/**
	 * ec2InstanceId -> publicIp
	 */
	private final Map<String, String> ec2ToPublicIp = new HashMap<>();
    }

}
