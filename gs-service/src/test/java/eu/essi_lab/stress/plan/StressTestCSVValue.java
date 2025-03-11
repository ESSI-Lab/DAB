package eu.essi_lab.stress.plan;

/**
 * @author Mattia Santoro
 */
public class StressTestCSVValue {

    private final String value;
    private final String column;

    public StressTestCSVValue(String column, String value) {
	this.column = column;
	this.value = value;
    }

    public String getColumn() {
	return column;
    }

    public String getValue() {
	return value;
    }
}
