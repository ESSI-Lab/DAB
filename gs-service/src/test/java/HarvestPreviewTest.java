import eu.essi_lab.accessor.eurobis.ld.*;
import eu.essi_lab.cdk.harvest.*;

/**
 * @author Fabrizio
 */
public class HarvestPreviewTest {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	HarvestPreviewTool.Preview preview = HarvestPreviewTool.get( //
		new EurOBISLdConnector(), //
		"https://marineinfo.org/id/collection/619.ttl");

	System.out.println(preview.getOriginalMetadata());
	System.out.println(preview.getHarmonizedMetadata());
	System.out.println(preview.getMappedResource().asString(true));
	System.out.println(preview.getIndexesReport());
    }
}
