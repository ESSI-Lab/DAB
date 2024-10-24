package eu.essi_lab.accessor.bnhs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BNHSClientExternalTestIT {

    @Test
    public void test() throws Exception {
	BNHSClient client = new BNHSClient();

	Integer c = client.getColumnSize();
	Integer r = client.getRowSize();

	System.out.println("c" + c + " r " + r);
	for (int i = 0; i < r; i++) {
	    System.out.print("Row " + i + ":\t\t");
	    for (int j = 0; j < c; j++) {
		String v = client.getValue(i, j);
		System.out.print(v + "\t\t");
	    }
	    System.out.println();
	}

	assertTrue(c >= 46);
	assertTrue(r >= 429);

	String river = client.getValueByKey("6", "River"); // key is the HYCOSID
	System.out.println(river);
	assertEquals("FAIRFORD", river);

	String notes = client.getValueByKey("620", "Station Notes (INTERNAL USE ONLY from here on)");
	System.out.println(notes);
	assertTrue(notes.contains(";"));
	
	String value = client.getValue(0, 0);
	System.out.println(value);
	assertEquals("HYCOSID", value);

    }

}
