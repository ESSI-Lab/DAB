package eu.essi_lab.gssrv.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.data.renderer.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.authorization.userfinder.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.auth.*;
import eu.essi_lab.model.exceptions.*;

import java.util.*;

/**
 * @author Fabrizio
 */
class UsersTabDescriptor extends AbstractGridDescriptor<UsersTabDescriptor.GridData> {

    private static final String ID_COLUMN = "Id";
    private static final String ROLE_COLUMN = "Role";
    private static final String EMAIL_COLUMN = "E-mail";
    private static final String FIRST_NAME_COLUMN = "First Name";
    private static final String SECOND_NAME_COLUMN = "Last Name";
    private static final String REG_DATE_COLUMN = "Registration Date";
    private static final String INSTITUTION_COLUMN = "Institution";
    private static final String ID_TYPE_COLUMN = "Id.Type";
    private static final String ENABLED_COLUMN = "Enabled";

    /**
     *
     */
    public UsersTabDescriptor() {

	setLabel("Users");

	getGrid().addColumn(UsersTabDescriptor.GridData::getPosition).//
		setHeader("").//
		setKey("#").//
		setWidth("50px");

	Grid.Column<UsersTabDescriptor.GridData> idCol = addSortableResizableColumn(ID_COLUMN, UsersTabDescriptor.GridData::getIdentifier,
		350);

	Grid.Column<UsersTabDescriptor.GridData> mailCol = addSortableResizableColumn(EMAIL_COLUMN, UsersTabDescriptor.GridData::getEmail,
		300);

	Grid.Column<UsersTabDescriptor.GridData> depCol = addSortableResizableColumn(ROLE_COLUMN, UsersTabDescriptor.GridData::getRole,
		300);

	Grid.Column<UsersTabDescriptor.GridData> fNameCol = addSortableResizableColumn(FIRST_NAME_COLUMN,
		UsersTabDescriptor.GridData::getFirstName, 300);

	Grid.Column<UsersTabDescriptor.GridData> sNameCol = addSortableResizableColumn(SECOND_NAME_COLUMN,
		UsersTabDescriptor.GridData::getLastName, 300);

	Grid.Column<UsersTabDescriptor.GridData> regDateCol = addSortableResizableColumn(REG_DATE_COLUMN,
		UsersTabDescriptor.GridData::getRegistrationDate, 300);

	Grid.Column<UsersTabDescriptor.GridData> instCol = addSortableResizableColumn(INSTITUTION_COLUMN,
		UsersTabDescriptor.GridData::getInstitution, 300);


	Grid.Column<UsersTabDescriptor.GridData> userIdCol = addSortableResizableColumn(ID_TYPE_COLUMN,
		UsersTabDescriptor.GridData::getUserIdentifierType, 200);

	Grid.Column<UsersTabDescriptor.GridData> enabledCol = addSortableColumn(ENABLED_COLUMN, UsersTabDescriptor.GridData::isEnabled,
		150);

	//
	//
	//

	HeaderRow filterRow = getGrid().appendHeaderRow();

	addFilterField(filterRow, idCol);
	addFilterField(filterRow, enabledCol);
	addFilterField(filterRow, depCol);
	addFilterField(filterRow, userIdCol);
	addFilterField(filterRow, mailCol);
	addFilterField(filterRow, fNameCol);
	addFilterField(filterRow, sNameCol);
	addFilterField(filterRow, regDateCol);
	addFilterField(filterRow, instCol);

	//
	//

	String desc = "Click 'Reload' to update the list of DAB registered users";

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withShowDirective(desc, false).//
		withComponent(getVerticalLayout()).//
		reloadable(() -> update(getVerticalLayout())).//
		build();

	setIndex(GSTabIndex.USERS.getIndex());
	addContentDescriptor(descriptor);

	//
	//
	//

	update(getVerticalLayout());
    }



