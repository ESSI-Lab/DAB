package eu.essi_lab.model.resource;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;

/**
 * @author Fabrizio
 */
public class GSResourceComparator {

    /**
     * 
     */
    private GSResourceComparator() {

    }

    /**
     * @param targets
     * @param res1
     * @param res2
     * @return
     */
    public static ComparisonResponse compare(List<Queryable> targets, GSResource res1, GSResource res2) {

	ComparisonResponse response = new ComparisonResponse();

	targets.forEach(target -> {

	    IndexesMetadata im1 = res1.getIndexesMetadata();
	    IndexesMetadata im2 = res2.getIndexesMetadata();

	    if (target.getName().equals(MetadataElement.BOUNDING_BOX.getName())) {

		Optional<BoundingBox> optBbox1 = im1.readBoundingBox();
		Optional<BoundingBox> optBbox2 = im2.readBoundingBox();

		ComparisonValues values = null;

		if (optBbox1.isPresent() && optBbox2.isEmpty()) {

		    values = new ComparisonValues(ContentType.SPATIAL, optBbox1.get());

		} else if (optBbox1.isEmpty() && optBbox2.isPresent()) {

		    values = new ComparisonValues(ContentType.SPATIAL, optBbox2.get());

		} else {

		    BoundingBox bbox1 = optBbox1.get();
		    BoundingBox bbox2 = optBbox2.get();

		    if (!bbox1.getCardinalValues().equals(bbox2.getCardinalValues())) {

			values = new ComparisonValues(ContentType.SPATIAL, bbox1, bbox2);
		    }
		}

		if (values != null) {

		    response.addComparisonValues(target, values);
		}

	    } else {

		List<String> prop1 = im1.read(target.getName()).stream().sorted().collect(Collectors.toList());
		List<String> prop2 = im2.read(target.getName()).stream().sorted().collect(Collectors.toList());

		if (!prop1.equals(prop2)) {

		    ComparisonValues values = new ComparisonValues(target.getContentType(), prop1, prop2);

		    if (prop1.isEmpty()) {

			values = new ComparisonValues(target.getContentType(), prop2);

		    } else if (prop2.isEmpty()) {

			values = new ComparisonValues(target.getContentType(), prop1);
		    }

		    response.addComparisonValues(target, values);
		}
	    }
	});

	return response;
    }

    /**
     * @author Fabrizio
     */
    public static class ComparisonResponse {

	private HashMap<Queryable, ComparisonValues> map;

	/**
	 * 
	 * 
	 */
	public ComparisonResponse() {

	    map = new HashMap<Queryable, GSResourceComparator.ComparisonValues>();
	}

	/**
	 * @param property
	 * @param values
	 */
	public void addComparisonValues(Queryable property, ComparisonValues values) {

	    map.put(property, values);
	}

	/**
	 * @return
	 */
	public List<Queryable> getProperties() {

	    return map.keySet().stream().sorted((q1, q2) -> q1.getName().compareTo(q2.getName())).collect(Collectors.toList());
	}

	/**
	 * @param property
	 * @return
	 */
	public Optional<ComparisonValues> getComparisonValues(Queryable property) {

	    return Optional.ofNullable(map.get(property));
	}
    }

    /**
     * @author Fabrizio
     * @param <ContentType>
     */
    public static class ComparisonValues {

	private ContentType contentType;
	private List<String> values1;
	private List<String> values2;

	/**
	 * @param contentType
	 * @param values1
	 */
	public ComparisonValues(ContentType contentType, List<String> values1) {

	    this(contentType, values1, new ArrayList<String>());
	}

	/**
	 * @param contentType
	 * @param values1
	 * @param values2
	 */
	public ComparisonValues(ContentType contentType, List<String> values1, List<String> values2) {
	    this.contentType = contentType;
	    this.values1 = values1;
	    this.values2 = values2;
	}

	/**
	 * @param contentType
	 * @param bbox1
	 */
	public ComparisonValues(ContentType contentType, BoundingBox bbox1) {

	    this(contentType, bbox1, null);
	}

	/**
	 * @param contentType
	 * @param bbox1
	 * @param bbox2
	 */
	public ComparisonValues(ContentType contentType, BoundingBox bbox1, BoundingBox bbox2) {

	    this.contentType = contentType;

	    try {
		this.values1 = Arrays.asList(bbox1.asString(true));

		if (bbox2 != null) {

		    this.values2 = Arrays.asList(bbox2.asString(true));

		} else {

		    this.values2 = new ArrayList<String>();
		}

	    } catch (UnsupportedEncodingException | JAXBException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	/**
	 * @return
	 */
	public List<String> getValues1() {

	    return values1;
	}

	/**
	 * @return
	 */
	public List<String> getValues2() {

	    return values2;
	}

	/**
	 * @return
	 */
	public ContentType getContentType() {

	    return contentType;
	}

    }
}
