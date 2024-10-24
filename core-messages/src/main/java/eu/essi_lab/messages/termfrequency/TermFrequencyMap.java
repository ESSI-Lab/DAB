package eu.essi_lab.messages.termfrequency;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

public class TermFrequencyMap extends DOMSerializer {

    private static JAXBContext context;
    static {

	try {
	    context = JAXBContext.newInstance(TermFrequencyMapType.class);

	} catch (JAXBException e) {

	    e.printStackTrace();
	}
    }

    public enum ItemsSortOrder {
	/**
	 * 
	 */
	BY_FREQUENCY,
	/**
	 * 
	 */
	BY_TERM
    }

    public enum TermFrequencyTarget {
	/**
	 * 
	 */
	FORMAT(MetadataElement.DISTRIBUTION_FORMAT_EL_NAME),
	/**
	 * 
	 */
	KEYWORD(MetadataElement.KEYWORD_EL_NAME),
	/**
	 * 
	 */
	SOURCE(ResourceProperty.SOURCE_ID.getName()),
	/**
	 * 
	 */
	SSC_SCORE(ResourceProperty.SSC_SCORE.getName()),
	/**
	 * 
	 */
	PROTOCOL(MetadataElement.ONLINE_PROTOCOL_EL_NAME),
	/**
	 * 
	 */
	INSTRUMENT_IDENTIFIER(MetadataElement.INSTRUMENT_IDENTIFIER_EL_NAME),
	/**
	 * 
	 */
	INSTRUMENT_TITLE(MetadataElement.INSTRUMENT_TITLE_EL_NAME),
	/**
	 * 
	 */
	PLATFORM_IDENTIFIER(MetadataElement.PLATFORM_IDENTIFIER_EL_NAME),
	/**
	 * 
	 */
	PLATFORM_TITLE(MetadataElement.PLATFORM_TITLE_EL_NAME),
	/**
	 * 
	 */
	ORGANISATION_NAME(MetadataElement.ORGANISATION_NAME_EL_NAME),
	/**
	 * 
	 */
	ORIGINATOR_ORGANISATION_IDENTIFIER(MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER_EL_NAME),
	/**
	 * 
	 */
	ORIGINATOR_ORGANISATION_DESCRIPTION(MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION_EL_NAME),
	/**
	 * 
	 */
	ATTRIBUTE_IDENTIFIER(MetadataElement.ATTRIBUTE_IDENTIFIER_EL_NAME),
	/**
	 * 
	 */
	ATTRIBUTE_TITLE(MetadataElement.ATTRIBUTE_TITLE_EL_NAME),
	/**
	 * 
	 */
	PROD_TYPE(MetadataElement.PRODUCT_TYPE_EL_NAME),
	/**
	 * 
	 */
	SENSOR_OP_MODE(MetadataElement.SENSOR_OP_MODE_EL_NAME),
	/**
	 * 
	 */
	SENSOR_SWATH(MetadataElement.SENSOR_SWATH_EL_NAME),
	/**
	 * 
	 */
	SAR_POL_CH(MetadataElement.SAR_POL_CH_EL_NAME),

	/**
	 * 
	 */
	S3_INSTRUMENT_IDX(MetadataElement.S3_INSTRUMENT_IDX_EL_NAME),
	/**
	 * 
	 */
	S3_PRODUCT_LEVEL(MetadataElement.S3_PRODUCT_LEVEL_EL_NAME),
	/**
	 * 
	 */
	S3_TIMELINESS(MetadataElement.S3_TIMELINESS_EL_NAME);

	private String name;

