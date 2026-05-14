package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.contextmenu.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.server.*;
import eu.essi_lab.cdk.harvest.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.index.jaxb.*;
import eu.essi_lab.model.resource.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class HarvestPreviewStarter extends GridMenuItemHandler implements ButtonChangeListener {

    private static final int DIALOG_HEIGHT = 700;
    private static final int DIALOG_WIDTH = 700;
    private ConfirmationDialog dialog;
    private VaadinSession session;
    private TextArea originalArea;
    private TextArea isoArea;
    private TextArea mappedArea;
    private Div propDiv;
    private TabSheet tabSheet;
    private Setting setting;
    private Html msgDiv;
    private CopyToClipboardButton clipboardButton;

    /**
     *
     */
    public HarvestPreviewStarter() {

	super(true, false);
    }

    @Override
    public void onClick(//
	    GridContextMenu.GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent,//
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selected) {

	this.setting = setting.get();

	session = VaadinSession.getCurrent();

	dialog = new ConfirmationDialog("Harvest preview", this);

	dialog.setHeader("Harvest preview");
	dialog.getFooterLayout().getStyle().remove("height");
	dialog.getContentLayout().getStyle().set("padding-bottom", "0px");
	dialog.setConfirmText("Start");

	dialog.setHeight(DIALOG_HEIGHT, Unit.PIXELS);
	dialog.setWidth(DIALOG_WIDTH, Unit.PIXELS);

	dialog.setMinHeight(DIALOG_HEIGHT, Unit.PIXELS);
	dialog.setMinWidth(DIALOG_WIDTH, Unit.PIXELS);

	dialog.setResizable(true);

	dialog.setCancelText("Close");
	dialog.setCloseOnConfirm(false);

	//
	//
	//

	VerticalLayout layout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	layout.getStyle().set("margin-left", "-15px");
	layout.getStyle().set("padding-right", "0px");

	layout.setSizeFull();

	//
	//
	//

	msgDiv = new Html("<div>Click the 'Start' button to proceed</div>");
	msgDiv.getStyle().set("font-size", "14px");

	layout.add(msgDiv);

	//
	//
	//

	tabSheet = new TabSheet();
	tabSheet.getElement().setEnabled(false);
	tabSheet.setSizeFull();

	tabSheet.addSelectedChangeListener((evt) -> {

	    if (clipboardButton != null) {
		clipboardButton.setEnabled(tabSheet.getSelectedIndex() != 3);
	    }
	});

	originalArea = createTextArea();

	isoArea = createTextArea();

	mappedArea = createTextArea();

	propDiv = new Div();
	propDiv.setWidthFull();
	propDiv.getStyle().set("padding-bottom", "10px");
	propDiv.setMaxHeight(DIALOG_HEIGHT - 360, Unit.PIXELS);

	tabSheet.add("Original metadata", createAreaDiv(originalArea));
	tabSheet.add("ISO metadata", createAreaDiv(isoArea));
	tabSheet.add("Mapped resource", createAreaDiv(mappedArea));
	tabSheet.add("Indexed properties", propDiv);

	layout.add(tabSheet);

	//
	//
	//

	clipboardButton = ComponentFactory.createCopyToClipboardButton(() -> switch (tabSheet.getSelectedIndex()) {
	    case 0 -> originalArea.getValue();
	    case 1 -> isoArea.getValue();
	    case 2 -> mappedArea.getValue();
	    default -> throw new IllegalStateException("Unexpected value: " + tabSheet.getSelectedIndex());
	});

	clipboardButton.getStyle().set("margin-bottom", "-15px");

	layout.add(clipboardButton);

	//
	//
	//

	dialog.setContent(layout);
	dialog.open();
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	Collection<UI> uIs = session.getUIs();

	msgDiv.setHtmlContent("<div>Preview preparation in progress, please wait...</div>");

	dialog.getConfirmButton().setEnabled(false);

	new Thread(() -> {

	    try {

		HarvestingSetting harvestingSetting = SettingUtils.downCast(setting, HarvestingSettingLoader.load().getClass());

		Chronometer chronometer = new Chronometer(Chronometer.TimeFormat.SEC_MLS);
		chronometer.start();

		HarvestPreviewTool.Preview preview = HarvestPreviewTool.get(harvestingSetting);

		session.access(() -> {

		    msgDiv.setHtmlContent(
			    "<div>Preview ready. Elapsed time (sec:mls): <b>" + chronometer.formatElapsedTime() + "</b></div>");

		    tabSheet.getElement().setEnabled(true);

		    originalArea.setValue(preview.getOriginalMetadata());
		    isoArea.setValue(preview.getHarmonizedMetadata());
		    mappedArea.setValue(preview.getMappedResourceString());

		    Html propertiesTable = createPropertiesTable(preview.getMappedResource().getIndexesMetadata());
		    propDiv.add(propertiesTable);

		    uIs.iterator().next().push();

		});

	    } catch (Exception e) {

		session.access(() -> {

		    msgDiv.setHtmlContent(
			    "<div><b>Error occurred</b>: <span style='color: red'>" + e.getMessage() + "</span></style></div>");

		    uIs.iterator().next().push();
		});
	    }
	}).start();
    }

    /**
     * @param indexes
     * @return
     */
    private Html createPropertiesTable(IndexesMetadata indexes) {

	String content = "<div><style>\n" + //

		"table { font-size: 13px; border-collapse: collapse; border: 1px solid lightgray;  width: 100%  }\n" +//

		"td { border: 1px solid lightgray; padding: 5px;  }\n" +//

		".th { background-color: #f0f0f0; font-weight: bold; padding: 5px;  }\n" +//

		"</style>\n";

	String table = "<table>";

	table += "<tr>";

	table += "<td class='th'>";

	table += "Property";

	table += "</td>";

	table += "<td class='th'>";

	table += "Value";

	table += "</td>";

	table += "</tr>";

	ArrayList<String> props = new ArrayList<>(indexes.getProperties());
	props.add(MetadataElement.BOUNDING_BOX.getName());

	String bbox = indexes.readBoundingBox().map(BoundingBox::toString).orElse("");

	for (String property : props.stream().sorted().distinct().toList()) {

	    String value = property.equals(MetadataElement.BOUNDING_BOX.getName())
		    ? bbox
		    : indexes.read(property).toString().replace("[", "").replace("]", "");

	    if (value.isEmpty()) {
		continue;
	    }

	    table += "<tr>";

	    table += "<td>";

	    table += property;

	    table += "</td>";

	    table += "<td>";

	    table += value;

	    table += "</td>";

	    table += "</tr>";
	}

	table += "</table>";

	content += table;

	content += "</div>";

	return new Html(content);
    }

    /**
     * @param area
     * @return
     */
    private Div createAreaDiv(TextArea area) {

	Div div = ComponentFactory.createDiv();

	div.setId("area-div");
	div.getStyle().set("padding-bottom", "0px !important");
	div.getStyle().set("margin-left", "-15px");

	div.setSizeFull();
	div.setMaxHeight(DIALOG_HEIGHT - 290, Unit.PIXELS);

	div.add(area);

	return div;
    }

    /**
     * @return
     */
    private TextArea createTextArea() {

	TextArea area = new TextArea();

	area.getStyle().set("font-size", "13px");
	area.setWidthFull();
	area.setMinHeight(DIALOG_HEIGHT - 290, Unit.PIXELS);
	area.addClassName("text-area-readonly");
	area.setReadOnly(true);

	return area;
    }

    @Override
    public String getItemText() {

	return "Harvest preview";
    }
}
