package eu.essi_lab.gssrv.conf;

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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.dialog.EnhancedDialog;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
class LoginDialog extends EnhancedDialog {

    /**
     * @param requestURL
     */
    public LoginDialog(String requestURL) {

	setTitle("Administrator login");

	getCloseButton().getStyle().set("display", "none");

	getMainLayout().getStyle().set("padding-bottom", "0px");

	VerticalLayout contentLayout = getContentLayout();
	contentLayout.getStyle().set("padding", "20px");
	contentLayout.getStyle().set("padding-bottom", "0px");

	Label label1 = ComponentFactory.createLabel("You must be logged in as administrator to view this page.");
	Label label2 = ComponentFactory.createLabel("Please login with one of the following providers:");

	contentLayout.add(label1);
	contentLayout.add(label2);

	//
	//
	//

	HorizontalLayout horizontalLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	horizontalLayout.getStyle().set("margin-left", "auto");
	horizontalLayout.getStyle().set("margin-right", "auto");
	horizontalLayout.getStyle().set("margin-top", "20px");
	horizontalLayout.getStyle().set("margin-bottom", "10px");

	contentLayout.add(horizontalLayout);

	Button googleButton = create("Google", "logo/Google_Logo.png", requestURL);

	horizontalLayout.add(googleButton);

	Button keycloakButton = create("Keycloak", "logo/Keycloak_Logo.png", requestURL);

	horizontalLayout.add(keycloakButton);
    }

    /**
     * @param provider
     * @param logoUrl
     * @return
     */
    private Button create(String provider, String logoUrl, String requestURL) {

	Image image = new Image(new StreamResource(provider, () -> getClass().getClassLoader().getResourceAsStream(logoUrl)), provider);
	image.setWidth("54px");
	image.setHeight("54px");

	Button button = new Button(image);
	button.getStyle().set("margin-left", "10px");
	button.getStyle().set("border", "1px solid lightgray");
	button.getStyle().set("cursor", "pointer");
	button.setTooltipText(provider);
	button.setWidth("60px");
	button.setHeight("60px");
	button.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {

	    @Override
	    public void onComponentEvent(ClickEvent<Button> event) {

		String url = "../../../gs-service/auth/user/login/" + provider.toLowerCase() + "?url=" + requestURL;

		UI.getCurrent().getPage().open(url, "_self");
	    }
	});

	return button;
    }
}
