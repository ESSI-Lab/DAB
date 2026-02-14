package eu.essi_lab.cfga.gui.components;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dependency.*;
import com.vaadin.flow.component.icon.*;

import java.util.function.*;

/**
 * @author Fabrizio
 */
public class CopyToClipboardButton extends Button {

    private Supplier<String> supplier;

    /**
     *
     */
    public CopyToClipboardButton() {

	this(() -> "");
    }

    /**
     * @param supplier
     */
    public CopyToClipboardButton(Supplier<String> supplier) {

	super(VaadinIcon.COPY.create());

	setSupplier(supplier);

	addThemeVariants(ButtonVariant.LUMO_SMALL);

	setTooltipText("Copy to clipboard");

	getStyle().set("border", "1px solid hsl(0deg 0% 81%)");
	getStyle().set("border-radius", "0px");

	addClickListener(e -> {

	    if (!getSupplier().get().isEmpty()) {

		UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", getSupplier().get());
	    }
	});
    }

    /**
     * @param supplier
     */
    public void setSupplier(Supplier<String> supplier) {

	this.supplier = supplier;
    }

    /**
     * @return
     */
    public Supplier<String> getSupplier() {

	return supplier;
    }
}
