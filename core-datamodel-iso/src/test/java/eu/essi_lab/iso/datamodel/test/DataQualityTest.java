package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityType;

public class DataQualityTest extends MetadataTest<DataQuality, DQDataQualityType> {

    public DataQualityTest() {
	super(DataQuality.class, DQDataQualityType.class);
    }

    @Override
    public void setProperties(DataQuality quality) {
	quality.setLineageStatement("lineage");

    }

    @Override
    public void checkProperties(DataQuality quality) {
	Assert.assertEquals("lineage", quality.getLineageStatement());

    }

    @Override
    public void clearProperties(DataQuality quality) {
	quality.setLineageStatement(null);

    }

    @Override
    public void checkNullProperties(DataQuality quality) {
	Assert.assertNull(quality.getLineageStatement());

    }
}
