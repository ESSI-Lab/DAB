package eu.essi_lab.lib.skos.llm.client;

/**
 * @author Fabrizio
 */
public class OpenRouterApiException extends RuntimeException {

    private final int httpStatusCode;

    /**
     *
     * @param message
     * @param httpStatusCode
     */
    public OpenRouterApiException(String message, int httpStatusCode) {
	super(message);
	this.httpStatusCode = httpStatusCode;
    }

    /**
     *
     * @return
     */
    public int httpStatusCode() {
	return httpStatusCode;
    }
}
