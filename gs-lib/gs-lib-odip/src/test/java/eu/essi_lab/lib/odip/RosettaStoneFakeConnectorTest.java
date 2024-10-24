package eu.essi_lab.lib.odip;

import eu.essi_lab.lib.odip.rosetta.FakeRosettaStone;
import eu.essi_lab.lib.odip.rosetta.RosettaStone;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class RosettaStoneFakeConnectorTest extends RosettaStoneTest {

    @Override
    public RosettaStone getConnector() {
	GSLoggerFactory.getLogger(getClass()).info("Using fake connector");
	return new FakeRosettaStone();
    }

}
