package eu.essi_lab.access.augmenter.test;

import eu.essi_lab.access.augmenter.AccessAugmenter;

/**
 * @author Fabrizio
 */
public class LoggedAccessAugmenterStarter {

    public static void main(String[] args) throws Exception {

	AccessAugmenterExternalTestIT accessAugmenterTestIT = new AccessAugmenterExternalTestIT() {

	    protected AccessAugmenter createAugmenter() {

		LoggedAccessAugmenter augmenter = new LoggedAccessAugmenter();
		augmenter.setDownloadFolder("C:\\Users\\Fabrizio\\Desktop\\CuashiHis-TEST-DATA");
		augmenter.setDownloadFolder("/home/boldrini/cuahsi-test");

		// augmenter.get
		return augmenter;
	    }
	};

	accessAugmenterTestIT.getResourceNamesToBeTested().add("parameter;LBR:USU39;platform;LBR:USU-LBR-Mendon");
	accessAugmenterTestIT.cuahsiTest();
    }

}
