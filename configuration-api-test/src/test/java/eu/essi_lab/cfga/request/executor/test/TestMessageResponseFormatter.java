package eu.essi_lab.cfga.request.executor.test;

import javax.ws.rs.core.Response;

import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;

public class TestMessageResponseFormatter implements MessageResponseFormatter {

    @Override
    public Provider getProvider() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Response format(RequestMessage message, MessageResponse messageResponse) throws GSException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public FormattingEncoding getEncoding() {
	// TODO Auto-generated method stub
	return null;
    }

}
