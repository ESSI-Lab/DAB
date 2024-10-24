package eu.essi_lab.request.executor.schedule;

import javax.ws.rs.core.Response;

import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.ProfilerHandler;
import eu.essi_lab.request.executor.IRequestExecutor;

@SuppressWarnings("rawtypes")
public class FakeProfilerHandler extends ProfilerHandler {

    @Override
    protected IRequestExecutor createExecutor() {
	return new IRequestExecutor() {

	    @Override
	    public AbstractCountResponse count(RequestMessage message) throws GSException {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public MessageResponse retrieve(RequestMessage message) throws GSException {
		System.out.println("It executed fine!");
		MessageResponse ret = new MessageResponse() {
		    @Override
		    public String getName() {
			return "FAKE";
		    }
		};
		return ret;
	    }

	    @Override
	    public boolean isAuthorized(RequestMessage message) throws GSException {
		// TODO Auto-generated method stub
		return true;
	    }
	};
    }

    @Override
    protected Response handleNotAuthorizedRequest(RequestMessage message) {
	// TODO Auto-generated method stub
	return null;
    }
}
