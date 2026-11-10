package eu.essi_lab.lib.skos.llm.service;

/**
 * @author Fabrizio
 */
public class TermExpansionException extends RuntimeException {

    /**
     * @param message
     * @param cause
     */
    public TermExpansionException(String message, Throwable cause) {
	super(message, cause);
    }
}
