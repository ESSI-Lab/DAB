package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.renderer.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.auth.*;
import eu.essi_lab.model.exceptions.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class UsersTabDescriptor extends AbstractGridDescriptor<UsersTabDescriptor.GridData> {

    private static final String ID_COLUMN = "Id";
    private static final String ROLE_COLUMN = "Role";
    private static final String ID_TYPE_COLUMN = "Id. Type";
    private static final String ENABLED_COLUMN = "Enabled";

    /**
     *
     */
    public UsersTabDescriptor() {

	setLabel("Users");

	getGrid().addColumn(UsersTabDescriptor.GridData::getPosition).//
		setHeader("").//
		setKey("#").//
		setWidth("15px");

	Grid.Column<UsersTabDescriptor.GridData> idCol = addSortableResizableColumn(ID_COLUMN, UsersTabDescriptor.GridData::getIdentifier,
		300);

	Grid.Column<UsersTabDescriptor.GridData> depCol = addSortableResizableColumn(ROLE_COLUMN, UsersTabDescriptor.GridData::getRole,
		300);

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
     * @throws GSException
     */
    private static DatabaseReader getReader() throws GSException {

	DatabaseSetting setting = ConfigurationWrapper.getDatabaseSetting();

	return DatabaseProviderFactory.getReader(setting.asStorageInfo());
    }

    /**
     * @return
     */
    private List<GSUser> getUsers() {

	try {

	    return getReader().getUsers();

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

		GSUser user = getReader().getUser(gridData.getIdentifier()).get();

		List<GSProperty> properties = user.getProperties();

		String content =
			"<div style='margin: auto; width: 1000px; padding: 15px'><style>\n" + //

				"table { border-collapse: collapse; border: 1px solid lightgray;  width: 100%  }\n" +//

				"td { border: 1px solid lightgray; padding: 5px;  }\n" +//

				".th { background-color: #f0f0f0; font-weight: bold; padding: 5px;  }\n" +//

				"</style>\n" ;

		String table = "<table>";

		table += "<tr>";

		table += "<td class='th'>";

		table += "Attribute";

		table += "</td>";

		table += "<td class='th'>";

		table += "Value";

		table += "</td>";

		table += "</tr>";

		for (GSProperty property : properties) {

		    table += "<tr>";

		    table += "<td>";

		    table += property.getName();

		    table += "</td>";

		    table += "<td>";

		    table += property.getValue();

		    table += "</td>";

		    table += "</tr>";
		}

		table += "</table>";

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
     * @return
     */
    private TextArea createTextArea(String string) {

	TextArea area = new TextArea();
	area.getStyle().set("font-size", "13px");
	area.getStyle().set("vertical-overflow", "auto");
	area.addClassName("text-area-readonly");
	area.setHeightFull();
	area.setWidthFull();
	area.setReadOnly(true);
	area.setMaxHeight(500, Unit.PIXELS);
	area.setValue(string);

	return area;
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
