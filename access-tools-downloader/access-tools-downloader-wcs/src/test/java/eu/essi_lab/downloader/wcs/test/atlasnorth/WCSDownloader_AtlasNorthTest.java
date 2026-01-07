package eu.essi_lab.downloader.wcs.test.atlasnorth;

import java.util.List;

import org.junit.Assert;

import eu.essi_lab.downloader.wcs.test.WCSDownloader_Test;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public abstract class WCSDownloader_AtlasNorthTest extends WCSDownloader_Test {

    public static final String ATLAS_NORTH_WCS_100_DOWNLOAD_URL = "http://nsidc.org/cgi-bin/atlas_north?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=greenland_bedrock_elevation&CRS=EPSG%3A32661&BBOX=-827767.555,-1150927.137,2172232.445,2044072.863&WIDTH=600&HEIGHT=639&FORMAT=GeoTIFFFloat32";
    public static final String ATLAS_NORTH_WCS_111_DOWNLOAD_URL = "http://nsidc.org/cgi-bin/atlas_north?SERVICE=WCS&VERSION=1.1.1&REQUEST=GetCoverage&identifier=greenland_bedrock_elevation&GridBaseCRS=urn:ogc:def:crs:EPSG::32661&GridType=urn:ogc:def:method:WCS:1.1:2dSimpleGrid&GridCS=urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS&BoundingBox=-822767.555,-1150927.137,2172232.445,2039072.863,urn:ogc:def:crs:EPSG::32661&GridOffsets=5000.0,-5000.0&FORMAT=image%2Ftiff";

    @Override
    public Online getOnline(String onlineResourceId) {
	Online onLine = new Online();
	onLine.setLinkage("http://nsidc.org/cgi-bin/atlas_north?");
	onLine.setProtocol(getProtocol().getCommonURN());
	onLine.setName("greenland_bedrock_elevation");
	onLine.setIdentifier(onlineResourceId);
	return onLine;
    }

    @Override
    protected void assertGoodDescriptor(DataDescriptor descriptor) throws Exception {
	Assert.assertEquals(DataType.GRID, descriptor.getDataType());
	Assert.assertEquals(CRS.fromIdentifier("EPSG:32661"), descriptor.getCRS());
	List<DataDimension> dimensions = descriptor.getSpatialDimensions();
	DataDimension dimension1 = dimensions.get(0);
	DataDimension dimension2 = dimensions.get(1);

	Double res1 = 5000.;
	Double res2 = -5000.;
	Double origin1 = -825267.555;
	Double origin2 = 2041572.863;
	Long size1 = 600l;
	Long size2 = 639l;

	NetProtocol protocol = getProtocol();

	if (protocol.equals(NetProtocolWrapper.WCS_2_0_1)) {

	} else if (protocol.equals(NetProtocolWrapper.WCS_1_1_1)) {
	    // slightly different origin is provided in the WCS 1.1.1 description document
	    origin1 = -822767.555;
	    origin2 = 2039072.863;
	}

	assertDimension(dimension1, origin1, res1, size1, "northing");
	assertDimension(dimension2, origin2, res2, size2, "easting");

    }

}
