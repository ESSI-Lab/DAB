package org.jvnet.jaxb2_commons.locator;

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

import java.text.MessageFormat;

/**
 * Validation event locator.
 * 
 * @author Aleksei Valikov
 */
public final class DefaultPropertyObjectLocator extends AbstractObjectLocator
		implements PropertyObjectLocator {

	/**
	 * Field name.
	 */
	protected final String propertyName;

	/**
	 * Constructs a new validation event locator.
	 * 
	 * @param parentLocator
	 *            parent location (may be <code>null</code>).
	 * @param object
	 *            object.
	 * @param propertyName
	 *            field name.
	 */
	protected DefaultPropertyObjectLocator(final ObjectLocator parentLocator,
			final String propertyName, final Object propertyValue) {
		super(parentLocator, propertyValue);
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Returns parameters for message formatting.
	 * 
	 * @return Message formatting parameters.
	 */
	public Object[] getMessageParameters() {
		return new Object[] { getObject(), getPropertyName() };
	}

	@Override
	protected String getDefaultMessage() {
		return MessageFormat.format("Field: {1}\nField value: {0}.",
				getMessageParameters());
	}

	@Override
	protected String getStepAsString() {
		return "." + getPropertyName();
	}

}
