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

public final class DefaultRootObjectLocator extends AbstractObjectLocator
		implements RootObjectLocator {

	public DefaultRootObjectLocator(Object rootObject) {
		super(null, rootObject);
	}

	public Object[] getMessageParameters() {
		return new Object[] { getObject() };
	}

	@Override
	protected String getDefaultMessage() {
		return MessageFormat
				.format("Root object: {0}.", getMessageParameters());
	}

	@Override
	protected String getStepAsString() {
		return "<root>";
	}
}
