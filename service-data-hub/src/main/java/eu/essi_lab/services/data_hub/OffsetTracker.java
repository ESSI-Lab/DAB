package eu.essi_lab.services.data_hub;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
class OffsetTracker {

    /**
     *
     */
    private final Map<TopicPartition, PartitionState> state = new ConcurrentHashMap<>();

    /**
     * @author Fabrizio
     */
    private static class PartitionState {

	long nextCommitOffset = -1;
	SortedSet<Long> completed = new TreeSet<>();
    }

    /**
     * @return
     */
    Map<TopicPartition, OffsetAndMetadata> buildOffsets() {

	Map<TopicPartition, Long> computed = computeCommitOffsets();

	Map<TopicPartition, OffsetAndMetadata> result = new HashMap<>();

	for (Map.Entry<TopicPartition, Long> e : computed.entrySet()) {

	    result.put(e.getKey(), new OffsetAndMetadata(e.getValue()));
	}

	return result;
    }

    /**
     * @param partitions
     */
    void onPartitionAssigned(Collection<TopicPartition> partitions) {

	for (TopicPartition tp : partitions) {

	    state.putIfAbsent(tp, new PartitionState());
	}
    }

    /**
     * @param record
     */
    void markProcessed(ConsumerRecord<byte[], byte[]> record) {

	markProcessed(new TopicPartition(//
			record.topic(),//
			record.partition()),//
		record.offset());
    }

    /**
     * @param tp
     * @param offset
     */
    void markProcessed(TopicPartition tp, long offset) {

	PartitionState ps = state.get(tp);

	synchronized (ps) {

	    ps.completed.add(offset);
	}
    }

    /**
     * @return
     */
    Map<TopicPartition, Long> computeCommitOffsets() {

	Map<TopicPartition, Long> result = new HashMap<>();

	for (Map.Entry<TopicPartition, PartitionState> entry : state.entrySet()) {

	    TopicPartition tp = entry.getKey();
	    PartitionState ps = entry.getValue();

	    synchronized (ps) {

		if (ps.nextCommitOffset == -1 && !ps.completed.isEmpty()) {

		    ps.nextCommitOffset = ps.completed.first();
		}

		long next = ps.nextCommitOffset;

		while (ps.completed.contains(next)) {

		    ps.completed.remove(next);
		    next++;
		}

		if (next > ps.nextCommitOffset) {

		    result.put(tp, next);
		    ps.nextCommitOffset = next;
		}
	    }
	}

	return result;
    }
}
