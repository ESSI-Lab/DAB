package eu.essi_lab.test.authorization.authzforce.essi.rbac;

/**
 * @author Fabrizio
 */
public class HISCentral_PPS_Test extends CreatorPSS_Test {

    @Override
    protected String getRole() {

	return "his_central";
    }

    @Override
    protected String getDenyForRoleOne() {

	return "gwp";
    }

    @Override
    protected String getDenyForRoleTwo() {

	return "whos";
    }
}
