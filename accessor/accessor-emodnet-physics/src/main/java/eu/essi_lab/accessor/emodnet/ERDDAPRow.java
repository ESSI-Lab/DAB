package eu.essi_lab.accessor.emodnet;

public class ERDDAPRow {

    String[] headers = null;
    private Object[] values;

    public ERDDAPRow(String[] headers, Object[] values) {
	this.headers = headers;
	this.values = values;
    }

    public Object getValue(String header) {
	Integer f = null;
	for (int i = 0; i < headers.length; i++) {
	    if (headers[i].equals(header)) {
		f = i;
		break;
	    }
	}
	if (f == null) {
	    return null;
	}
	return values[f];
    }

    public String[] getHeaders() {
	return headers;

    }

    @Override
    public String toString() {
	String ret = "";
	for (int i = 0; i < headers.length; i++) {
	    String header = headers[i];
	    ret += header + ": " + values[i].toString()+"\n";
	}
	return ret;
    }

}
