/**
 * 
 */
package eu.essi_lab.cfga.gs.task;

/**
 * This interface must be implemented by harvesting embedded tasks
 * 
 * @author Fabrizio
 */
public interface HarvestingEmbeddedTask extends CustomTask {

    /**
     * @author Fabrizio
     */
    public enum ExecutionStage {
	/**
	 * 
	 */
	BEFORE_HARVESTING_END,
	/**
	 * 
	 */
	AFTER_HARVESTING_END;
    }

    /**
     * @return
     */
    public ExecutionStage getExecutionStage();
}
