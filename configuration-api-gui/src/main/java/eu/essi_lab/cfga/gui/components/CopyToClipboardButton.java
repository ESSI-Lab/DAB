package eu.essi_lab.cfga.gui.components;

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
import com.vaadin.flow.component.button.*;
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
