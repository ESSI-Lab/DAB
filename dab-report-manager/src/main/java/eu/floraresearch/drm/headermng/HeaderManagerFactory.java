package eu.floraresearch.drm.headermng;

import eu.floraresearch.drm.ConfigReader;

public class HeaderManagerFactory {

    public static HeaderManager createHeaderManager() {

	String hm = ConfigReader.getInstance().readHeaderManager();

	try {
	    Class<?> clazz = Class.forName(hm);
	    Object tempObject = clazz.newInstance();

	    HeaderManager r = (HeaderManager) tempObject;

	    return r;

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
}
