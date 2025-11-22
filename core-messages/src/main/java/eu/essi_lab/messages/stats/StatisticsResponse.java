/**
 * 
 */
package eu.essi_lab.messages.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
@XmlRootElement(name = "StatisticsResponse", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class StatisticsResponse extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(StatisticsResponse.class);
	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(StatisticsResponse.class).error("Can't get jaxb context", e);
	}
    }

    @XmlAttribute(name = "groupBy")
    private String groupBy;

    @XmlAttribute(name = "itemsCount", required = true)
    private int itemsCount;

    @XmlAttribute(name = "startIndex", required = true)
    private int startIndex;

    @XmlTransient
    private final List<ResponseItem> itemsList;

    /**
     * 
     */
    public StatisticsResponse() {

	this(null);
    }

    /**
     * @param groupBy
     */
    public StatisticsResponse(String groupBy) {
	itemsList = new ArrayList<>();
	if (groupBy != null) {
	    setGroupBy(groupBy);
	}
	startIndex = 1;
    }

    /**
     * @return
     */
    @XmlTransient
    public int getItemsCount() {

	return itemsCount;
    }
    
    /**
     * @return
     */
    public void setItemsCount(int count) {

	itemsCount = count;
    }


    /**
     * @return
     */
    @XmlTransient
    public int getStartIndex() {

	return startIndex;
    }

    /**
     * @return
     */
    public void setStartIndex(int startIndex) {

	this.startIndex = startIndex;
    }

    /**
     * @return
     */
    @XmlAttribute(name = "returnedItemsCount", required = true)
    public int getReturnedItemsCount() {

	return itemsList.size();
    }

    @XmlElement(name = "ResponseItem", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public List<ResponseItem> getItems() {

	return itemsList;
    }

    /**
     * @return
     */
    @XmlTransient
    public Optional<String> getGroupBy() {

	return Optional.ofNullable(groupBy);
    }

    /**
     * @param groupBy
     */
    public void setGroupBy(String groupBy) {

	this.groupBy = groupBy;
    }

    /**
     * If this response items are grouped by a {@link Queryable} which expresses an amount of time in milliseconds, this
     * method converts
     * the range expressed in the form "milliseconds-milliseconds" of each item "groupedBy" attribute, in the most
     * readable form "ISODateTime-ISODateTime"
     */

    public void convertGroupedByMillisToISODataTime() {

	if (getGroupBy().isPresent()) {

	    getItems().forEach(item -> {

		try {

		    String groupedBy = item.getGroupedBy().get();

		    String[] split = groupedBy.split(ResponseItem.ITEMS_RANGE_SEPARATOR);

		    String start = ISO8601DateTimeUtils.getISO8601DateTime(new Date(Long.parseLong(split[0])));
		    String end = ISO8601DateTimeUtils.getISO8601DateTime(new Date(Long.parseLong(split[1])));

		    item.setGroupedBy(start + ResponseItem.ITEMS_RANGE_SEPARATOR + end);

		} catch (Exception ex) {
		}
	    });
	}
    }

    /**
     * If one or more response items is the computation result of a bounding box frequency (either
     * {@link MetadataElement#BOUNDING_BOX} or
     * {@link RuntimeInfoElement#DISCOVERY_MESSAGE_BBOX}), the related result value is modified by replacing
     * each bbox value with the related region name (e.g.:
     * 46.3726520216244,9.53095237240833,49.0211627691393,17.1620685652599ITEMSEP0 -> AustriaITEMSEP0).
     * Furthermore, if this is a computation result from a frequency applied to a bbox,
     * due to the particular implementation of this kind of function with ML (the results do not come from a
     * pre-ordered/pre-sized
     * element values lexicon), they need to be ordered in descending order of the frequency value and reduced according
     * to
     * the <code>maxResults</code> value.<br>
     * 
     * @param maxResults
     */
    public void adjustBboxFrequencyResult(int maxResults) {

	getItems().forEach(item -> {

	    Optional<ComputationResult> bboxFreq = item.getFrequency(MetadataElement.BOUNDING_BOX);
	    Optional<ComputationResult> msgBbox = item.getFrequency(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX);

	    ComputationResult result = null;

	    if (bboxFreq.isPresent()) {

		result = bboxFreq.get();

	    } else if (msgBbox.isPresent()) {

		result = msgBbox.get();
	    }

	    if (result != null) {

		String value = result.getValue();

		//
		// sorts and reduce the list size
		//
		//
		List<String> list = Arrays.stream(value.split(" ")).//
		sorted((v1, v2) -> {

		    Integer freq1 = Integer.valueOf(v1.split(ComputationResult.FREQUENCY_ITEM_SEP)[1]);
		    Integer freq2 = Integer.valueOf(v2.split(ComputationResult.FREQUENCY_ITEM_SEP)[1]);

		    return freq2.compareTo(freq1);// descending order
		}).//
		collect(Collectors.toList());

		list = list.subList(0, Math.min(maxResults, list.size()));

		//
		// converts bboxes to locations
		//
		value = list.//
		stream().//
		map(e -> {

		    String bbox = e.split(ComputationResult.FREQUENCY_ITEM_SEP)[0];
		    String freq = e.split(ComputationResult.FREQUENCY_ITEM_SEP)[1];

		    Optional<String> location = RegionsManager.getURLEncodedLocation(bbox);
		    return location.map(s -> s + ComputationResult.FREQUENCY_ITEM_SEP + freq).orElse(null);

		}).//
		filter(Objects::nonNull).//
		collect(Collectors.joining(" "));

		result.setValue(value);
	    }
	});
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static StatisticsResponse create(String string) throws JAXBException {

	return new StatisticsResponse() {
	}.fromStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static StatisticsResponse create(InputStream stream) throws JAXBException {

	return new StatisticsResponse() {
	}.fromStream(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static StatisticsResponse create(Node node) throws JAXBException {

	return new StatisticsResponse() {
	}.fromNode(node);
    }

    @Override
    public StatisticsResponse fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (StatisticsResponse) unmarshaller.unmarshal(stream);
    }

    @Override
    public StatisticsResponse fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (StatisticsResponse) unmarshaller.unmarshal(node);
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty("jaxb.formatted.output", true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

	return marshaller;
    }

    @Override
    protected Object getElement() {

	return this;
    }
}
