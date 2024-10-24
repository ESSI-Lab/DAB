package eu.essi_lab.access.compliance.wrapper;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class DataDescriptorWrapperTest {

    @Test
    public void test() throws JAXBException, ParseException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("dataDescriptor.xml");
	DataDescriptor descriptor = DataDescriptorWrapper.wrap(stream);

	CRS crs = descriptor.getCRS();
	Assert.assertEquals(CRS.EPSG_4326(), crs);

	DataFormat dataFormat = descriptor.getDataFormat();
	Assert.assertEquals(DataFormat.NETCDF_3(), dataFormat);

	DataType dataType = descriptor.getDataType();
	Assert.assertEquals(DataType.TIME_SERIES, dataType);

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	ContinueDimension sizedDimension = temporalDimension.getContinueDimension();

	Datum datum = sizedDimension.getDatum();
	String name = sizedDimension.getName();
	Number lower = sizedDimension.getLower();
	Number upper = sizedDimension.getUpper();
	Number resolution = sizedDimension.getResolution();
	Long size = sizedDimension.getSize();
	DimensionType type = sizedDimension.getType();
	Unit uom = sizedDimension.getUom();

	Assert.assertEquals("time", name);
	Assert.assertEquals(Datum.UNIX_EPOCH_TIME(), datum);
	Assert.assertEquals(1318372200000l, lower);
	Assert.assertEquals(1318408200000l, upper);
	Assert.assertEquals(Unit.MILLI_SECOND, uom);
	Assert.assertEquals(1800000l, resolution);
	Assert.assertEquals((Long) 1000000l, size);
	Assert.assertEquals(DimensionType.TIME, type);
    }
}
