package eu.essi_lab.downloader.wcs.test.afromaison;

import java.util.List;

import org.junit.Assert;

import eu.essi_lab.downloader.wcs.test.WCSDownloader_Test;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public abstract class WCSDownloader_AfromaisonTest extends WCSDownloader_Test {

    public static final String AFROMAISON_WCS_100_DOWNLOAD_URL = "http://afromaison.grid.unep.ch:8080/geoserver/ows?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=ethiopia%3AETH_Aster&CRS=EPSG%3A4326&BBOX=37.44736111111123,11.599583333333463,38.0490277777779,12.126527777777909&WIDTH=2166&HEIGHT=1897&FORMAT=GeoTIFF";

    @Override
    public Online getOnline(String onlineResourceId) {
	Online onLine = new Online();
	onLine.setLinkage("http://afromaison.grid.unep.ch:8080/geoserver/ows?");
	onLine.setName("ethiopia:ETH_Aster");
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

	Double res1 = -2.7777777777777767E-4;
	Double res2 = 2.777777777777777E-4;
	Double origin1 = 12.12638888888902;
	Double origin2 = 37.44750000000012;
	Long size1 = 1897l;
	Long size2 = 2166l;

	assertDimension(dimension1, origin1, res1, size1, "latitude");
	assertDimension(dimension2, origin2, res2, size2, "longitude");

    }

}