	private TermFrequencyTarget(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	public static TermFrequencyTarget fromValue(String value) {
	    switch (value) {
	    case MetadataElement.DISTRIBUTION_FORMAT_EL_NAME:
		return FORMAT;
	    case MetadataElement.KEYWORD_EL_NAME:
		return KEYWORD;
	    case ResourceProperty.SOURCE_ID_NAME:
		return SOURCE;
	    case ResourceProperty.SSC_SCORE_EL_NAME:
		return SSC_SCORE;
	    case MetadataElement.ONLINE_PROTOCOL_EL_NAME:
		return PROTOCOL;
	    case MetadataElement.ORGANISATION_NAME_EL_NAME:
		return ORGANISATION_NAME;
	    case MetadataElement.INSTRUMENT_IDENTIFIER_EL_NAME:
		return INSTRUMENT_IDENTIFIER;
	    case MetadataElement.INSTRUMENT_TITLE_EL_NAME:
		return INSTRUMENT_TITLE;
	    case MetadataElement.PLATFORM_IDENTIFIER_EL_NAME:
		return PLATFORM_IDENTIFIER;
	    case MetadataElement.PLATFORM_TITLE_EL_NAME:
		return TermFrequencyTarget.PLATFORM_TITLE;
	    case MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER_EL_NAME:
		return ORIGINATOR_ORGANISATION_IDENTIFIER;
	    case MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION_EL_NAME:
		return ORIGINATOR_ORGANISATION_DESCRIPTION;
	    case MetadataElement.ATTRIBUTE_IDENTIFIER_EL_NAME:
		return ATTRIBUTE_IDENTIFIER;
	    case MetadataElement.ATTRIBUTE_TITLE_EL_NAME:
		return ATTRIBUTE_TITLE;
	    case MetadataElement.PRODUCT_TYPE_EL_NAME:
		return PROD_TYPE;
	    case MetadataElement.SENSOR_OP_MODE_EL_NAME:
		return SENSOR_OP_MODE;
	    case MetadataElement.SENSOR_SWATH_EL_NAME:
		return SENSOR_SWATH;
	    case MetadataElement.SAR_POL_CH_EL_NAME:
		return TermFrequencyTarget.SAR_POL_CH;
	    case MetadataElement.S3_INSTRUMENT_IDX_EL_NAME:
		return TermFrequencyTarget.S3_INSTRUMENT_IDX;
	    case MetadataElement.S3_PRODUCT_LEVEL_EL_NAME:
		return TermFrequencyTarget.S3_PRODUCT_LEVEL;
	    case MetadataElement.S3_TIMELINESS_EL_NAME:
		return TermFrequencyTarget.S3_TIMELINESS;
	    }
	    return null;

	}

	public static List<String> asStringList() {

	    return Arrays.asList(values()).//
		    stream().//
		    map(TermFrequencyTarget::getName).//
		    collect(Collectors.toList());
	}
    }

    private ArrayList<String> targets;
    private TermFrequencyMapType type;

    public TermFrequencyMap() {
    }

