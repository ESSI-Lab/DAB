package eu.essi_lab.downloader.wcs.test.cropland;

import java.util.List;

import org.junit.Assert;

import eu.essi_lab.downloader.wcs.test.WCSDownloader_Test;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public abstract class WCSDownloader_CropLandTest extends WCSDownloader_Test {

    public static final String CROP_LAND_WCS_100_DOWNLOAD_URL = "http://gis.csiss.gmu.edu/cgi-bin/cdl_services?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=cdl_2009&CRS=epsg%3A102004&BBOX=-2356078.0,276918.0,2258154.0,3172566.0&WIDTH=823&HEIGHT=517&FORMAT=GTiff";
    public static final String CROP_LAND_WCS_111_DOWNLOAD_URL = "http://gis.csiss.gmu.edu/cgi-bin/cdl_services?SERVICE=WCS&VERSION=1.1.1&REQUEST=GetCoverage&identifier=cdl_2009&GridBaseCRS=urn:ogc:def:crs:EPSG::102004&GridType=urn:ogc:def:method:WCS:1.1:2dSimpleGrid&GridCS=urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS&BoundingBox=-2353246.6998784933,279690.4332688588,2255378.6998784933,3169737.5667311414,urn:ogc:def:crs:EPSG::102004&GridOffsets=5606.600243013366,-5600.866537717601&FORMAT=image%2Ftiff";

    @Override
    public Online getOnline(String onlineResourceId) {
	Online onLine = new Online();
	onLine.setLinkage("http://gis.csiss.gmu.edu/cgi-bin/cdl_services?");
	onLine.setName("cdl_2009");
	onLine.setIdentifier(onlineResourceId);
	onLine.setProtocol(getProtocol().getCommonURN());
	return onLine;
    }

    @Override
    protected void assertGoodDescriptor(DataDescriptor descriptor) throws Exception {
	Assert.assertEquals(DataType.GRID, descriptor.getDataType());
	Assert.assertEquals(CRS.fromIdentifier("EPSG:102004"), descriptor.getCRS());
	List<DataDimension> dimensions = descriptor.getSpatialDimensions();
	DataDimension dimension1 = dimensions.get(0);
	DataDimension dimension2 = dimensions.get(1);

	Double res1 = 56.;
	Double res2 = -56.;
	Double origin1 = -2356050.;
	Double origin2 = 3172594.;
	Long size1 = 82398l;
	Long size2 = 51709l;

	NetProtocol protocol = getProtocol();

	if (protocol.equals(NetProtocols.WCS_2_0_1)) {

	} else if (protocol.equals(NetProtocols.WCS_1_1_1)) {
	    // slightly different origin is provided in the WCS 1.1.1 description document
	    origin1 = -2356022.;
	    origin2 = 3172566.;
	}

	assertDimension(dimension1, origin1, res1, size1, null);
	assertDimension(dimension2, origin2, res2, size2, null);

    }

    @Override
    public boolean reduceDimensions() {
	return true;
    }
}
