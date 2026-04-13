package eu.essi_lab.services.data_hub.test;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

/**
 * @author Fabrizio
 */
public class KafkaGroupDescriptor {

    public static void main(String[] args) throws Exception {

	String groupId = "GROUP_ID";

	Properties props = new Properties();

	props.put("bootstrap.servers", "SERVER");
	props.put("group.id", groupId);

	props.put("enable.auto.commit", "false");
	props.put("auto.offset.reset", "latest");
	props.put("security.protocol", "SASL_SSL");
	props.put("sasl.mechanism", "SCRAM-SHA-512");

	String user = "USER";
	String pwd = "PWD";

	props.put("sasl.jaas.config",
		"org.apache.kafka.common.security.scram.ScramLoginModule required " + "username=\"" + user + "\" " + "password=\"" + pwd
			+ "\";");

	props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	try (AdminClient admin = AdminClient.create(props)) {

	    DescribeConsumerGroupsResult result = admin.describeConsumerGroups(List.of(groupId));

	    ConsumerGroupDescription group = result.describedGroups().get(groupId).get();

	    System.out.println("Group: " + group.groupId());

	    for (MemberDescription member : group.members()) {

		System.out.println("\nMember: " + member.consumerId());

		Set<TopicPartition> partitions = member.assignment().topicPartitions();

		System.out.println("Assigned partitions: " + partitions);
	    }
	}
    }
}
