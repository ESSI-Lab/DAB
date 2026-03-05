package org.jvnet.jaxb2_commons.xml.bind.model;

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

import java.util.List;

import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.xml.bind.model.origin.MEnumLeafInfoOrigin;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MOriginated;

public interface MEnumLeafInfo<T, C extends T> extends MPackagedTypeInfo<T, C>,
		MTypeInfo<T, C>, MOriginated<MEnumLeafInfoOrigin> {

	public C getTargetClass();

	public MTypeInfo<T, C> getBaseTypeInfo();

	public List<MEnumConstantInfo<T, C>> getConstants();

	public void addEnumConstantInfo(MEnumConstantInfo<T, C> enumConstantInfo);

	public void removeEnumConstantInfo(MEnumConstantInfo<T, C> enumConstantInfo);

	public QName getElementName();

	public MElementInfo<T, C> createElementInfo(MClassInfo<T, C> scope,
			QName substitutionHead);

}
