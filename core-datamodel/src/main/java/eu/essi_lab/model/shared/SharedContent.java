package eu.essi_lab.model.shared;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ilsanto
 */
public class SharedContent<T> {

    /**
     * @author Fabrizio
     */
    public enum SharedContentCategory {

	/**
	 * 
	 */
	DATABASE_CACHE("Database cache"),
	/**
	 * 
	 */
	LOCAL_CACHE("File system cache"),
	/**
	 * 
	 */
	LOCAL_PERSISTENT("File system persistent"),
	/**
	 * 
	 */
	ELASTIC_SEARCH_PERSISTENT("Elastic search persistent");

	private String label;

	/**
	 * @param label
	 */
	private SharedContentCategory(String label) {

	    this.label = label;
	}

	/**
	 * @return
	 */
	public String getLabel() {

	    return label;
	}

	/**
	 * @return
	 */
	public static List<String> getLabels() {

	    return Arrays.asList(values()).//
		    stream().//
		    map(v -> v.getLabel()).//
		    collect(Collectors.toList());
	}

	/**
	 * @param label
	 * @return
	 */
	public static Optional<SharedContentCategory> fromLabel(String label) {

	    return Arrays.asList(values()).//
		    stream().//

		    filter(v -> v.getLabel().equals(label)).//
		    findFirst();
	}
    }

    /**
     * @author Fabrizio
     */
    public enum SharedContentType {

	/**
	 * 
	 */
	GS_RESOURCE_TYPE,
	/**
	 * 
	 */
	GENERIC_TYPE,
	/**
	 * 
	 */
	FILE_TYPE,	
	/**
	 * 
	 */
	JSON_TYPE
    }

    private SharedContentType type;
    private SharedContentCategory category;
    private String identifier;
    private T content;

    public SharedContentType getType() {

	return type;
    }

    /**
     * @param type
     */
    public void setType(SharedContentType type) {

	this.type = type;
    }

    /**
     * @return
     */
    public SharedContentCategory getCategory() {

	return category;
    }

    /**
     * @param category
     */
    public void setCategory(SharedContentCategory category) {

	this.category = category;
    }

    /**
     * @return
     */
    public String getIdentifier() {

	return identifier;
    }

    /**
     * @param identifier
     */
    public void setIdentifier(String identifier) {

	this.identifier = identifier;
    }

    /**
     * @return
     */
    public T getContent() {

	return content;
    }

    /**
     * @param content
     */
    public void setContent(T content) {

	this.content = content;
    }
}
