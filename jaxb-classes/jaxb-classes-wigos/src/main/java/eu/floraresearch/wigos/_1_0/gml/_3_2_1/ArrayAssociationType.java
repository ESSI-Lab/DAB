//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.09.12 at 03:34:17 PM CEST 
//


package eu.floraresearch.wigos._1_0.gml._3_2_1;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import eu.floraresearch.wigos._1_0.main.AbstractEnvironmentalMonitoringFacilityType;
import eu.floraresearch.wigos._1_0.main.AttributionType;
import eu.floraresearch.wigos._1_0.main.ClimateZoneType;
import eu.floraresearch.wigos._1_0.main.ControlCheckReportType;
import eu.floraresearch.wigos._1_0.main.DataGenerationType;
import eu.floraresearch.wigos._1_0.main.DataPolicyType;
import eu.floraresearch.wigos._1_0.main.DeploymentType;
import eu.floraresearch.wigos._1_0.main.DescriptionType;
import eu.floraresearch.wigos._1_0.main.EquipmentLogType;
import eu.floraresearch.wigos._1_0.main.EquipmentType;
import eu.floraresearch.wigos._1_0.main.EventReportType;
import eu.floraresearch.wigos._1_0.main.FacilityLogType;
import eu.floraresearch.wigos._1_0.main.FacilitySetType;
import eu.floraresearch.wigos._1_0.main.FrequenciesType;
import eu.floraresearch.wigos._1_0.main.GeospatialLocationType;
import eu.floraresearch.wigos._1_0.main.HeaderType;
import eu.floraresearch.wigos._1_0.main.InstrumentOperatingStatusType;
import eu.floraresearch.wigos._1_0.main.LogEntryType;
import eu.floraresearch.wigos._1_0.main.LogType;
import eu.floraresearch.wigos._1_0.main.MaintenanceReportType;
import eu.floraresearch.wigos._1_0.main.ObservingCapabilityType;
import eu.floraresearch.wigos._1_0.main.ObservingFacilityType;
import eu.floraresearch.wigos._1_0.main.PopulationType;
import eu.floraresearch.wigos._1_0.main.ProcessingType;
import eu.floraresearch.wigos._1_0.main.ProgramAffiliationType;
import eu.floraresearch.wigos._1_0.main.ReportingStatusType;
import eu.floraresearch.wigos._1_0.main.ReportingType;
import eu.floraresearch.wigos._1_0.main.ResponsiblePartyType;
import eu.floraresearch.wigos._1_0.main.ResultSetType;
import eu.floraresearch.wigos._1_0.main.SamplingType;
import eu.floraresearch.wigos._1_0.main.ScheduleType;
import eu.floraresearch.wigos._1_0.main.SurfaceCoverType;
import eu.floraresearch.wigos._1_0.main.SurfaceRoughnessType;
import eu.floraresearch.wigos._1_0.main.TerritoryType;
import eu.floraresearch.wigos._1_0.main.TimeZoneType;
import eu.floraresearch.wigos._1_0.main.TopographyBathymetryType;
import eu.floraresearch.wigos._1_0.main.WIGOSMetadataRecordType;
import eu.floraresearch.wigos._1_0.metce.EruptingVolcanoType;
import eu.floraresearch.wigos._1_0.metce.MeasurementContextType;
import eu.floraresearch.wigos._1_0.metce.TropicalCycloneType;
import eu.floraresearch.wigos._1_0.metce.VolcanoType;
import eu.floraresearch.wigos._1_0.om.OMObservationType;
import eu.floraresearch.wigos._1_0.opm.AbstractObservablePropertyType;
import eu.floraresearch.wigos._1_0.opm.CategoryConstraintType;
import eu.floraresearch.wigos._1_0.opm.CompositeObservablePropertyType;
import eu.floraresearch.wigos._1_0.opm.ConstraintType;
import eu.floraresearch.wigos._1_0.opm.ObservablePropertyType;
import eu.floraresearch.wigos._1_0.opm.QualifiedObservablePropertyType;
import eu.floraresearch.wigos._1_0.opm.RangeConstraintType;
import eu.floraresearch.wigos._1_0.opm.ScalarConstraintType;
import eu.floraresearch.wigos._1_0.opm.StatisticalQualifierType;
import eu.floraresearch.wigos._1_0.sampling.SFSamplingFeatureCollectionType;
import eu.floraresearch.wigos._1_0.sampling.SFSamplingFeatureType;
import eu.floraresearch.wigos._1_0.ssf.SFSpatialSamplingFeatureType;


