package eu.essi_lab.cfga.gs.setting.oauth;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class OAuthSetting extends Setting implements EditableSetting {

    private static final String CLIENT_ID_OPTION_KEY = "clientId";
    private static final String CLIENT_SECRET_OPTION_KEY = "clientSecret";
    private static final String ADMIN_USER_ID_OPTION_KEY = "adminUserId";

    /**
     * @author Fabrizio
     */
    public enum OAuthProvider implements LabeledEnum {

	GOOGLE("Google", "google"), //
	KEYCLOAK("Keycloak", "keycloak");

	private final String label;
	private final String providerName;

	/**
	 * @param label
	 * @param providerName
	 */
	OAuthProvider(String label, String providerName) {
	    this.label = label;
	    this.providerName = providerName;
	}

	/**
	 * @return the value
	 */
	public String getProviderName() {

	    return providerName;
	}

	@Override
	public String getLabel() {

	    return label;
	}
    }

    /**
     *
     */
    public OAuthSetting() {

	setName("OAuth settings");
	enableCompactMode(false);
	setCanBeDisabled(false);
	setShowHeader(false);
	setCanBeCleaned(false);

	//
	//
	//

	setSelectionMode(SelectionMode.SINGLE);

	GoogleProviderSetting googleProviderSetting = new GoogleProviderSetting();
	googleProviderSetting.setSelected(true);
	addSetting(googleProviderSetting);

	KeycloakProviderSetting keycloakProviderSetting = new KeycloakProviderSetting();
	addSetting(keycloakProviderSetting);

	//
	//
	//

	Option<String> adminUserOption = StringOptionBuilder.get().//
		withKey(ADMIN_USER_ID_OPTION_KEY).//
		withLabel("Admin user identifier").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(adminUserOption);

	Option<String> clientIdOption = StringOptionBuilder.get().//
		withKey(CLIENT_ID_OPTION_KEY).//
		withLabel("Client id").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(clientIdOption);

	Option<String> clientSecretOption = StringOptionBuilder.get().//
		withKey(CLIENT_SECRET_OPTION_KEY).//
		withLabel("Client secret").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(clientSecretOption);

	//
	// set the validator
	//
	setValidator(new OAuthSettingValidator());
    }

    /**
     * @param object
     */
    public OAuthSetting(String object) {

	super(object);
    }

    /**
     * @param object
     */
    public OAuthSetting(JSONObject object) {

	super(object);
    }

    /**
     * @author Fabrizio
     */
    public static class OAuthSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    OAuthSetting thisSetting = (OAuthSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    Optional<String> adminId = thisSetting.getAdminId();
	    if (adminId.isEmpty()) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Admin identifier missing");
	    }

	    Optional<String> clientId = thisSetting.getClientId();
	    if (clientId.isEmpty()) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Client identifier identifier missing");
	    }

	    Optional<String> clientSecret = thisSetting.getClientSecret();
	    if (clientSecret.isEmpty()) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Client secret missing");
	    }

	    OAuthProviderSetting providerSetting = thisSetting.getSelectedProviderSetting();

	    Optional<String> loginURL = providerSetting.getLoginURL();
	    if (loginURL.isEmpty()) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Login URL missing");
	    }

	    Optional<String> tokenURL = providerSetting.getTokenURL();
	    if (tokenURL.isEmpty()) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("Token URL missing");
	    }

	    Optional<String> userInfoURL = providerSetting.getUserInfoURL();
	    if (userInfoURL.isEmpty()) {

		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		validationResponse.getErrors().add("User info URL missing");
	    }

	    return validationResponse;
	}
    }

    /**
     * @author Fabrizio
     */
    public static class TabDescriptorProvider extends TabDescriptor {

	/**
	 *
	 */
	public TabDescriptorProvider() {

	    setLabel("Authorization");

	    TabContentDescriptor descriptor = TabContentDescriptorBuilder.get(OAuthSetting.class).//

		    build();

	    setIndex(GSTabIndex.AUTHORIZATION.getIndex());
	    addContentDescriptor(descriptor);

	}
    }

    /**
     * @param provider
     */
    @SuppressWarnings("incomplete-switch")
    public void selectProvider(OAuthProvider provider) {

	switch (provider) {
	case GOOGLE -> {
	    getSetting(GoogleProviderSetting.IDENTIFIER, GoogleProviderSetting.class).get().setSelected(true);
	    getSetting(KeycloakProviderSetting.IDENTIFIER, KeycloakProviderSetting.class).get().setSelected(false);
	}

	case KEYCLOAK -> {
	    getSetting(GoogleProviderSetting.IDENTIFIER, GoogleProviderSetting.class).get().setSelected(false);
	    getSetting(KeycloakProviderSetting.IDENTIFIER, KeycloakProviderSetting.class).get().setSelected(true);
	}
	}
    }

    /**
     * @return
     */
    public OAuthProvider getSelectedProvider() {

	return getSelectedProviderSetting().getIdentifier().equals(KeycloakProviderSetting.IDENTIFIER) ? //
		OAuthProvider.KEYCLOAK : OAuthProvider.GOOGLE;
    }

    /**
     * @return
     */
    public OAuthProviderSetting getSelectedProviderSetting() {

	Optional<GoogleProviderSetting> google = getSetting(GoogleProviderSetting.IDENTIFIER, GoogleProviderSetting.class);
	Optional<KeycloakProviderSetting> keycloak = getSetting(KeycloakProviderSetting.IDENTIFIER, KeycloakProviderSetting.class);

	return google.isPresent() && google.get().isSelected() ? google.get() : keycloak.get();
    }

    /**
     *
     */
    public void setClientId(String clientId) {

	getOption(CLIENT_ID_OPTION_KEY, String.class).get().setValue(clientId);
    }

    /**
     * @return
     */
    public Optional<String> getAdminId() {

	return getOption(ADMIN_USER_ID_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     *
     */
    public void setAdminId(String adminId) {

	getOption(ADMIN_USER_ID_OPTION_KEY, String.class).get().setValue(adminId);
    }

    /**
     * @return
     */
    public Optional<String> getClientId() {

	return getOption(CLIENT_ID_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     *
     */
    public void setClientSecret(String clientSecret) {

	getOption(CLIENT_SECRET_OPTION_KEY, String.class).get().setValue(clientSecret);
    }

    /**
     * @return
     */
    public Optional<String> getClientSecret() {

	return getOption(CLIENT_SECRET_OPTION_KEY, String.class).get().getOptionalValue();
    }
}
