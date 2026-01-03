package eu.essi_lab.model.resource;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;

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

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.composed.ComposedElement;
import eu.essi_lab.model.resource.composed.ComposedElementBuilder;

/**
 * @author boldrini
 */
public class QualifierElementWrapper {

    private ComposedElement element;

    /**
     * @param element
     */
    public QualifierElementWrapper(ComposedElement element) {

	this.element = element;
    }

    /**
     * @param name
     * @return
     */
    public static ComposedElement build() {

	return ComposedElementBuilder.get("qualifier").//

		addItem("qualifierCode", Queryable.ContentType.TEXTUAL).//
		addItem("qualifierDescription", Queryable.ContentType.TEXTUAL).//
		build();
    }

    /**
     * @return
     */
    public static QualifierElementWrapper get() {

	return new QualifierElementWrapper(build());
    }

    /**
     * @return the element
     */
    public ComposedElement getElement() {

	return element;
    }

    /**
     * @return
     */
    public String getCode() {

	return element.getProperty("qualifierCode").get().getStringValue();
    }

    /**
     * @param code
     */
    public void setCode(String code) {

	element.getProperty("qualifierCode").get().setValue(code);
    }

    /**
     * @return
     */
    public String getDescription() {

	return element.getProperty("qualifierDescription").get().getStringValue();
    }

    /**
     * @param description
     */
    public void setDescription(String description) {

	element.getProperty("qualifierDescription").get().setValue(description);
    }

}