/**
 * <p>Java class for ArrayAssociationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayAssociationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}AbstractObject" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}OwnershipAttributeGroup"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayAssociationType", propOrder = {
    "abstractObject"
})
public class ArrayAssociationType {

    @XmlElementRef(name = "AbstractObject", namespace = "http://www.opengis.net/gml/3.2", type = JAXBElement.class, required = false)
    protected List<JAXBElement<?>> abstractObject;
    @XmlAttribute(name = "owns")
    protected java.lang.Boolean owns;

    /**
     * Gets the value of the abstractObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PopulationType }{@code >}
     * {@link JAXBElement }{@code <}{@link CoverageFunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link DataBlockType }{@code >}
     * {@link JAXBElement }{@code <}{@link AttributionType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReportingStatusType }{@code >}
     * {@link JAXBElement }{@code <}{@link SurfaceCoverType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReportingType }{@code >}
     * {@link JAXBElement }{@code <}{@link InstrumentOperatingStatusType }{@code >}
     * {@link JAXBElement }{@code <}{@link ProcessingType }{@code >}
     * {@link JAXBElement }{@code <}{@link CategoryExtentType }{@code >}
     * {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}
     * {@link JAXBElement }{@code <}{@link ValueArrayType }{@code >}
     * {@link JAXBElement }{@code <}{@link CompositeValueType }{@code >}
     * {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}
     * {@link JAXBElement }{@code <}{@link MeasureOrNilReasonListType }{@code >}
     * {@link JAXBElement }{@code <}{@link CodeOrNilReasonListType }{@code >}
     * {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link QuantityExtentType }{@code >}
     * {@link JAXBElement }{@code <}{@link Count }{@code >}
     * {@link JAXBElement }{@code <}{@link eu.floraresearch.wigos._1_0.gml._3_2_1.Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Category }{@code >}
     * {@link JAXBElement }{@code <}{@link Quantity }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link JAXBElement }{@code <}{@link GridFunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link GenericMetaDataType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractMetaDataType }{@code >}
     * {@link JAXBElement }{@code <}{@link MappingRuleType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeZoneType }{@code >}
     * {@link JAXBElement }{@code <}{@link ParameterValueGroupType }{@code >}
     * {@link JAXBElement }{@code <}{@link ParameterValueType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeneralParameterValueType }{@code >}
     * {@link JAXBElement }{@code <}{@link ArcByBulgeType }{@code >}
     * {@link JAXBElement }{@code <}{@link ArcStringByBulgeType }{@code >}
     * {@link JAXBElement }{@code <}{@link OffsetCurveType }{@code >}
     * {@link JAXBElement }{@code <}{@link ClothoidType }{@code >}
     * {@link JAXBElement }{@code <}{@link CircleType }{@code >}
     * {@link JAXBElement }{@code <}{@link ArcType }{@code >}
     * {@link JAXBElement }{@code <}{@link ArcStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link CubicSplineType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStringSegmentType }{@code >}
     * {@link JAXBElement }{@code <}{@link BezierType }{@code >}
     * {@link JAXBElement }{@code <}{@link BSplineType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeodesicType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeodesicStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link CircleByCenterPointType }{@code >}
     * {@link JAXBElement }{@code <}{@link ArcByCenterPointType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCurveSegmentType }{@code >}
     * {@link JAXBElement }{@code <}{@link StatisticalQualifierType }{@code >}
     * {@link JAXBElement }{@code <}{@link ResultSetType }{@code >}
     * {@link JAXBElement }{@code <}{@link eu.floraresearch.wigos._1_0.metce.RangeBoundsType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScheduleType }{@code >}
     * {@link JAXBElement }{@code <}{@link eu.floraresearch.wigos._1_0.opm.RangeBoundsType }{@code >}
     * {@link JAXBElement }{@code <}{@link ProgramAffiliationType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeometricComplexType }{@code >}
     * {@link JAXBElement }{@code <}{@link CompositeCurveType }{@code >}
     * {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     * {@link JAXBElement }{@code <}{@link RingType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractRingType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link CurveType }{@code >}
     * {@link JAXBElement }{@code <}{@link OrientableCurveType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCurveType }{@code >}
     * {@link JAXBElement }{@code <}{@link CompositeSolidType }{@code >}
     * {@link JAXBElement }{@code <}{@link SolidType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractSolidType }{@code >}
     * {@link JAXBElement }{@code <}{@link PointType }{@code >}
     * {@link JAXBElement }{@code <}{@link TinType }{@code >}
     * {@link JAXBElement }{@code <}{@link SurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link SurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link SurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link OrientableSurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     * {@link JAXBElement }{@code <}{@link CompositeSurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link ShellType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractSurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeometricPrimitiveType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiSolidType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiCurveType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiPointType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiSurfaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeometricAggregateType }{@code >}
     * {@link JAXBElement }{@code <}{@link RectifiedGridType }{@code >}
     * {@link JAXBElement }{@code <}{@link GridType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeTopologyComplexType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimeComplexType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeInstantType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimePeriodType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimeGeometricPrimitiveType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeNodeType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeEdgeType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimeTopologyPrimitiveType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimeObjectType }{@code >}
     * {@link JAXBElement }{@code <}{@link MeasurementContextType }{@code >}
     * {@link JAXBElement }{@code <}{@link TopoComplexType }{@code >}
     * {@link JAXBElement }{@code <}{@link TopoSolidType }{@code >}
     * {@link JAXBElement }{@code <}{@link EdgeType }{@code >}
     * {@link JAXBElement }{@code <}{@link FaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link NodeType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTopoPrimitiveType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTopologyType }{@code >}
     * {@link JAXBElement }{@code <}{@link BagType }{@code >}
     * {@link JAXBElement }{@code <}{@link SFSamplingFeatureCollectionType }{@code >}
     * {@link JAXBElement }{@code <}{@link FrequenciesType }{@code >}
     * {@link JAXBElement }{@code <}{@link DynamicFeatureCollectionType }{@code >}
     * {@link JAXBElement }{@code <}{@link DynamicFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link WIGOSMetadataRecordType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObservingFacilityType }{@code >}
     * {@link JAXBElement }{@code <}{@link EquipmentType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractEnvironmentalMonitoringFacilityType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractFeatureCollectionType }{@code >}
     * {@link JAXBElement }{@code <}{@link EquipmentLogType }{@code >}
     * {@link JAXBElement }{@code <}{@link FacilityLogType }{@code >}
     * {@link JAXBElement }{@code <}{@link LogType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractContinuousCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link SFSpatialSamplingFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link SFSamplingFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link EruptingVolcanoType }{@code >}
     * {@link JAXBElement }{@code <}{@link VolcanoType }{@code >}
     * {@link JAXBElement }{@code <}{@link FacilitySetType }{@code >}
     * {@link JAXBElement }{@code <}{@link FeatureCollectionType }{@code >}
     * {@link JAXBElement }{@code <}{@link DeploymentType }{@code >}
     * {@link JAXBElement }{@code <}{@link TropicalCycloneType }{@code >}
     * {@link JAXBElement }{@code <}{@link OMObservationType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DiscreteCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObservingCapabilityType }{@code >}
     * {@link JAXBElement }{@code <}{@link eu.floraresearch.wigos._1_0.main.ProcessType }{@code >}
     * {@link JAXBElement }{@code <}{@link eu.floraresearch.wigos._1_0.metce.ProcessType }{@code >}
     * {@link JAXBElement }{@code <}{@link DirectedObservationAtDistanceType }{@code >}
     * {@link JAXBElement }{@code <}{@link DirectedObservationType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObservationType }{@code >}
     * {@link JAXBElement }{@code <}{@link DataGenerationType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link MovingObjectStatusType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTimeSliceType }{@code >}
     * {@link JAXBElement }{@code <}{@link ArrayType }{@code >}
     * {@link JAXBElement }{@code <}{@link OperationMethodType }{@code >}
     * {@link JAXBElement }{@code <}{@link CoordinateSystemAxisType }{@code >}
     * {@link JAXBElement }{@code <}{@link TemporalDatumType }{@code >}
     * {@link JAXBElement }{@code <}{@link VerticalDatumType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeodeticDatumType }{@code >}
     * {@link JAXBElement }{@code <}{@link EngineeringDatumType }{@code >}
     * {@link JAXBElement }{@code <}{@link ImageDatumType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractDatumType }{@code >}
     * {@link JAXBElement }{@code <}{@link CompoundCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeodeticCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeographicCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link EngineeringCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link TemporalCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link ProjectedCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link DerivedCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeneralDerivedCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link VerticalCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link ImageCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeocentricCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCRSType }{@code >}
     * {@link JAXBElement }{@code <}{@link PrimeMeridianType }{@code >}
     * {@link JAXBElement }{@code <}{@link DictionaryType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObliqueCartesianCSType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeCSType }{@code >}
     * {@link JAXBElement }{@code <}{@link CartesianCSType }{@code >}
     * {@link JAXBElement }{@code <}{@link VerticalCSType }{@code >}
     * {@link UserDefinedCS1 }
     * {@link JAXBElement }{@code <}{@link AffineCSType }{@code >}
     * {@link CylindricalCS1 }
     * {@link LinearCS1 }
     * {@link PolarCS1 }
     * {@link JAXBElement }{@code <}{@link TemporalCSType }{@code >}
     * {@link JAXBElement }{@code <}{@link SphericalCSType }{@code >}
     * {@link JAXBElement }{@code <}{@link EllipsoidalCSType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCoordinateSystemType }{@code >}
     * {@link JAXBElement }{@code <}{@link DefinitionProxyType }{@code >}
     * {@link JAXBElement }{@code <}{@link PassThroughOperationType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConversionType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeneralConversionType }{@code >}
     * {@link JAXBElement }{@code <}{@link TransformationType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeneralTransformationType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCoordinateOperationType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCoordinateOperationType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConcatenatedOperationType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractCoordinateOperationType }{@code >}
     * {@link JAXBElement }{@code <}{@link DictionaryType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeOrdinalReferenceSystemType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeClockType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeCalendarType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeCoordinateSystemType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeReferenceSystemType }{@code >}
     * {@link JAXBElement }{@code <}{@link DerivedUnitType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConventionalUnitType }{@code >}
     * {@link JAXBElement }{@code <}{@link BaseUnitType }{@code >}
     * {@link JAXBElement }{@code <}{@link UnitDefinitionType }{@code >}
     * {@link JAXBElement }{@code <}{@link EllipsoidType }{@code >}
     * {@link JAXBElement }{@code <}{@link OperationParameterGroupType }{@code >}
     * {@link JAXBElement }{@code <}{@link OperationParameterType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGeneralOperationParameterType }{@code >}
     * {@link JAXBElement }{@code <}{@link DefinitionType }{@code >}
     * {@link JAXBElement }{@code <}{@link QualifiedObservablePropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObservablePropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link CompositeObservablePropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractObservablePropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractGMLType }{@code >}
     * {@link JAXBElement }{@code <}{@link TopographyBathymetryType }{@code >}
     * {@link JAXBElement }{@code <}{@link DataPolicyType }{@code >}
     * {@link JAXBElement }{@code <}{@link AffinePlacementType }{@code >}
     * {@link JAXBElement }{@code <}{@link ResponsiblePartyType }{@code >}
     * {@link JAXBElement }{@code <}{@link SurfaceRoughnessType }{@code >}
     * {@link JAXBElement }{@code <}{@link ClimateZoneType }{@code >}
     * {@link JAXBElement }{@code <}{@link EnvelopeWithTimePeriodType }{@code >}
     * {@link JAXBElement }{@code <}{@link EnvelopeType }{@code >}
     * {@link JAXBElement }{@code <}{@link HeaderType }{@code >}
     * {@link JAXBElement }{@code <}{@link DescriptionType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeospatialLocationType }{@code >}
     * {@link JAXBElement }{@code <}{@link SamplingType }{@code >}
     * {@link JAXBElement }{@code <}{@link EventReportType }{@code >}
     * {@link JAXBElement }{@code <}{@link MaintenanceReportType }{@code >}
     * {@link JAXBElement }{@code <}{@link ControlCheckReportType }{@code >}
     * {@link JAXBElement }{@code <}{@link LogEntryType }{@code >}
     * {@link JAXBElement }{@code <}{@link TerritoryType }{@code >}
     * {@link JAXBElement }{@code <}{@link FileType }{@code >}
     * {@link JAXBElement }{@code <}{@link RangeConstraintType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScalarConstraintType }{@code >}
     * {@link JAXBElement }{@code <}{@link CategoryConstraintType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConstraintType }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getAbstractObject() {
        if (abstractObject == null) {
            abstractObject = new ArrayList<JAXBElement<?>>();
        }
        return this.abstractObject;
    }

    /**
     * Gets the value of the owns property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.Boolean }
     *     
     */
    public boolean isOwns() {
        if (owns == null) {
            return false;
        } else {
            return owns;
        }
    }

    /**
     * Sets the value of the owns property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.Boolean }
     *     
     */
    public void setOwns(java.lang.Boolean value) {
        this.owns = value;
    }

}