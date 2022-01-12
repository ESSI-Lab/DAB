package eu.essi_lab.indexes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
public abstract class CustomIndexedElements extends IndexedElementsGroup implements Pluggable {

    /**
     * Returns the list of the available custom indexes
     * 
     * @return the list of the available custom indexes
     */
    public abstract List<IndexedMetadataElement> getIndexes();

    /**
     * Checks whether or not the given <code>name</code> complies with the following conventions:
     * <ul>
     * <li>it matches the regular expression [a-zA-Z]</li>
     * <li>the only allowed exception to the above rule is the character '_'</li>
     * <li>it is unique. To avoid name clash with the predefined {@link IndexedElement}s, check the names list
     * with the {@link #printNames()} method</li>
     * </ul>
     * This check is performed by the GI-suite during the following phases:<br>
     * <br>
     * <ul>
     * <li>harvesting</li>
     * <li>automatic indexes configuration of the Database (which support such feature)</li>
     * </ul>
     * If the check fails, the related custom {@link IndexedMetadataElement} is discarded and a warning log is printed
     * 
     * @param name the name of the custom {@link IndexedMetadataElement} under test
     * @return <code>true</code> if the name is valid, <code>false</code> otherwise
     */
    public static boolean checkName(String name) {

	String name_ = name.replace("_", "");
	boolean allLetters = Pattern.matches("[a-zA-Z]+", name_);

	if (!allLetters) {

	    GSLoggerFactory.getLogger(CustomIndexedElements.class)
		    .warn("Given indedexd element name " + name + " contains invalid characters");
	    return false;
	}

	List<IndexedElementInfo> supportedIndexes = IndexedMetadataElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC);
	supportedIndexes.addAll(IndexedElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));
	supportedIndexes.addAll(IndexedResourceElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));

	List<String> predNames = supportedIndexes.stream().//
		map(i -> i.getElementName()).//
		collect(Collectors.toCollection(() -> new ArrayList<String>()));

	if (predNames.contains(name)) {

	    GSLoggerFactory.getLogger(CustomIndexedElements.class).warn("Given indedexd element name " + name + " is already defined");
	    return false;
	}

	return true;
    }

    /**
     * Utility method which prints the names of all the {@link IndexedElement}s defined in the GI-suite
     */
    public static void printNames() {

	List<IndexedElementInfo> supportedIndexes = IndexedMetadataElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC);
	supportedIndexes.addAll(IndexedElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));
	supportedIndexes.addAll(IndexedResourceElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));

	supportedIndexes.forEach(i -> System.out.println(i.getElementName()));
    }

    /**
     * Returns the list of {@link IndexedElementInfo} of the available custom indexes
     * 
     * @param impl the database implementation
     * @return the list of {@link IndexedElementInfo} of the available custom indexes
     * @see #getIndexes()
     */
    public List<IndexedElementInfo> getIndexesInfo(DatabaseImpl impl) {

	return getIndexes().//
		stream().//
		map(el -> el.getInfo(impl.getName())).//
		collect(Collectors.toList());//
    }
}
