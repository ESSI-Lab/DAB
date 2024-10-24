package eu.essi_lab.cfga.request.executor.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.handler.ProfilerHandler;
import eu.essi_lab.request.executor.IRequestExecutor;

@SuppressWarnings("rawtypes")
public class TestProfilerHandler extends ProfilerHandler {

    public static List<Integer> list = Collections.synchronizedList(new ArrayList<Integer>());

    @SuppressWarnings("rawtypes")
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
		int id = System.identityHashCode(this);
		String preamble = TestProfilerHandler.class.getName() + "(" + id + "): ";
		System.out.println(preamble + "request received");
		System.out.println(preamble + "SLEEP");
		try {
		    Thread.sleep((long) (Math.random() * 100.0));
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		System.out.println(preamble + "SLEPT");
		if (message instanceof DiscoveryMessage) {
		    DiscoveryMessage dm = (DiscoveryMessage) message;
		    Bond bond = dm.getUserBond().get();
		    if (bond instanceof SimpleValueBond) {
			SimpleValueBond svb = (SimpleValueBond) bond;
			String value = svb.getPropertyValue();
			Integer i = Integer.parseInt(value);
			list.add(i);
			System.out.println(preamble + "result is " + i);
			ResultSet<GSResource> rs = new ResultSet<>();
			List<GSResource> results = new ArrayList<>();
			for (int j = 0; j < i; j++) {
			    GSResource res = new Dataset();
			    res.setOriginalId(UUID.randomUUID().toString());
			    results.add(res);
			}
			rs.setResultsList(results);
			return rs;
		    }
		}
		return null;
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
