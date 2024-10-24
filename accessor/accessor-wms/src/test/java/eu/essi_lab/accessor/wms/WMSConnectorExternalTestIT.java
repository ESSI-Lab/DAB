package eu.essi_lab.accessor.wms;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Connector;

public class WMSConnectorExternalTestIT {

    private WMS_1_3_0Connector connector;

    @Before
    public void init() {
	this.connector = new WMS_1_3_0Connector();

    }


    @Test
    public void visitTest() throws IOException {
	String url;
	url = "http://afromaison.grid.unep.ch:8080/geoserver/ows?";
	url = "http://wms.agg.nrcan.gc.ca/wms2/wms2.aspx?";
	url = "http://webmap.ornl.gov/ogcbroker/wms?";
	url = "http://sdi.iia.cnr.it/gmosgeoserver/ows?";
	url = "https://wms.geo.admin.ch/?lang=en&";
	url = "http://wservice-sit.mop.gov.cl/arcgis/services/DV/red_caminera_de_chile/MapServer/WMSServer?";
	url = "http://sustainable-caucasus.grid.unep.ch/geoserver/ows?";
	
	
	
	connector.setSourceURL(url);
	
	ConnectorVisitor visitor = new ConnectorVisitor(connector);
	visitor.visit();
	System.out.println(visitor.getResourcesNumber());
    }
    
}
