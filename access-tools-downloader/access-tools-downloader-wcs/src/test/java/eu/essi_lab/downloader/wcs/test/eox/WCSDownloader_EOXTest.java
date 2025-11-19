package eu.essi_lab.downloader.wcs.test.eox;

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

public abstract class WCSDownloader_EOXTest extends WCSDownloader_Test {

    public static final String EOX_WCS_100_DOWNLOAD_URL = "http://ows.eox.at/cite/mapserver?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=MER_FRS_1PNUPA20090701_124435_000005122080_00224_38354_6861_RGB&CRS=EPSG%3A4326&BBOX=-55.4120394710015,-36.09745549707904,-36.732795471001516,-3.586695897079038&WIDTH=669&HEIGHT=1164&FORMAT=GTiff";
    public static final String EOX_WCS_111_DOWNLOAD_URL = "http://ows.eox.at/cite/mapserver?SERVICE=WCS&VERSION=1.1.1&REQUEST=GetCoverage&identifier=MER_FRS_1PNUPA20090701_124435_000005122080_00224_38354_6861_RGB&GridBaseCRS=urn:ogc:def:crs:EPSG::4326&GridType=urn:ogc:def:method:WCS:1.1:2dSimpleGrid&GridCS=urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS&BoundingBox=-36.09745549707903,-55.3841183289985,-3.61462610292096,-36.73279547100151,urn:ogc:def:crs:EPSG::4326&GridOffsets=-0.0279302058419244,0.0279211420029895&FORMAT=image%2Ftiff";
    public static final String EOX_WCS_201_DOWNLOAD_URL = "http://ows.eox.at/cite/mapserver?SERVICE=WCS&VERSION=2.0.1&REQUEST=GetCoverage&coverageId=MER_FRS_1PNUPA20090701_124435_000005122080_00224_38354_6861_RGB&outputCrs=http://www.opengis.net/def/crs/EPSG/0/4326&subset=lat(-36.083251000000004,-3.600661)&subset=long(-55.398079,-36.746851)&SCALESIZE=lat(1164),long(669)&FORMAT=image%2Ftiff";

    @Override
    public Online getOnline(String onlineResourceId) {
	Online onLine = new Online();
	onLine.setLinkage("http://ows.eox.at/cite/mapserver?");
	onLine.setName("MER_FRS_1PNUPA20090701_124435_000005122080_00224_38354_6861_RGB");
	onLine.setProtocol(getProtocol().getCommonURN());
	onLine.setIdentifier(onlineResourceId);
	return onLine;
    }

    @Override
    protected void assertGoodDescriptor(DataDescriptor descriptor) throws Exception {
	Assert.assertEquals(DataType.GRID, descriptor.getDataType());
	Assert.assertEquals(CRS.EPSG_4326(), descriptor.getCRS());
	List<DataDimension> dimensions = descriptor.getSpatialDimensions();
	DataDimension dimension1 = dimensions.get(0);
	DataDimension dimension2 = dimensions.get(1);

	Double res1 = -0.0279302058419244;
	Double res2 = 0.0279211420029895;
	Double origin1 = -3.600661;
	Double origin2 = -55.3980789;
	Long size1 = 1164l;
	Long size2 = 669l;

	NetProtocol protocol = getProtocol();

	if (protocol.equals(NetProtocolWrapper.WCS_2_0_1.get())) {
	    // different values are found in the 2.0.1 description document!
	    res1 = -0.027930; // rounded
	    res2 = 0.027921; // rounded
	    origin1 = -3.600661;
	    origin2 = -55.398079;// rounded
	} else if (protocol.equals(NetProtocolWrapper.WCS_1_1_1.get())) {
	    // different grid origin found in the 1.1.1 description document!
	    origin1 = -3.61462610292096;
	    origin2 = -55.3841183289985;
	}

	assertDimension(dimension1, origin1, res1, size1, "latitude");
	assertDimension(dimension2, origin2, res2, size2, "longitude");

    }

}
