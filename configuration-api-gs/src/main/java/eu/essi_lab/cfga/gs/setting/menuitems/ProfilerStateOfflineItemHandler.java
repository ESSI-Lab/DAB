/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.menuitems;

/**
 * @author Fabrizio
 */
public class ProfilerStateOfflineItemHandler extends ProfilerStateOnlineItemHandler {

    /**
     * 
     */
    public ProfilerStateOfflineItemHandler() {
    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public ProfilerStateOfflineItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    /**
     * @return
     */
    protected boolean online() {

	return false;
    }

    @Override
    public String getItemText() {

	return "Turn offline selected profilers";
    }

}
