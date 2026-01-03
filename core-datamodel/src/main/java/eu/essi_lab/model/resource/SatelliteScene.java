/**
 * 
 */
package eu.essi_lab.model.resource;

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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
@XmlRootElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class SatelliteScene extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(SatelliteScene.class);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSResource.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(GSResource.class).error(e.getMessage(), e);
	}
    }

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String origin;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String collectionQueryables;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sceneIdentifier;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String polarizationOrientationCode;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Integer row;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Integer path;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String thumbnailURL;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Integer relativeOrbit;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String productType;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String ingDate;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sensorOpMode;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String platid;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sensorSwath;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dusId;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String footprint;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String size;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String polChannel;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String cloudCoverPercentage;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sarPolCh;

    // ----------------------------------------------
    //
    // SAT EXTENSIONS
    //
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String processingLevel;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String fileStorePath;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String fileSpecification;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dataOwner;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dayNightFlag;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String imageGSD;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sourceID;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String productFormat;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dataURL;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dataProvider;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String bands;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String acquisitionType;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String missionDatatakeid;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String startOrbitNumber;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String stopOrbitNumber;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String orbitDirection;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String productClass;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String productConsolidation;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String stopRelativeOrbitNumber;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sliceNumber;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String status;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String processingBaseline;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String s3ProductLevel;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String s3Timeliness;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String s3InstrumentIdx;

    /**
     * 
     */
    public SatelliteScene() {
	setOrigin("none");
    }

    /**
     * @return
     */
    @XmlTransient
    public String getOrigin() {
	return origin;
    }

    /**
     * @param sceneOrigin
     */
    public void setOrigin(String sceneOrigin) {
	this.origin = sceneOrigin;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getCloudCoverPercentage() {
	return cloudCoverPercentage;
    }

    /**
     * @param cloudCoverPercentage
     */
    public void setCloudCoverPercentage(String cloudCoverPercentage) {
	this.cloudCoverPercentage = cloudCoverPercentage;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSarPolCh() {

	return sarPolCh;
    }

    /**
     * @param sarPolCh
     */
    public void setSarPolCh(String sarPolCh) {

	this.sarPolCh = sarPolCh;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getCollectionQueryables() {
	return collectionQueryables;
    }

    /**
     * @param collectionQueryables
     */
    public void setCollectionQueryables(String collectionQueryables) {
	this.collectionQueryables = collectionQueryables;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSceneIdentifier() {
	return sceneIdentifier;
    }

    /**
     * @param sceneIdentifier
     */
    public void setSceneIdentifier(String sceneIdentifier) {
	this.sceneIdentifier = sceneIdentifier;
    }

    /**
     * @return
     */
    @XmlTransient
    public Integer getRow() {
	return row;
    }

    /**
     * @param row
     */
    public void setRow(Integer row) {
	this.row = row;
    }

    /**
     * @return
     */
    @XmlTransient
    public Integer getPath() {
	return path;
    }

    /**
     * @param path
     */
    public void setPath(Integer path) {
	this.path = path;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getThumbnailURL() {
	return thumbnailURL;
    }

    /**
     * @param thumbnailURL
     */
    public void setThumbnailURL(String thumbnailURL) {
	this.thumbnailURL = thumbnailURL;
    }

    /**
     * @return
     */
    @XmlTransient
    public Integer getRelativeOrbit() {
	return relativeOrbit;
    }

    /**
     * @param relativeOrbit
     */
    public void setRelativeOrbit(Integer relativeOrbit) {
	this.relativeOrbit = relativeOrbit;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getProductType() {
	return productType;
    }

    /**
     * @param prodType
     */
    public void setProductType(String prodType) {
	this.productType = prodType;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getIngDate() {
	return ingDate;
    }

    /**
     * @param ingDate
     */
    public void setIngDate(String ingDate) {
	this.ingDate = ingDate;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSensorOpMode() {
	return sensorOpMode;
    }

    /**
     * @param sensorOpMode
     */
    public void setSensorOpMode(String sensorOpMode) {
	this.sensorOpMode = sensorOpMode;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getPlatid() {
	return platid;
    }

    /**
     * @param platid
     */
    public void setPlatid(String platid) {
	this.platid = platid;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSensorSwath() {
	return sensorSwath;
    }

    /**
     * @param sensorSwath
     */
    public void setSensorSwath(String sensorSwath) {
	this.sensorSwath = sensorSwath;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getDusId() {
	return dusId;
    }

    /**
     * @param dusId
     */
    public void setDusId(String dusId) {
	this.dusId = dusId;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getFootprint() {
	return footprint;
    }

    /**
     * @param footprint
     */
    public void setFootprint(String footprint) {
	this.footprint = footprint;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSize() {
	return size;
    }

    /**
     * @param size
     */
    public void setSize(String size) {
	this.size = size;
    }

    /**
     * @return
     */
    @XmlTransient
    public List<String> getPolChannels() {

	if (this.polChannel == null) {

	    return Lists.newArrayList();
	}

	return Arrays.asList(polChannel.split(","));
    }

    /**
     * @param polChannel
     */
    public void addPolChannel(String polChannel) {

	if (this.polChannel == null) {

	    this.polChannel = polChannel;

	} else {

	    this.polChannel = this.polChannel + "," + polChannel;
	}
    }

    /**
     * @return
     */
    @XmlTransient
    public String getProcessingLevel() {
	return processingLevel;
    }

    /**
     * @param processingLevel
     */
    public void setProcessingLevel(String processingLevel) {
	this.processingLevel = processingLevel;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getFileStorePath() {
	return fileStorePath;
    }

    /**
     * @param fileStorePath
     */
    public void setFileStorePath(String fileStorePath) {
	this.fileStorePath = fileStorePath;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getFileSpecification() {
	return fileSpecification;
    }

    /**
     * @param fileSpecification
     */
    public void setFileSpecification(String fileSpecification) {
	this.fileSpecification = fileSpecification;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getDataOwner() {
	return dataOwner;
    }

    /**
     * @param dataOwner
     */
    public void setDataOwner(String dataOwner) {
	this.dataOwner = dataOwner;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getDayNightFlag() {
	return dayNightFlag;
    }

    /**
     * @param dayNightFlag
     */
    public void setDayNightFlag(String dayNightFlag) {
	this.dayNightFlag = dayNightFlag;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getImageGSD() {
	return imageGSD;
    }

    /**
     * @param imageGSD
     */
    public void setImageGSD(String imageGSD) {
	this.imageGSD = imageGSD;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSourceID() {
	return sourceID;
    }

    /**
     * @param sourceID
     */
    public void setSourceID(String sourceID) {
	this.sourceID = sourceID;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getProductFormat() {
	return productFormat;
    }

    /**
     * @param productFormat
     */
    public void setProductFormat(String productFormat) {
	this.productFormat = productFormat;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getDataURL() {
	return dataURL;
    }

    /**
     * @param dataURL
     */
    public void setDataURL(String dataURL) {
	this.dataURL = dataURL;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getDataProvider() {
	return dataProvider;
    }

    /**
     * @param dataProvider
     */
    public void setDataProvider(String dataProvider) {
	this.dataProvider = dataProvider;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getBands() {
	return bands;
    }

    /**
     * @param bands
     */
    public void setBands(String bands) {
	this.bands = bands;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getAcquisitionType() {
	return acquisitionType;
    }

    /**
     * @param acquisitionType
     */
    public void setAcquisitionType(String acquisitionType) {
	this.acquisitionType = acquisitionType;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getMissionDatatakeid() {
	return missionDatatakeid;
    }

    /**
     * @param missionDatatakeid
     */
    public void setMissionDatatakeid(String missionDatatakeid) {
	this.missionDatatakeid = missionDatatakeid;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getStartOrbitNumber() {
	return startOrbitNumber;
    }

    /**
     * @param startOrbitNumber
     */
    public void setStartOrbitNumber(String startOrbitNumber) {
	this.startOrbitNumber = startOrbitNumber;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getStopOrbitNumber() {
	return stopOrbitNumber;
    }

    /**
     * @param stopOrbitNumber
     */
    public void setStopOrbitNumber(String stopOrbitNumber) {
	this.stopOrbitNumber = stopOrbitNumber;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getOrbitDirection() {
	return orbitDirection;
    }

    /**
     * @param orbitDirection
     */
    public void setOrbitDirection(String orbitDirection) {
	this.orbitDirection = orbitDirection;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getProductClass() {
	return productClass;
    }

    /**
     * @param productClass
     */
    public void setProductClass(String productClass) {
	this.productClass = productClass;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getProductConsolidation() {
	return productConsolidation;
    }

    /**
     * @param productConsolidation
     */
    public void setProductConsolidation(String productConsolidation) {
	this.productConsolidation = productConsolidation;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getStopRelativeOrbitNumber() {
	return stopRelativeOrbitNumber;
    }

    /**
     * @param stopRelativeOrbitNumber
     */
    public void setStopRelativeOrbitNumber(String stopRelativeOrbitNumber) {
	this.stopRelativeOrbitNumber = stopRelativeOrbitNumber;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getSliceNumber() {
	return sliceNumber;
    }

    /**
     * @param sliceNumber
     */
    public void setSliceNumber(String sliceNumber) {
	this.sliceNumber = sliceNumber;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getStatus() {
	return status;
    }

    /**
     * @param status
     */
    public void setStatus(String status) {
	this.status = status;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getProcessingBaseline() {
	return processingBaseline;
    }

    /**
     * @param processingBaseline
     */
    public void setProcessingBaseline(String processingBaseline) {
	this.processingBaseline = processingBaseline;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getS3ProductLevel() {
	return s3ProductLevel;
    }

    /**
     * @param s3ProductLevel
     */
    public void setS3ProductLevel(String s3ProductLevel) {
	this.s3ProductLevel = s3ProductLevel;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getS3Timeliness() {
	return s3Timeliness;
    }

    /**
     * @param s3Timeliness
     */
    public void setS3Timeliness(String s3Timeliness) {
	this.s3Timeliness = s3Timeliness;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getS3InstrumentIdx() {
	return s3InstrumentIdx;
    }

    /**
     * @param s3InstrumentIdx
     */
    public void setS3InstrumentIdx(String s3InstrumentIdx) {
	this.s3InstrumentIdx = s3InstrumentIdx;
    }

   
    @Override
    public SatelliteScene fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (SatelliteScene) unmarshaller.unmarshal(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static SatelliteScene create(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (SatelliteScene) unmarshaller.unmarshal(node);
    }

    @Override
    public SatelliteScene fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (SatelliteScene) unmarshaller.unmarshal(node);
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

    @Override
    protected Object getElement() throws JAXBException {

	return this;
    }

    @Override
    public String toString() {

	try {
	    return asString(true);
	} catch (Exception e) {
	}

	return "error occurred";
    }
}
