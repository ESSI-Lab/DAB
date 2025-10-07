/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabrizio
 */
public class SKOSSResponse {

    private List<SKOSSResponseItem> results;

    /**
     * @param results
     */
    private SKOSSResponse(List<SKOSSResponseItem> results) {

	this.results = results;
    }

    /**
     * @param results
     * @return
     */
    public static SKOSSResponse of(List<SKOSSResponseItem> results) {

	return new SKOSSResponse(results);
    }

    /**
     * @return the results
     */
    public List<SKOSSResponseItem> getResults() {

	return results;
    }

    /**
     * @return
     */
    public List<String> getLabels() {

	return Stream.concat(//
		getPrefLabels().stream(), //
		getAltLabels().stream()).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public List<String> getPrefLabels() {

	return getResults().//
		stream().//
		filter(r -> r.getPref().isPresent()).//
		map(r -> r.getPref().get()).//
		distinct().//
		sorted().//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public List<String> getAltLabels() {

	return getResults().//
		stream().//
		filter(r -> r.getAlt().isPresent()).//
		map(r -> r.getAlt().get()).//
		distinct().//
		sorted().//
		collect(Collectors.toList());
    }
}
