package eu.essi_lab.demo.accessor;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;

/**
 * @author Fabrizio
 */
public class DemoConnectorSetting extends HarvestedConnectorSetting {

    /**
     * 
     */
    public DemoConnectorSetting() {
    }

    @Override
    protected String initConnectorType() {

	return DemoConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Demo Connector options";
    }
}
