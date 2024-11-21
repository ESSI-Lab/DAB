package eu.essi_lab.accessor.wof;

import org.junit.Before;

public class CUAHSIHISServerAccessor3ExternalTestIT extends CUAHSIHISServerAccessorExternalTestIT {

    @SuppressWarnings("rawtypes")
    @Before
    public void init() {
	this.endpoint = "https://hydroportal.cuahsi.org/mwdis/cuahsi_1_1.asmx?WSDL";
	this.connector = new CUAHSIHISServerConnector();
	connector.setSourceURL(endpoint);
	this.mapper = new WML_1_1Mapper();
    }

}
