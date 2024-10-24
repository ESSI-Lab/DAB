package eu.essi_lab.accessor.wof;

import org.junit.Before;

public class CUAHSIHISServerAccessor1ExternalTestIT extends CUAHSIHISServerAccessorExternalTestIT {

    @SuppressWarnings("rawtypes")
    @Before
    public void init() {
	this.endpoint = "http://hydrolite.ddns.net/italia/hsl-tos/index.php/default/services/cuahsi_1_1.asmx?WSDL";
	this.connector = new CUAHSIHISServerConnector();
	connector.setSourceURL(endpoint);
	this.mapper = new WML_1_1Mapper();
    }

}
