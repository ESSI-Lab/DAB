package eu.essi_lab.demo.accessor;

import eu.essi_lab.adk.harvest.HarvestedAccessor;

/**
 * @author Fabrizio
 */
public class DemoAccessor extends HarvestedAccessor<DemoConnector> {

    /**
     * 
     */
    public static String TYPE = "Demo";

    @Override
    protected String initSettingName() {

	return "Demo";
    }

    @Override
    protected String initAccessorType() {

	return TYPE;
    }

    @Override
    protected DemoConnectorSetting initHarvestedConnectorSetting() {

	return new DemoConnectorSetting();
    }
}
