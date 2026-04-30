package eu.essi_lab.accessor.hiscentral.umbria;

import java.util.List;

public class SensorConfig {

    List<String> resourceIds;
    List<HISCentralUmbriaConnector.UMBRIA_Variable> variables;
    boolean useStationId;

    public SensorConfig(List<String> resourceIds, List<HISCentralUmbriaConnector.UMBRIA_Variable> variables, boolean useStationId) {
	this.resourceIds = resourceIds;
	this.variables = variables;
	this.useStationId = useStationId;
    }
}
