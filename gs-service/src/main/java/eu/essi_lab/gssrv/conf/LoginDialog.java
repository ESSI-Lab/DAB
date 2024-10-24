package eu.essi_lab.gssrv.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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

	setTitle("Login as administrator");

 	getCloseButton().getStyle().set("display", "none");

	VerticalLayout contentLayout = getContentLayout();
	contentLayout.getStyle().set("padding", "20px");

	Label label1 = ComponentFactory.createLabel("You must be logged in as administrator to view this page");
	Label label2 = ComponentFactory.createLabel("Please login with one of the following providers");

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

	Button googleButton = new Button("GOOGLE");

	googleButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {

	    @Override
	    public void onComponentEvent(ClickEvent<Button> event) {

		String url = "../../../gs-service/auth/user/login/google?url=" + requestURL;

		UI.getCurrent().getPage().open(url, "_self");
	    }
	});

	horizontalLayout.add(googleButton);

	Button facebook = new Button("FACEBOOK");
	facebook.getStyle().set("margin-left", "10px");

	facebook.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {

	    @Override
	    public void onComponentEvent(ClickEvent<Button> event) {

		String url = "../../../gs-service/auth/user/login/facebook?url=" + requestURL;

		UI.getCurrent().getPage().open(url, "_self");
	    }
	});

	horizontalLayout.add(facebook);

	Button twitter = new Button("TWITTER");
	twitter.getStyle().set("margin-left", "10px");

	twitter.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {

	    @Override
	    public void onComponentEvent(ClickEvent<Button> event) {

		String url = "../../../gs-service/auth/user/login/twitter?url=" + requestURL;

		UI.getCurrent().getPage().open(url, "_self");
	    }
	});

	horizontalLayout.add(twitter);

    }
}
