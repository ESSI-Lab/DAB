package eu.essi_lab.messages.termfrequency;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

@XmlRootElement(name = "termFrequency", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
@XmlAccessorType(XmlAccessType.FIELD)
public class TermFrequencyMapType {

    @XmlElementWrapper(name = ResourceProperty.SOURCE_ID_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<TermFrequencyItem> sourceId;

    @XmlElementWrapper(name = MetadataElement.KEYWORD_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<TermFrequencyItem> keyword;

    @XmlElementWrapper(name = MetadataElement.DISTRIBUTION_FORMAT_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<TermFrequencyItem> format;

    @XmlElementWrapper(name = MetadataElement.ONLINE_PROTOCOL_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> protocol;

    @XmlElementWrapper(name = MetadataElement.INSTRUMENT_IDENTIFIER_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> instrumentId;
    
    @XmlElementWrapper(name = MetadataElement.INSTRUMENT_TITLE_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> instrumentTitle;

    @XmlElementWrapper(name = MetadataElement.PLATFORM_IDENTIFIER_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> platformId;
    
    @XmlElementWrapper(name = MetadataElement.PLATFORM_TITLE_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> platformTitle;

    @XmlElementWrapper(name = MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> origOrgId;
    
    @XmlElementWrapper(name = MetadataElement.ORIGINATOR_ORGANISATION_DESCRIPTION_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> origOrgDesc;

    @XmlElementWrapper(name = MetadataElement.ATTRIBUTE_IDENTIFIER_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> attributeId;
    
    @XmlElementWrapper(name = MetadataElement.ATTRIBUTE_TITLE_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> attributeTitle;
    
    @XmlElementWrapper(name = MetadataElement.OBSERVED_PROPERTY_URI_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> observedPropertyURI;

    @XmlElementWrapper(name = MetadataElement.ORGANISATION_NAME_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> orgName;

    @XmlElementWrapper(name = MetadataElement.PRODUCT_TYPE_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> prodType;

    @XmlElementWrapper(name = MetadataElement.SENSOR_OP_MODE_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> sensorOpMode;

    @XmlElementWrapper(name = MetadataElement.SENSOR_SWATH_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> sensorSwath;

    @XmlElementWrapper(name = MetadataElement.SAR_POL_CH_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> sarPolCh;

    @XmlElementWrapper(name = MetadataElement.S3_INSTRUMENT_IDX_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> s3InstrumentIdx;

    @XmlElementWrapper(name = MetadataElement.S3_PRODUCT_LEVEL_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> s3ProductLevel;

    @XmlElementWrapper(name = MetadataElement.S3_TIMELINESS_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ArrayList<TermFrequencyItem> s3Timeliness;

    @XmlElementWrapper(name = ResourceProperty.SSC_SCORE_EL_NAME, namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "result", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    // this is required due to a possible bug of Jackson mapper
    // see JS_API_ResultSetFormatter for more info
    @JsonProperty(ResourceProperty.SSC_SCORE_EL_NAME)
    private List<TermFrequencyItem> sscScore;

    public TermFrequencyMapType() {
	sourceId = new ArrayList<TermFrequencyItem>();
	keyword = new ArrayList<TermFrequencyItem>();
	format = new ArrayList<TermFrequencyItem>();
	protocol = new ArrayList<TermFrequencyItem>();
	instrumentId = new ArrayList<TermFrequencyItem>();
	instrumentTitle= new ArrayList<TermFrequencyItem>();
	platformId = new ArrayList<TermFrequencyItem>();
	platformTitle = new ArrayList<TermFrequencyItem>();
	origOrgId = new ArrayList<TermFrequencyItem>();
	origOrgDesc= new ArrayList<TermFrequencyItem>();
	attributeId = new ArrayList<TermFrequencyItem>();
	attributeTitle = new ArrayList<TermFrequencyItem>();
	observedPropertyURI = new ArrayList<TermFrequencyItem>();
	orgName = new ArrayList<TermFrequencyItem>();
	sscScore = new ArrayList<TermFrequencyItem>();
	prodType = new ArrayList<TermFrequencyItem>();
	sensorOpMode = new ArrayList<TermFrequencyItem>();
	sensorSwath = new ArrayList<TermFrequencyItem>();
	sarPolCh = new ArrayList<TermFrequencyItem>();
	s3InstrumentIdx = new ArrayList<TermFrequencyItem>();
	s3ProductLevel = new ArrayList<TermFrequencyItem>();
	s3Timeliness = new ArrayList<TermFrequencyItem>();
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getS3InstrumentIdx() {
	return s3InstrumentIdx;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getS3ProductLevel() {
	return s3ProductLevel;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getS3Timeliness() {
	return s3Timeliness;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getProdType() {
	return prodType;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getSensorOpMode() {
	return sensorOpMode;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getSensorSwath() {
	return sensorSwath;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getSarPolCh() {
	return sarPolCh;
    }

    @XmlTransient
    public List<TermFrequencyItem> getOrganisationName() {

	return orgName;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getInstrumentId() {
	return instrumentId;
    }
    
    @XmlTransient
    public ArrayList<TermFrequencyItem> getInstrumentTitle() {
	return instrumentTitle;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getPlatformId() {
	return platformId;
    }
    
    @XmlTransient
    public ArrayList<TermFrequencyItem> getPlatformTitle() {
	return platformTitle;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getOrigOrgId() {
	return origOrgId;
    }
    
    @XmlTransient
    public ArrayList<TermFrequencyItem> getOrigOrgDescription() {
	return origOrgDesc;
    }

    @XmlTransient
    public ArrayList<TermFrequencyItem> getAttributeId() {
	return attributeId;
    }
    
    @XmlTransient
    public ArrayList<TermFrequencyItem> getAttributeTitle() {
	return attributeTitle;
    }
    
    @XmlTransient
    public ArrayList<TermFrequencyItem> getObservedPropertyURI() {
	return observedPropertyURI;
    }

    @XmlTransient
    public List<TermFrequencyItem> getSourceId() {
	return sourceId;
    }

    @XmlTransient
    public List<TermFrequencyItem> getKeyword() {
	return keyword;
    }

    @XmlTransient
    public List<TermFrequencyItem> getFormat() {
	return format;
    }

    @XmlTransient
    public List<TermFrequencyItem> getProtocol() {
	return protocol;
    }

    @XmlTransient
    public List<TermFrequencyItem> getSSCScore() {

	return sscScore;
    }
}
