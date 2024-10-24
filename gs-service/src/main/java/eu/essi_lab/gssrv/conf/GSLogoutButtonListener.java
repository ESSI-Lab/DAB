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
import com.vaadin.flow.component.button.Button;

import eu.essi_lab.cfga.gui.ConfigurationView;
import eu.essi_lab.cfga.gui.LogOutButtonListener;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class GSLogoutButtonListener extends LogOutButtonListener {

    /**
     * @param view
     */
    public GSLogoutButtonListener(ConfigurationView view) {

	super(view);
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	String requestURL = view.getRequestURL();

	String url = "../../../gs-service/auth/user/logout?url=" + requestURL;

	view.getUI().get().getPage().open(url, "_self");
	
	view.getUI().get().push();
    }
}