    public TermFrequencyMap(TermFrequencyMapType type) {

	this.type = type;
	this.targets = new ArrayList<String>();

	List<TermFrequencyItem> format = type.getFormat();
	List<TermFrequencyItem> keyword = type.getKeyword();
	List<TermFrequencyItem> protocol = type.getProtocol();
	List<TermFrequencyItem> orgName = type.getOrganisationName();
	List<TermFrequencyItem> providerID = type.getSourceId();
	List<TermFrequencyItem> instruments = type.getInstrumentId();
	List<TermFrequencyItem> instrumentTitle = type.getInstrumentTitle();
	List<TermFrequencyItem> platforms = type.getPlatformId();
	List<TermFrequencyItem> platformTitle = type.getPlatformTitle();
	List<TermFrequencyItem> originatorOrganisation = type.getOrigOrgId();
	List<TermFrequencyItem> originatorOrganisationDescription = type.getOrigOrgDescription();
	List<TermFrequencyItem> attribute = type.getAttributeId();
	List<TermFrequencyItem> attributeTitle = type.getAttributeTitle();
	List<TermFrequencyItem> sscScore = type.getSSCScore();

	ArrayList<TermFrequencyItem> prodType = type.getProdType();
	ArrayList<TermFrequencyItem> sarPolCh = type.getSarPolCh();
	ArrayList<TermFrequencyItem> sensorOpMode = type.getSensorOpMode();
	ArrayList<TermFrequencyItem> sensorSwath = type.getSensorSwath();

	ArrayList<TermFrequencyItem> s3InstrumentIdx = type.getS3InstrumentIdx();
	ArrayList<TermFrequencyItem> s3ProductLevel = type.getS3ProductLevel();
	ArrayList<TermFrequencyItem> s3Timeliness = type.getS3Timeliness();

	if (!s3InstrumentIdx.isEmpty()) {
	    targets.add(TermFrequencyTarget.S3_INSTRUMENT_IDX.getName());
	}

	if (!s3ProductLevel.isEmpty()) {
	    targets.add(TermFrequencyTarget.S3_PRODUCT_LEVEL.getName());
	}

	if (!s3Timeliness.isEmpty()) {
	    targets.add(TermFrequencyTarget.S3_TIMELINESS.getName());
	}

	if (!prodType.isEmpty()) {
	    targets.add(TermFrequencyTarget.PROD_TYPE.getName());
	}

	if (!sarPolCh.isEmpty()) {
	    targets.add(TermFrequencyTarget.SAR_POL_CH.getName());
	}

	if (!sensorOpMode.isEmpty()) {
	    targets.add(TermFrequencyTarget.SENSOR_OP_MODE.getName());
	}

	if (!sensorSwath.isEmpty()) {
	    targets.add(TermFrequencyTarget.SENSOR_SWATH.getName());
	}

	if (!format.isEmpty()) {
	    targets.add(TermFrequencyTarget.FORMAT.getName());
	}

	if (!keyword.isEmpty()) {
	    targets.add(TermFrequencyTarget.KEYWORD.getName());
	}

	if (!protocol.isEmpty()) {
	    targets.add(TermFrequencyTarget.PROTOCOL.getName());
	}

	if (!providerID.isEmpty()) {
	    targets.add(TermFrequencyTarget.SOURCE.getName());
	}

	if (!sscScore.isEmpty()) {
	    targets.add(TermFrequencyTarget.SSC_SCORE.getName());
	}

	if (!orgName.isEmpty()) {
	    targets.add(TermFrequencyTarget.ORGANISATION_NAME.getName());
	}

	if (!instruments.isEmpty()) {
	    targets.add(TermFrequencyTarget.INSTRUMENT_IDENTIFIER.getName());
	}
	
	if (!instrumentTitle.isEmpty()) {
	    targets.add(TermFrequencyTarget.INSTRUMENT_TITLE.getName());
	}

	if (!platforms.isEmpty()) {
	    targets.add(TermFrequencyTarget.PLATFORM_IDENTIFIER.getName());
	}
	
	if (!platformTitle.isEmpty()) {
	    targets.add(TermFrequencyTarget.PLATFORM_TITLE.getName());
	}

	if (!originatorOrganisation.isEmpty()) {
	    targets.add(TermFrequencyTarget.ORIGINATOR_ORGANISATION_IDENTIFIER.getName());
	}

	if (!originatorOrganisationDescription.isEmpty()) {
	    targets.add(TermFrequencyTarget.ORIGINATOR_ORGANISATION_DESCRIPTION.getName());
	}

	if (!attribute.isEmpty()) {
	    targets.add(TermFrequencyTarget.ATTRIBUTE_IDENTIFIER.getName());
	}
	
	if (!attributeTitle.isEmpty()) {
	    targets.add(TermFrequencyTarget.ATTRIBUTE_TITLE.getName());
	}
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static TermFrequencyMap create(InputStream stream) throws JAXBException {

	return new TermFrequencyMap() {
	}.fromStream(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static TermFrequencyMap create(Node node) throws JAXBException {

	return new TermFrequencyMap() {
	}.fromNode(node);
    }

    public void sort(ItemsSortOrder sortOrder) {

	for (String target : targets) {

	    List<TermFrequencyItem> items = getItems(//
		    TermFrequencyTarget.fromValue(target), //
		    ItemsSortOrder.BY_FREQUENCY);//
	    getItems(TermFrequencyTarget.fromValue(target)).clear();
	    getItems(TermFrequencyTarget.fromValue(target)).addAll(items);
	}
    }

    public ArrayList<String> getTargets() {

	return targets;
    }

    public int getTargetsCount() {

	return targets.size();
    }

    public List<TermFrequencyItem> getItems(TermFrequencyTarget target) {
	switch (target) {
	case FORMAT:
	    return type.getFormat();
	case PROTOCOL:
	    return type.getProtocol();
	case KEYWORD:
	    return type.getKeyword();
	case ORGANISATION_NAME:
	    return type.getOrganisationName();
	case SSC_SCORE:
	    return type.getSSCScore();
	case SAR_POL_CH:
	    return type.getSarPolCh();
	case PROD_TYPE:
	    return type.getProdType();
	case SENSOR_OP_MODE:
	    return type.getSensorOpMode();
	case SENSOR_SWATH:
	    return type.getSensorSwath();
	case S3_INSTRUMENT_IDX:
	    return type.getS3InstrumentIdx();
	case S3_PRODUCT_LEVEL:
	    return type.getS3ProductLevel();
	case S3_TIMELINESS:
	    return type.getS3Timeliness();
	default:
	case SOURCE:
	    return type.getSourceId();
	case INSTRUMENT_IDENTIFIER:
	    return type.getInstrumentId();
	case INSTRUMENT_TITLE:
	    return type.getInstrumentTitle();
	case ORIGINATOR_ORGANISATION_IDENTIFIER:
	    return type.getOrigOrgId();
	case ORIGINATOR_ORGANISATION_DESCRIPTION:
	    return type.getOrigOrgDescription();
	case PLATFORM_IDENTIFIER:
	    return type.getPlatformId();
	case PLATFORM_TITLE:
	    return type.getPlatformTitle();
	case ATTRIBUTE_IDENTIFIER:
	    return type.getAttributeId();
	case ATTRIBUTE_TITLE:
	    return type.getAttributeTitle();
	}
    }

    public List<TermFrequencyItem> getItems(TermFrequencyTarget target, ItemsSortOrder sortOrder) {

	List<TermFrequencyItem> l = getItems(target);

	TermFrequencyItem[] a = l.toArray(new TermFrequencyItem[] {});

	switch (sortOrder) {
	case BY_FREQUENCY:

	    Arrays.sort(a, new Comparator<TermFrequencyItem>() {

		@Override
		public int compare(TermFrequencyItem o1, TermFrequencyItem o2) {

		    return o1.getFreq() < o2.getFreq() ? 1 : o1.getFreq() > o2.getFreq() ? -1 : 0;
		}
	    });

	    break;

	case BY_TERM:

	    Arrays.sort(a, new Comparator<TermFrequencyItem>() {

		@Override
		public int compare(TermFrequencyItem o1, TermFrequencyItem o2) {

		    return o1.getTerm().compareToIgnoreCase(o2.getTerm());
		}
	    });

	    break;
	}

	return Arrays.asList(a);
    }

    public int getItemsCount(TermFrequencyTarget target) {

	return getItems(target).size();
    }

    public TermFrequencyMap merge(TermFrequencyMap map, int maxItemsCount) {

	List<String> targets = TermFrequencyTarget.asStringList();

	for (String target : targets) {

	    // 1: creates the union list. can contains items with the same term, they must be merged
	    List<TermFrequencyItem> thisItems = this.getItems(TermFrequencyTarget.fromValue(target));
	    List<TermFrequencyItem> mapItems = map.getItems(TermFrequencyTarget.fromValue(target));
	    thisItems.addAll(mapItems);

	    // 2: merges items with same term. the merged frequency is the sum
	    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
	    for (TermFrequencyItem thisItem : thisItems) {
		String term = thisItem.getTerm();
		if (hashMap.containsKey(term)) {
		    Integer freq = hashMap.get(term);
		    hashMap.put(term, freq + thisItem.getFreq());
		} else {
		    hashMap.put(term, thisItem.getFreq());
		}
	    }

	    // 3: replaces the union list with the merged items
	    thisItems.clear();
	    Set<String> keySet = hashMap.keySet();
	    for (String term : keySet) {
		TermFrequencyItem item = new TermFrequencyItem();
		item.setFreq(hashMap.get(term));
		item.setTerm(term);
		thisItems.add(item);
	    }

	    // 4: sorts the list by frequency and cuts off the terms with lower frequencies
	    List<TermFrequencyItem> items = getItems(//
		    TermFrequencyTarget.fromValue(target), //
		    ItemsSortOrder.BY_FREQUENCY);//
	    getItems(TermFrequencyTarget.fromValue(target)).clear();
	    int max = items.size() <= maxItemsCount ? items.size() : maxItemsCount;
	    getItems(TermFrequencyTarget.fromValue(target)).addAll(items.subList(0, max));
	}

	return this;
    }

    @Override
    public String toString() {

	String out = "";

	TermFrequencyTarget[] values = TermFrequencyTarget.values();
	for (TermFrequencyTarget target : values) {

	    out += "**************************************************************\n";
	    out += "* Target: " + target + "\n";
	    out += "**************************************************************\n\n";

	    List<TermFrequencyItem> tfList = getItems(target);
	    for (TermFrequencyItem tf : tfList) {
		out += "- Term: " + tf.getTerm() + "\n";
		out += "- Freq: " + tf.getFreq() + "\n";
		out += "\n";
	    }
	}

	return out;
    }

    @Override
    public TermFrequencyMap fromStream(InputStream stream) throws JAXBException {
	Unmarshaller unmarshaller = createUnmarshaller();
	Object object = unmarshaller.unmarshal(stream);
	return new TermFrequencyMap((TermFrequencyMapType) object);
    }

    @Override
    public TermFrequencyMap fromNode(Node node) throws JAXBException {
	Unmarshaller unmarshaller = createUnmarshaller();
	Object object = unmarshaller.unmarshal(node);
	return new TermFrequencyMap((TermFrequencyMapType) object);
    }

    @Override
    public TermFrequencyMapType getElement() throws JAXBException {

	return this.type;
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

	return marshaller;
    }

}
