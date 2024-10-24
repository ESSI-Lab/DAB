package eu.essi_lab.lib.odip;

import eu.essi_lab.lib.odip.rosetta.RosettaStone;
import eu.essi_lab.lib.odip.rosetta.RosettaStoneConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class RosettaStoneConnectorExternalTestIT extends RosettaStoneTest {

    @Override
    public RosettaStone getConnector() {
	GSLoggerFactory.getLogger(getClass()).info("Using real connector");
	return new RosettaStoneConnector();
    }
 
}
