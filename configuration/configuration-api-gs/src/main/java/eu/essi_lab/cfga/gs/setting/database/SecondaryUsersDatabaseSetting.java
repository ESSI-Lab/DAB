/**
 *
 */
package eu.essi_lab.cfga.gs.setting.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Users managed by third parties in a Keycloak realm.<br>
 * <br>
 * DAB holds no credentials for this realm and never reads its users: users authenticate to DAB by sending a Keycloak access token in the
 * <code>Authorization: Bearer</code> header. The token is verified with the realm public keys, and the user is built from its claims:
 * <ul>
 * <li>the role is always the configured one ({@link #getUsersRole()}), whatever the realm says</li>
 * <li>the permissions are the roles of the configured client ({@link #getClientId()}) which are also listed in
 * {@link #getAllowedPermissions()}</li>
 * <li>the allowed views are the client roles in the form <code>view:&lt;view id&gt;</code></li>
 * <li>the download limits are those of the largest configured tier ({@link #getDownloadTiers()}) among the client roles in the form
 * <code>download-tier:&lt;tier name&gt;</code></li>
 * </ul>
 *
 * @author Fabrizio
 */
public class SecondaryUsersDatabaseSetting extends UsersDatabaseSetting {

    public static final String USERS_ROLE_OPTION_KEY = "secUsersRole";
    public static final String CLIENT_ID_OPTION_KEY = "secUsersClientId";
    public static final String ALLOWED_PERMISSIONS_OPTION_KEY = "secUsersAllowedPermissions";
    public static final String DOWNLOAD_TIERS_OPTION_KEY = "secUsersDownloadTiers";
    public static final String LOGIN_CLIENT_ID_OPTION_KEY = "secUsersLoginClientId";
    public static final String SCRIPTS_CLIENT_ID_OPTION_KEY = "secUsersScriptsClientId";

    public static final String DEFAULT_CLIENT_ID = "dab";
    public static final String DEFAULT_ALLOWED_PERMISSIONS = "api,downloads";

    /**
     * Download limits of a tier
     *
     * @param maxDownloadSizeMB
     * @param maxDownloadPartSizeMB optional
     */
    public record DownloadTier(int maxDownloadSizeMB, Integer maxDownloadPartSizeMB) {
    }

    /**
     *
     */
    public SecondaryUsersDatabaseSetting() {

	setName("Secondary user database setting (Keycloak)");

	setDescription("If enabled and configured, users of the given Keycloak realm can access DAB by sending a Keycloak access token "
		+ "in the 'Authorization: Bearer' header. DAB holds no credentials for the realm. All these users are given the configured "
		+ "role; their permissions, views and download limits are read from the roles of the configured client, "
		+ "within the limits set here");

	Setting dbSetting = getDbSetting();

	//
	// no credentials and no database type: DAB never reads the users of this realm
	//
	dbSetting.removeOption(DATABASE_USER_OPTION_KEY);
	dbSetting.removeOption(DATABASE_PWD_OPTION_KEY);
	dbSetting.removeOption(DATABASE_TYPE_OPTION_KEY);

	dbSetting.getOption(DATABASE_URI_OPTION_KEY, String.class).get().setLabel("Keycloak URL (e.g. https://keycloak.example.org)");
	dbSetting.getOption(DATABASE_NAME_OPTION_KEY, String.class).get().setLabel("Realm");

	Option<String> clientIdOption = new Option<>(String.class);
	clientIdOption.setLabel("Client ID (expected token audience, and client whose roles are read)");
	clientIdOption.setKey(CLIENT_ID_OPTION_KEY);
	clientIdOption.setValue(DEFAULT_CLIENT_ID);
	clientIdOption.setRequired(true);
	clientIdOption.setCanBeDisabled(false);
	clientIdOption.setMultiValue(false);

	dbSetting.addOption(clientIdOption);

	Option<String> roleOption = new Option<>(String.class);
	roleOption.setLabel("Role assigned to all the users of this realm");
	roleOption.setKey(USERS_ROLE_OPTION_KEY);
	roleOption.setRequired(true);
	roleOption.setCanBeDisabled(false);
	roleOption.setMultiValue(false);

	dbSetting.addOption(roleOption);

	Option<String> permissionsOption = new Option<>(String.class);
	permissionsOption.setLabel("Permissions that can be granted by the realm (comma separated client roles)");
	permissionsOption.setKey(ALLOWED_PERMISSIONS_OPTION_KEY);
	permissionsOption.setValue(DEFAULT_ALLOWED_PERMISSIONS);
	permissionsOption.setRequired(false);
	permissionsOption.setCanBeDisabled(false);
	permissionsOption.setMultiValue(false);

	dbSetting.addOption(permissionsOption);

	Option<String> tiersOption = new Option<>(String.class);
	tiersOption.setLabel("Download tiers: <tier name>=<max download size MB>,<max download part size MB>, separated by new lines, ';' or ',' (e.g. standard=100,20, large=2000,200)");
	tiersOption.setKey(DOWNLOAD_TIERS_OPTION_KEY);
	tiersOption.setRequired(false);
	tiersOption.setCanBeDisabled(false);
	tiersOption.setMultiValue(false);
	tiersOption.enableTextArea(true);

	dbSetting.addOption(tiersOption);

	Option<String> loginClientOption = new Option<>(String.class);
	loginClientOption.setLabel("Login client ID: public client used by web pages to log in (e.g. the O&M API 'Authorize' button)");
	loginClientOption.setKey(LOGIN_CLIENT_ID_OPTION_KEY);
	loginClientOption.setRequired(false);
	loginClientOption.setCanBeDisabled(false);
	loginClientOption.setMultiValue(false);

	dbSetting.addOption(loginClientOption);

	Option<String> scriptsClientOption = new Option<>(String.class);
	scriptsClientOption.setLabel("Scripts client ID: public client with the device login, used by scripts to get an access token");
	scriptsClientOption.setKey(SCRIPTS_CLIENT_ID_OPTION_KEY);
	scriptsClientOption.setRequired(false);
	scriptsClientOption.setCanBeDisabled(false);
	scriptsClientOption.setMultiValue(false);

	dbSetting.addOption(scriptsClientOption);
    }

    //
    // Keycloak realm
    //

    /**
     * @return the Keycloak base URL, without trailing slash
     */
    public String getKeycloakUrl() {

	String url = Optional.ofNullable(getDatabaseUri()).orElse("").trim();

	while (url.endsWith("/")) {
	    url = url.substring(0, url.length() - 1);
	}

	return url;
    }

    /**
     * @return
     */
    public String getRealm() {

	return Optional.ofNullable(getDatabaseName()).orElse("").trim();
    }

    /**
     * @return the expected issuer of the tokens, e.g. https://keycloak.example.org/realms/users
     */
    public String getIssuer() {

	return getKeycloakUrl() + "/realms/" + getRealm();
    }

    /**
     * @return the URL of the realm public keys
     */
    public String getJwksUrl() {

	return getIssuer() + "/protocol/openid-connect/certs";
    }

    //
    // client
    //

    /**
     * @param clientId
     */
    public void setClientId(String clientId) {

	getDbSetting().getOption(CLIENT_ID_OPTION_KEY, String.class).get().setValue(clientId);
    }

    /**
     * @return the expected token audience, which is also the client whose roles are read
     */
    public Optional<String> getClientId() {

	return readTrimmed(CLIENT_ID_OPTION_KEY);
    }

    /**
     * @param clientId
     */
    public void setLoginClientId(String clientId) {

	getDbSetting().getOption(LOGIN_CLIENT_ID_OPTION_KEY, String.class).get().setValue(clientId);
    }

    /**
     * @return the public client used by web pages to log in, if configured
     */
    public Optional<String> getLoginClientId() {

	return readTrimmed(LOGIN_CLIENT_ID_OPTION_KEY);
    }

    /**
     * @param clientId
     */
    public void setScriptsClientId(String clientId) {

	getDbSetting().getOption(SCRIPTS_CLIENT_ID_OPTION_KEY, String.class).get().setValue(clientId);
    }

    /**
     * @return the public client with the device login, used by scripts, if configured
     */
    public Optional<String> getScriptsClientId() {

	return readTrimmed(SCRIPTS_CLIENT_ID_OPTION_KEY);
    }

    //
    // role
    //

    /**
     * @param role
     */
    public void setUsersRole(String role) {

	getDbSetting().getOption(USERS_ROLE_OPTION_KEY, String.class).get().setValue(role);
    }

    /**
     * @return the role to assign to all the users of this realm, if configured
     */
    public Optional<String> getUsersRole() {

	return readTrimmed(USERS_ROLE_OPTION_KEY);
    }

    //
    // permissions
    //

    /**
     * @param permissions comma separated
     */
    public void setAllowedPermissions(String permissions) {

	getDbSetting().getOption(ALLOWED_PERMISSIONS_OPTION_KEY, String.class).get().setValue(permissions);
    }

    /**
     * @return the permissions the realm can grant; client roles not in this list are ignored
     */
    public List<String> getAllowedPermissions() {

	return readTrimmed(ALLOWED_PERMISSIONS_OPTION_KEY).//
		map(value -> Arrays.stream(value.split(",")).//
			map(String::trim).//
			filter(p -> !p.isEmpty()).//
			toList())
		.orElse(Collections.emptyList());
    }

    //
    // download tiers
    //

    /**
     * @param tiers one per line, in the form <code>name=maxDownloadSizeMB,maxDownloadPartSizeMB</code>
     */
    public void setDownloadTiers(String tiers) {

	getDbSetting().getOption(DOWNLOAD_TIERS_OPTION_KEY, String.class).get().setValue(tiers);
    }

    /**
     * A tier: <code>name=maxDownloadSizeMB[,maxDownloadPartSizeMB]</code>
     */
    private static final Pattern TIER_PATTERN = Pattern.compile("([A-Za-z0-9_.:-]+)\\s*=\\s*(-?\\d+)\\s*(?:,\\s*(-?\\d+))?");

    /**
     * Separators allowed between tiers: new lines, ';' or ','
     */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s,;]*");

    /**
     * Tiers are in the form <code>name=maxDownloadSizeMB[,maxDownloadPartSizeMB]</code>, separated by new lines, ';' or ',', e.g.
     * <code>standard=100,20, large=2000,200</code>. Text in the wrong format and tiers with negative sizes are ignored (and logged)
     *
     * @return the configured tiers by name
     */
    public Map<String, DownloadTier> getDownloadTiers() {

	Map<String, DownloadTier> tiers = new LinkedHashMap<>();

	Optional<String> value = readTrimmed(DOWNLOAD_TIERS_OPTION_KEY);

	if (value.isEmpty()) {

	    return tiers;
	}

	String text = value.get();

	Matcher matcher = TIER_PATTERN.matcher(text);

	int last = 0;

	while (matcher.find()) {

	    warnIfNotSeparator(text.substring(last, matcher.start()));
	    last = matcher.end();

	    String name = matcher.group(1);

	    try {

		int size = Integer.parseInt(matcher.group(2));
		Integer partSize = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : null;

		if (size < 0 || (partSize != null && partSize < 0)) {

		    GSLoggerFactory.getLogger(getClass()).warn("Ignoring download tier with negative size: '{}'", matcher.group());

		} else {

		    tiers.put(name, new DownloadTier(size, partSize));
		}

	    } catch (NumberFormatException ex) {

		GSLoggerFactory.getLogger(getClass()).warn("Ignoring download tier with invalid size: '{}'", matcher.group());
	    }
	}

	warnIfNotSeparator(text.substring(last));

	return tiers;
    }

    /**
     * @param text
     */
    private void warnIfNotSeparator(String text) {

	if (!SEPARATORS.matcher(text).matches()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Ignoring download tiers text in wrong format: '{}'", text.trim());
	}
    }

    //
    // no credentials and no database type for this setting
    //

    @Override
    public String getDatabaseUser() {

	return null;
    }

    @Override
    public String getDatabasePassword() {

	return null;
    }

    @Override
    public Optional<String> getDatabaseType() {

	return Optional.empty();
    }

    /**
     * @param key
     * @return
     */
    private Optional<String> readTrimmed(String key) {

	return getDbSetting().//
		getOption(key, String.class).//
		flatMap(Option::getOptionalValue).//
		map(String::trim).//
		filter(v -> !v.isEmpty());
    }

    protected String getDbSettingId() {

	return "secUsersDbSetting";
    }

    protected String getVolatileDbSettingId() {

	return "secUsersVolFDbSetting";
    }
}
