/**
 * 
 */
package eu.essi_lab.cfga.gs.task;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;

/**
 * @author Fabrizio
 */
public abstract class AbstractEmbeddedTask extends AbstractCustomTask implements HarvestingEmbeddedTask {

    private ListRecordsRequest request;

    @Override
    public void setListRecordsRequest(ListRecordsRequest request) {

	this.request = request;
    }

    @Override
    public ListRecordsRequest getListRecordsRequest() {

	return this.request;
    }

}
