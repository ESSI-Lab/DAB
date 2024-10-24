package eu.essi_lab.accessor.nrfa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author boldrini
 *
 */
public class NRFAClientExternalTestIT {

    @Test
    public void test() {
	NRFAClient client = new NRFAClient();
	List<String> identifiers = client.getStationIdentifiers();
	assertTrue(identifiers.size() > 100);
	StationInfo station = client.getStationInfo("43009");
	assertEquals("43009", station.getIdentifier());
	assertEquals("Stour at Hammoon", station.getName());
	assertEquals("Stour", station.getRiver());
	assertEquals("Hammoon", station.getLocation());
	assertEquals("50.93139", station.getLatitude());
	assertEquals("-2.25744", station.getLongitude());
	ParameterInfo parameter = station.getParameterInfos().get(0);
	long sevenDays = 1000 * 60 * 60 * 24 * 7l;
	List<SimpleEntry<Date, BigDecimal>> values = client.getValues("43009", parameter.getIdentifier(), parameter.getBegin(),
		new Date(parameter.getBegin().getTime() + sevenDays));
	for (SimpleEntry<Date, BigDecimal> value : values) {
	    System.out.println(value.getKey() + ": " + value.getValue());
	}
	assertTrue(values.size() == 8);

    }

}
