package eu.essi_lab.iso.datamodel.test;

import java.util.List;

import org.junit.Assert;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import net.opengis.iso19139.gmd.v_20060504.MDGridSpatialRepresentationType;

public class GridSpatialRepresentationTest extends MetadataTest<GridSpatialRepresentation, MDGridSpatialRepresentationType> {

    private Dimension dimension = new Dimension();

    public GridSpatialRepresentationTest() {
	super(GridSpatialRepresentation.class, MDGridSpatialRepresentationType.class);
    }

    @Override
    public void setProperties(GridSpatialRepresentation grid) {
	grid.setCellGeometryCode("point");
	grid.setNumberOfDimensions(2);
	Dimension dim = this.dimension;
	grid.addAxisDimension(dim);
    }

    @Override
    public void checkProperties(GridSpatialRepresentation grid) {
	Assert.assertEquals("point", grid.getCellGeometryCode());
	Assert.assertEquals((long) 2, (long) grid.getNumberOfDimensions());
	Assert.assertEquals(dimension, grid.getAxisDimension());
	List<Dimension> dimensions = Lists.newArrayList(grid.getAxisDimensions());
	Assert.assertEquals(1, dimensions.size());
    }

    @Override
    public void clearProperties(GridSpatialRepresentation grid) {
	grid.setCellGeometryCode(null);
	grid.setNumberOfDimensions(null);
	grid.clearAxisDimensions();
    }

    @Override
    public void checkNullProperties(GridSpatialRepresentation grid) {
	Assert.assertNull(grid.getCellGeometryCode());
	Assert.assertNull(grid.getNumberOfDimensions());
	Assert.assertNull(grid.getAxisDimension());
    }
}
