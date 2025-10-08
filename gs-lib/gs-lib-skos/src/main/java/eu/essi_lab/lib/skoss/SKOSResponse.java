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
public class SKOSResponse {

    private List<SKOSResponseItem> results;

    /**
     * @param results
     */
    private SKOSResponse(List<SKOSResponseItem> results) {

	this.results = results;
    }

    /**
     * @param results
     * @return
     */
    public static SKOSResponse of(List<SKOSResponseItem> results) {

	return new SKOSResponse(results);
    }

    /**
     * @return the results
     */
    public List<SKOSResponseItem> getResults() {

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
