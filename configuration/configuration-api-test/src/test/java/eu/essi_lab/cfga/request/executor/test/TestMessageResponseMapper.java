package eu.essi_lab.cfga.request.executor.test;

import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;

public class TestMessageResponseMapper implements MessageResponseMapper {

    @Override
    public Provider getProvider() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MessageResponse map(RequestMessage message, MessageResponse messageResponse) throws GSException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MappingSchema getMappingSchema() {
	// TODO Auto-generated method stub
	return null;
    }

}
