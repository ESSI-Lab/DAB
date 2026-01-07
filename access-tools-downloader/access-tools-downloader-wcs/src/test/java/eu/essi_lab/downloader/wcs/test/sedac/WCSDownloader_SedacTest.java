package eu.essi_lab.downloader.wcs.test.sedac;

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

public abstract class WCSDownloader_SedacTest extends WCSDownloader_Test {

    public static final String SEDAC_WCS100_DOWNLOAD_URL = "http://sedac.ciesin.columbia.edu/geoserver/wcs?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=wildareas-v3%3Awildareas-v3-1993-human-footprint&CRS=EPSG%3A4326&BBOX=-179.999999909593,-55.79166793239201,179.99661110853617,83.66621130702998&WIDTH=363&HEIGHT=140&FORMAT=GeoTIFF";
    public static final String SEDAC_WCS111_DOWNLOAD_URL = "http://sedac.ciesin.columbia.edu/geoserver/wcs?SERVICE=WCS&VERSION=1.1.1&REQUEST=GetCoverage&identifier=wildareas-v3%3Awildareas-v3-1993-human-footprint&GridBaseCRS=urn:ogc:def:crs:EPSG::4326&GridType=urn:ogc:def:method:WCS:1.1:2dGridIn2dCrs&GridCS=urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS&BoundingBox=-55.30349681253593,-179.50413680901704,83.15825471803305,179.50074800796023,urn:ogc:def:crs:EPSG::4326&GridOffsets=-0.9961277088530143,0.0,0.0,0.9917262011518709&FORMAT=image%2Ftiff";

    @Override
    public Online getOnline(String onlineResourceId) {
	Online onLine = new Online();
	onLine.setLinkage("http://sedac.ciesin.columbia.edu/geoserver/wcs?");
	onLine.setName("wildareas-v3:wildareas-v3-1993-human-footprint");
	onLine.setIdentifier(onlineResourceId);
	onLine.setProtocol(getProtocol().getCommonURN());
	return onLine;
    }

    @Override
    protected void assertGoodDescriptor(DataDescriptor descriptor) throws Exception {
	Assert.assertEquals(DataType.GRID, descriptor.getDataType());
	Assert.assertEquals(CRS.EPSG_4326(), descriptor.getCRS());
	List<DataDimension> dimensions = descriptor.getSpatialDimensions();
	DataDimension dimension1 = dimensions.get(0);
	DataDimension dimension2 = dimensions.get(1);

	Double res1 = -0.009892734570434986;
	Double res2 = 0.009892734570434986;
	Double origin1 = 83.66126493974477;
	Double origin2 = -179.99505354230777;
	Long size1 = 14097l;
	Long size2 = 36390l;

	NetProtocol protocol = getProtocol();

	if (protocol.equals(NetProtocolWrapper.WCS_1_1_1)) {
	    // the size is missing from the coverage description, but it is inferred
	    // (as different) from the bounding box & resolution values
	    size1 = 14098l;
	    size2 = 36391l;
	}

	assertDimension(dimension1, origin1, res1, size1, "latitude");
	assertDimension(dimension2, origin2, res2, size2, "longitude");

    }

    @Override
    public boolean reduceDimensions() {
	return true;
    }

}
