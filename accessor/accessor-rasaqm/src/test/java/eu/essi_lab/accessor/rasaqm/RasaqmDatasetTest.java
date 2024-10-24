package eu.essi_lab.accessor.rasaqm;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class RasaqmDatasetTest {

    @Test
    public void test() throws JAXBException {
	RasaqmDataset dataset = new RasaqmDataset();
	dataset.setParameterId("PID");
	dataset.setParameterName("PNAME");
	RasaqmSeries station = new RasaqmSeries();
	station.setLatitude(new BigDecimal("12.3"));
	station.setStationName("SNAME");
	station.getData().add(new RasaqmData(new Date(1000l), new BigDecimal("2.1")));
	dataset.addSeries(station);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	dataset.marshal(baos);

	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	RasaqmDataset dataset2 = RasaqmDataset.unmarshal(bais);
	assertEquals(dataset.getParameterId(), dataset2.getParameterId());
	assertEquals(dataset.getParameterName(), dataset2.getParameterName());
	RasaqmSeries station2 = dataset2.getSeries("SNAME");
	List<RasaqmData> data2 = station2.getData();
	RasaqmData data2Value = data2.get(0);
	assertEquals(new BigDecimal("2.1"), data2Value.getValue());
	assertEquals(new Date(1000l), data2Value.getDate());

    }

}
