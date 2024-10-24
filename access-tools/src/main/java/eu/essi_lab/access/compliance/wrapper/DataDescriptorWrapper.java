package eu.essi_lab.access.compliance.wrapper;

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
import java.text.ParseException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;

/**
 * @author Fabrizio
 */
@XmlRootElement(name = "DataDescriptor", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class DataDescriptorWrapper extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(DataDescriptorWrapper.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    @XmlElement(name = "rangeMin", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Number rangeMin;

    @XmlTransient
    public Number getRangeMin() {
	return rangeMin;
    }

    public void setRangeMin(Number rangeMin) {
	this.rangeMin = rangeMin;
    }

    @XmlTransient
    public Number getRangeMax() {
	return rangeMax;
    }

    public void setRangeMax(Number rangeMax) {
	this.rangeMax = rangeMax;
    }

    @XmlElement(name = "rangeMax", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Number rangeMax;

    @XmlElement(name = "dataType", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dataType;

    @XmlElement(name = "dataFormat", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String dataFormat;

    @XmlElement(name = "CRS", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String crs;

    @XmlElement(name = "continueDimension", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElementWrapper(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ContinueDimensionWrapper> continueSpatialDimensions;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ContinueDimensionWrapper continueTemporalDimension;

    @XmlElement(name = "continueDimension", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElementWrapper(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ContinueDimensionWrapper> continueOtherDimensions;

    @XmlElement(name = "finiteDimension", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElementWrapper(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<FiniteDimensionWrapper> finiteSpatialDimensions;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private FiniteDimensionWrapper finiteTemporalDimension;

    @XmlElement(name = "finiteDimension", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElementWrapper(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<FiniteDimensionWrapper> finiteOtherDimensions;

    /**
     * 
     */
    public DataDescriptorWrapper() {
    }

    /**
     * @param dataDescriptor
     */
    public DataDescriptorWrapper(DataDescriptor dataDescriptor) {

	if (dataDescriptor.getDataType() != null) {
	    setDataType(dataDescriptor.getDataType().toString());
	}

	if (dataDescriptor.getDataFormat() != null) {
	    setDataFormat(dataDescriptor.getDataFormat().getIdentifier());
	}

	if (dataDescriptor.getCRS() != null) {
	    setCRS(dataDescriptor.getCRS().getIdentifier());
	}

	if (dataDescriptor.getRangeMinimum() != null) {
	    setRangeMin(dataDescriptor.getRangeMinimum());
	}

	if (dataDescriptor.getRangeMaximum() != null) {
	    setRangeMax(dataDescriptor.getRangeMaximum());
	}

	List<DataDimension> spatialDimensions = dataDescriptor.getSpatialDimensions();
	List<DataDimension> otherDimensions = dataDescriptor.getOtherDimensions();
	DataDimension temporalDimension = dataDescriptor.getTemporalDimension();

	if (!spatialDimensions.isEmpty()) {

	    for (DataDimension dataDimension : spatialDimensions) {

		if (dataDimension instanceof FiniteDimension) {

		    FiniteDimension finiteDimension = dataDimension.getFiniteDimension();
		    FiniteDimensionWrapper finiteDimensionWrapper = new FiniteDimensionWrapper(finiteDimension);

		    getFiniteSpatialDimensions().add(finiteDimensionWrapper);

		} else {

		    ContinueDimension continueDimension = dataDimension.getContinueDimension();
		    ContinueDimensionWrapper continueDimensionWrapper = new ContinueDimensionWrapper(continueDimension);

		    getContinueSpatialDimensions().add(continueDimensionWrapper);
		}
	    }
	}

	if (!otherDimensions.isEmpty()) {

	    for (DataDimension dataDimension : otherDimensions) {

		if (dataDimension instanceof FiniteDimension) {

		    FiniteDimension finiteDimension = dataDimension.getFiniteDimension();
		    FiniteDimensionWrapper finiteDimensionWrapper = new FiniteDimensionWrapper(finiteDimension);

		    getFiniteOtherDimensions().add(finiteDimensionWrapper);

		} else {

		    ContinueDimension continueDimension = dataDimension.getContinueDimension();
		    ContinueDimensionWrapper continueDimensionWrapper = new ContinueDimensionWrapper(continueDimension);

		    getContinueOtherDimensions().add(continueDimensionWrapper);
		}
	    }
	}

	if (temporalDimension != null) {

	    if (temporalDimension instanceof FiniteDimension) {

		FiniteDimensionWrapper finiteDimensionWrapper = new FiniteDimensionWrapper(temporalDimension.getFiniteDimension());
		setFiniteTemporalDimension(finiteDimensionWrapper);

	    } else {

		ContinueDimensionWrapper continueDimensionWrapper = new ContinueDimensionWrapper(temporalDimension.getContinueDimension());
		setContinueTemporalDimension(continueDimensionWrapper);
	    }
	}

    }

    @XmlTransient
    public List<ContinueDimensionWrapper> getContinueSpatialDimensions() {

	if (continueSpatialDimensions == null) {

	    continueSpatialDimensions = Lists.newArrayList();
	}

	return continueSpatialDimensions;
    }

    @XmlTransient
    public ContinueDimensionWrapper getSizedTemporalDimension() {

	return continueTemporalDimension;
    }

    public void setContinueTemporalDimension(ContinueDimensionWrapper sizedTemporalDimension) {

	this.continueTemporalDimension = sizedTemporalDimension;
    }

    public void setFiniteTemporalDimension(FiniteDimensionWrapper discreteTemporalDimension) {

	this.finiteTemporalDimension = discreteTemporalDimension;
    }

    @XmlTransient
    public List<ContinueDimensionWrapper> getContinueOtherDimensions() {

	if (continueOtherDimensions == null) {

	    continueOtherDimensions = Lists.newArrayList();
	}

	return continueOtherDimensions;
    }

    @XmlTransient
    public List<FiniteDimensionWrapper> getFiniteSpatialDimensions() {

	if (finiteSpatialDimensions == null) {

	    finiteSpatialDimensions = Lists.newArrayList();
	}

	return finiteSpatialDimensions;
    }

    @XmlTransient
    public FiniteDimensionWrapper getDiscreteTemporalDimension() {

	return finiteTemporalDimension;
    }

    @XmlTransient
    public List<FiniteDimensionWrapper> getFiniteOtherDimensions() {

	if (finiteOtherDimensions == null) {

	    finiteOtherDimensions = Lists.newArrayList();
	}

	return finiteOtherDimensions;
    }

    @XmlTransient
    public String getDataType() {

	return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {

	this.dataType = dataType;
    }

    /**
     * @return the dataFormat
     */
    @XmlTransient
    public String getDataFormat() {

	return dataFormat;
    }

    /**
     * @param dataFormat the dataFormat to set
     */
    public void setDataFormat(String dataFormat) {

	this.dataFormat = dataFormat;
    }

    /**
     * @return the crs
     */
    @XmlTransient
    public String getCRS() {

	return crs;
    }

    /**
     * @param crs the crs to set
     */
    public void setCRS(String crs) {

	this.crs = crs;
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     * @throws ParseException
     */
    public static DataDescriptor wrap(InputStream stream) throws JAXBException, ParseException {

	DataDescriptorWrapper wrapper = new DataDescriptorWrapper().fromStream(stream);
	return wrap(wrapper);
    }

    /**
     * @param stream
     * @return
     * @throws ParseException
     * @throws JAXBException
     */
    public static DataDescriptor wrap(DataDescriptorWrapper wrapper) throws ParseException {

	DataDescriptor out = new DataDescriptor();

	if (wrapper.getRangeMin() != null) {
	    out.setRangeMinimum(wrapper.getRangeMin());
	}

	if (wrapper.getRangeMax() != null) {
	    out.setRangeMaximum(wrapper.getRangeMax());
	}

	if (wrapper.getCRS() != null) {
	    out.setCRS(CRS.fromIdentifier(wrapper.getCRS()));
	}

	if (wrapper.getDataFormat() != null) {
	    out.setDataFormat(DataFormat.fromIdentifier(wrapper.getDataFormat()));
	}

	if (wrapper.getDataType() != null) {
	    out.setDataType(DataType.valueOf(wrapper.getDataType()));
	}

	// -------------------------
	//
	// other dimensions
	//
	//
	{
	    List<DataDimension> dataDimensionList = Lists.newArrayList();

	    // discrete
	    List<FiniteDimensionWrapper> discreteOtherDimensions = wrapper.getFiniteOtherDimensions();
	    for (FiniteDimensionWrapper dim : discreteOtherDimensions) {

		DataDimension dataDimension = FiniteDimensionWrapper.wrap(dim);
		dataDimensionList.add(dataDimension);
	    }

	    // sized
	    List<ContinueDimensionWrapper> sizedOtherDimensions = wrapper.getContinueOtherDimensions();
	    for (ContinueDimensionWrapper dim : sizedOtherDimensions) {

		DataDimension dataDimension = ContinueDimensionWrapper.wrap(dim);
		dataDimensionList.add(dataDimension);
	    }

	    out.setOtherDimensions(dataDimensionList);
	}

	// -------------------------
	//
	// spatial dimensions
	//
	//
	{
	    List<DataDimension> dataDimensionList = Lists.newArrayList();

	    List<FiniteDimensionWrapper> finiteSpatialDimensions = wrapper.getFiniteSpatialDimensions();
	    for (FiniteDimensionWrapper dim : finiteSpatialDimensions) {

		DataDimension dataDimension = FiniteDimensionWrapper.wrap(dim);
		dataDimensionList.add(dataDimension);
	    }

	    List<ContinueDimensionWrapper> continueSpatialDimensions = wrapper.getContinueSpatialDimensions();
	    for (ContinueDimensionWrapper dim : continueSpatialDimensions) {

		DataDimension dataDimension = ContinueDimensionWrapper.wrap(dim);
		dataDimensionList.add(dataDimension);
	    }

	    out.setSpatialDimensions(dataDimensionList);
	}

	// -------------------------
	//
	// temporal dimensions
	//
	//
	ContinueDimensionWrapper sizedTemporalDimension = wrapper.getSizedTemporalDimension();
	if (sizedTemporalDimension != null) {

	    out.setTemporalDimension(ContinueDimensionWrapper.wrap(sizedTemporalDimension));
	}

	FiniteDimensionWrapper discreteTemporalDimension = wrapper.getDiscreteTemporalDimension();
	if (discreteTemporalDimension != null) {

	    out.setTemporalDimension(FiniteDimensionWrapper.wrap(discreteTemporalDimension));
	}

	return out;
    }

    @Override
    public DataDescriptorWrapper fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (DataDescriptorWrapper) unmarshaller.unmarshal(stream);
    }

    @Override
    public DataDescriptorWrapper fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (DataDescriptorWrapper) unmarshaller.unmarshal(node);
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
    protected Object getElement() throws JAXBException {

	return this;
    }
}
