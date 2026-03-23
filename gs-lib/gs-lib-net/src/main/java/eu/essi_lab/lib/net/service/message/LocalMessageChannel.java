package eu.essi_lab.lib.net.service.message;

import eu.essi_lab.lib.utils.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class LocalMessageChannel implements MessageChannel {

    /**
     *
     */
    private static final Map<String, List<Message>> MESSAGE_MAP = new ConcurrentHashMap<>();
    private final int channelSize;

    /**
     *
     */
    public LocalMessageChannel() {

	this.channelSize = 100;
    }

    /**
     * @param channelSize
     */
    public LocalMessageChannel(int channelSize) {

	this.channelSize = channelSize;
    }

    /**
     * @param serviceId
     * @param level
     * @param message
     */
    @Override
    public void publish(String serviceId, MessageLevel level, String message) {

	List<Message> messages = MESSAGE_MAP.computeIfAbsent(serviceId, k -> new ArrayList<>());

	messages.add(Message.of(serviceId, level, message));

	MESSAGE_MAP.put(serviceId, ListUtils.lastN(messages, channelSize));
    }

    /**
     * @param serviceId
     * @return
     */
    @Override
    public List<Message> read(String serviceId) {

	return read_(serviceId);
    }

    /**
     * @param serviceId
     * @param minLevel
     * @return
     */
    @Override
    public List<Message> read(String serviceId, MessageLevel minLevel) {

	return read_(serviceId).//
		stream().//
		filter(m -> m.getLevel().ordinal() > minLevel.ordinal()).//
		toList();//
    }

    /**
     * @param serviceId
     * @param max
     * @return
     */
    @Override
    public List<Message> read(String serviceId, int max) {

	return ListUtils.lastN(read_(serviceId), max);
    }

    /**
     * @param serviceId
     * @param max
     * @param minLevel
     * @return
     */
    @Override
    public List<Message> read(String serviceId, int max, MessageLevel minLevel) {

	return ListUtils.lastN(read_(serviceId).//
		stream().//
		filter(m -> m.getLevel().ordinal() > minLevel.ordinal()).//
		toList(), max);//
    }

    /**
     * @param serviceId
     * @return
     */
    private List<Message> read_(String serviceId) {

	List<Message> messages = MESSAGE_MAP.get(serviceId);

	return messages == null ? new ArrayList<>() :
		messages.stream(). //
			sorted(Comparator.comparing(MessageChannel.Message::getTimestamp)).//
			toList();//
    }

    /**
     * @param serviceId
     */
    @Override
    public void removeAll(String serviceId) {

	MESSAGE_MAP.remove(serviceId);
    }

    @Override
    public int size(String serviceId) {

	List<Message> messages = MESSAGE_MAP.get(serviceId);

	return messages == null ? 0 : messages.size();
    }
}