    /**
     * @return
     */
    private List<GSUser> getUsers() {

	try {


	    return UserFinder.newInstance().getUsers();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(HarvestingSetting.class).error("Error occurred: {}", e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected List<UsersTabDescriptor.GridData> getItems() {

	return getUsers(). //
		stream().//
		map(v -> new UsersTabDescriptor.GridData(v, getGrid())).//
		sorted((v1, v2) -> v2.getIdentifier().compareTo(v1.getIdentifier())).//
		toList();
    }

    /**
     * @author Fabrizio
     */
    protected static class GridFilter extends AbstractGridFilter<UsersTabDescriptor.GridData> {

	@Override
	protected String getItemValue(String colum, UsersTabDescriptor.GridData gridData) {

	    return switch (colum) {
		case ID_COLUMN -> gridData.getIdentifier();
		case EMAIL_COLUMN -> gridData.getEmail();
		case FIRST_NAME_COLUMN -> gridData.getFirstName();
		case SECOND_NAME_COLUMN -> gridData.getLastName();

		case INSTITUTION_COLUMN -> gridData.getInstitution();
		case REG_DATE_COLUMN -> gridData.getRegistrationDate();

		case ROLE_COLUMN -> gridData.getRole();
		case ID_TYPE_COLUMN -> gridData.getUserIdentifierType();
		case ENABLED_COLUMN -> gridData.isEnabled();
		default -> null;
	    };
	}
    }

    @Override
    protected AbstractGridFilter<UsersTabDescriptor.GridData> createGridFilter() {

	return new UsersTabDescriptor.GridFilter();
    }

    @Override
    protected Class<UsersTabDescriptor.GridData> getGridDataModel() {

	return UsersTabDescriptor.GridData.class;
    }

    /**
     * @param configuration
     * @param readOnly
     * @param container
     * @return
     */
    @Override
    protected Optional<Renderer<UsersTabDescriptor.GridData>> createItemDetailsRenderer() {

	return Optional.of(new ComponentRenderer<>((gridData) -> {

	    try {

		GSUser user = getUsers().//
			stream().//
			filter(u -> u.getIdentifier().equals(gridData.getIdentifier())).
			findFirst().get();//

		List<GSProperty> properties = user.getProperties();

		String content = "<div style='margin: auto; width: 1000px; padding: 15px'><style>\n" + //

			"table { border-collapse: collapse; border: 1px solid lightgray;  width: 100%  }\n" +//

			"td { border: 1px solid lightgray; padding: 5px;  }\n" +//

			".th { background-color: #f0f0f0; font-weight: bold; padding: 5px;  }\n" +//

			"</style>\n";

		StringBuilder table = new StringBuilder("<table>");

		table.append("<tr>");

		table.append("<td class='th'>");

		table.append("Attribute");

		table.append("</td>");

		table.append("<td class='th'>");

		table.append("Value");

		table.append("</td>");

		table.append("</tr>");

		for (GSProperty property : properties) {

		    table.append("<tr>");

		    table.append("<td>");

		    table.append(property.getName());

		    table.append("</td>");

		    table.append("<td>");

		    table.append(property.getValue());

		    table.append("</td>");

		    table.append("</tr>");
		}

		table.append("</table>");

		content += table;

		content += "</div>";

		return new Html(content);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(ViewsDescriptor.class).error("Error occurred: {}", e.getMessage(), e);
	    }

	    return null;
	}));
    }

    /**
     * @author Fabrizio
     */
    protected static class GridData implements GridDataModel {

	private final GSUser user;
	private final Grid<UsersTabDescriptor.GridData> grid;

	/**
	 * @param user
	 * @param grid
	 */
	private GridData(GSUser user, Grid<UsersTabDescriptor.GridData> grid) {

	    this.user = user;
	    this.grid = grid;
	}

	/**
	 * @return
	 */
	public String getPosition() {

	    return String.valueOf(//

		    grid.getListDataView().//
			    getItems().//
			    toList().//
			    stream().map(UsersTabDescriptor.GridData::getIdentifier).toList().//
			    indexOf(user.getIdentifier()) + 1);
	}

	@Override
	public String getIdentifier() {

	    return Optional.ofNullable(user.getIdentifier()).orElse("");
	}

	/**
	 * @return
	 */
	public String getEmail() {

	    return user.getStringPropertyValue("email").orElse("");
	}

	/**
	 * @return
	 */
	public String getFirstName() {

	    return user.getStringPropertyValue("firstName").orElse("");
	}

	/**
	 * @return
	 */
	public String getLastName() {

	    return user.getStringPropertyValue("lastName").orElse("");
	}

	/**
	 * @return
	 */
	public String getRegistrationDate() {

	    return user.getStringPropertyValue("registrationDate").orElse("");
	}

	/**
	 * @return
	 */
	public String getInstitution() {

	    return user.getStringPropertyValue("institution").orElse("");
	}

	/**
	 * @return the size
	 */
	public String getRole() {

	    return Optional.ofNullable(user.getRole()).orElse("");
	}

	/**
	 * @return
	 */
	public String getUserIdentifierType() {

	    return Optional.ofNullable(user.getUserIdentifierType()).map(String::valueOf).orElse("");

	}

	/**
	 * @return
	 */
	public String isEnabled() {

	    return String.valueOf(user.isEnabled());
	}

	@Override
	public String toString() {

	    return getIdentifier();
	}

	@Override
	public boolean equals(Object o) {

	    return o instanceof UsersTabDescriptor.GridData && ((UsersTabDescriptor.GridData) o).getIdentifier()
		    .equals(this.getIdentifier());
	}
    }
}
