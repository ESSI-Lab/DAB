package eu.floraresearch.drm.headermng;

public class EmptyHeaderManager implements HeaderManager {

    @Override
    // @formatter:off
    public String createHeader() {
	return "";
    }
}
