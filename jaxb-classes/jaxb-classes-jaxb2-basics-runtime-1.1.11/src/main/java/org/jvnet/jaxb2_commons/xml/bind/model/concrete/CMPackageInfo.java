package org.jvnet.jaxb2_commons.xml.bind.model.concrete;

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

import org.jvnet.jaxb2_commons.lang.StringUtils;
import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MPackageInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPackageInfoOrigin;

public class CMPackageInfo implements MPackageInfo {

	private final MPackageInfoOrigin origin;
	private final String packageName;

	public CMPackageInfo(MPackageInfoOrigin origin, String packageName) {
		Validate.notNull(origin);
		Validate.notNull(packageName);
		this.origin = origin;
		this.packageName = packageName;
	}

	public MPackageInfoOrigin getOrigin() {
		return origin;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getPackagedName(String localName) {
		if (StringUtils.isEmpty(packageName)) {
			return localName;
		} else {
			return packageName + "." + localName;
		}
	}

	public String getLocalName() {
		return null;
	}

	public String getContainerLocalName(String delimiter) {
		return null;
	}

	public MPackageInfo getPackageInfo() {
		return this;
	}

}
