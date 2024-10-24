package eu.essi_lab.accessor.cdi.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TestResult {
    int success = 0;
    int errors = 0;
    int skip = 0;

    HashMap<String, HashSet<String>> errorMap = new HashMap<String, HashSet<String>>();

    public HashMap<String, HashSet<String>> getErrorMap() {
	return errorMap;
    }

    public void skip() {
	skip++;
    }

    public void success() {
	success++;
    }

    public void error() {
	errors++;
    }

    public void printReport() {
	System.out.println(getReport());
    }

    public String getReport() {
	String ret = "Valid documents: " + success + "\n";
	ret += "Skipped documents: " + skip + "\n";
	ret += "Documents with errors: " + errors + "\n";

	Set<String> errorTypes = errorMap.keySet();

	ret += errorTypes.size() + " different error types" + "\n";

	for (String errorType : errorTypes) {
	    ret += errorType + "\n";
	    HashSet<String> identifiers = errorMap.get(errorType);
	    for (String identifier : identifiers) {
		System.out.println(identifier);
	    }
	    ret += "\n";
	    ret += "***\n";
	    ret += "\n";
	}

	ret += "\n";
	return ret;
    }
}
