package eu.essi_lab.accessor.cehq;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import eu.essi_lab.accessor.cehq.CEHQClient.CEHQProperty;

public class CEHQClientExternalTestIT {

    @Test
    public void test() throws Exception {
	CEHQClient client = new CEHQClient();
	List<String> identifiers = client.getStationIdentifiers();
	System.out.println(identifiers.size());
	assertTrue(identifiers.size() > 700);
	for (String id : identifiers) {
	    System.out.println("ID: <" + id + ">");
	}
	String id = "080718";
	// Map<CEHQVariable, SimpleEntry<Date, Date>> temporalExtent = client.getTimeSeriesTemporalExtent(id);
	// for (CEHQVariable var : temporalExtent.keySet()) {
	// SimpleEntry<Date, Date> extent = temporalExtent.get(var);
	// System.out.println(var.getLabel());
	// System.out.println(extent.getKey());
	// System.out.println(extent.getValue());
	// }
	// assertEquals(2, temporalExtent.size());
	//
	// List<SimpleEntry<Date, BigDecimal>> data = client.getData(id, CEHQVariable.N, 1997);
	// SimpleEntry<Date, BigDecimal> first = data.get(0);
	// SimpleEntry<Date, BigDecimal> last = data.get(data.size() - 1);
	// assertEquals(new BigDecimal("26.869"), first.getValue());
	// assertEquals(new BigDecimal("25.952"), last.getValue());

	Map<CEHQProperty, String> props = client.getStationProperties(id);
	for (CEHQProperty prop : props.keySet()) {
	    System.out.println(prop + ": " + props.get(prop));
	}
    }

}
