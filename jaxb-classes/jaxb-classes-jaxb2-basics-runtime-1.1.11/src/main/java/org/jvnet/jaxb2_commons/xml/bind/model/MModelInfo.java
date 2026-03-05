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

import java.util.Collection;

import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.xml.bind.model.origin.MModelInfoOrigin;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MOriginated;

public interface MModelInfo<T, C extends T> extends MCustomizable,
		MOriginated<MModelInfoOrigin> {

	public Collection<MBuiltinLeafInfo<T, C>> getBuiltinLeafInfos();

	public Collection<MClassInfo<T, C>> getClassInfos();

	public MClassInfo<T, C> getClassInfo(String name);

	public Collection<MEnumLeafInfo<T, C>> getEnumLeafInfos();

	public Collection<MTypeInfo<T, C>> getTypeInfos();

	public MTypeInfo<T, C> getTypeInfo(QName typeNam);

	public Collection<MElementInfo<T, C>> getElementInfos();

	public MElementInfo<T, C> getGlobalElementInfo(QName elementName);

	public void addBuiltinLeafInfo(MBuiltinLeafInfo<T, C> builtinLeafInfo);

	public void addEnumLeafInfo(MEnumLeafInfo<T, C> enumLeafInfo);

	public void removeEnumLeafInfo(MEnumLeafInfo<T, C> enumLeafInfo);

	public void addClassInfo(MClassInfo<T, C> classInfo);

	public void removeClassInfo(MClassInfo<T, C> classInfo);

	public void addElementInfo(MElementInfo<T, C> elementInfo);

	public void removeElementInfo(MElementInfo<T, C> elementInfo);
}
