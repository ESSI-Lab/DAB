/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

/**
 * @author Fabrizio
 */
public class ConfigurationMapping extends IndexMapping {

    public static final String CONFIGURATION_INDEX = "configuration-index";

    /**
     * @return
     */
    public static final ConfigurationMapping get() {

	return new ConfigurationMapping();
    }

    /**
     * 
     */
    private ConfigurationMapping() {

	super(CONFIGURATION_INDEX);

	addProperty("name", "keyword");
	addProperty("configuration", "binary");
    }
}
