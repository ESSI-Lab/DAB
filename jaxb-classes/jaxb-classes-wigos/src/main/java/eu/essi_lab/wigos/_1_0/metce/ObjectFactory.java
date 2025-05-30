//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.11.27 at 01:47:52 PM CET 
//


package eu.essi_lab.wigos._1_0.metce;

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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import eu.essi_lab.wigos._1_0.gml._3_2_1.PointPropertyType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.essi_lab.wigos._1_0.metce package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Process_QNAME = new QName("http://def.wmo.int/metce/2013", "Process");
    private final static QName _MeasurementContext_QNAME = new QName("http://def.wmo.int/metce/2013", "MeasurementContext");
    private final static QName _RangeBounds_QNAME = new QName("http://def.wmo.int/metce/2013", "RangeBounds");
    private final static QName _TropicalCyclone_QNAME = new QName("http://def.wmo.int/metce/2013", "TropicalCyclone");
    private final static QName _Volcano_QNAME = new QName("http://def.wmo.int/metce/2013", "Volcano");
    private final static QName _EruptingVolcano_QNAME = new QName("http://def.wmo.int/metce/2013", "EruptingVolcano");
    private final static QName _VolcanoTypeName_QNAME = new QName("http://def.wmo.int/metce/2013", "name");
    private final static QName _VolcanoTypePosition_QNAME = new QName("http://def.wmo.int/metce/2013", "position");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.essi_lab.wigos._1_0.metce
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProcessType }
     * 
     */
    public ProcessType createProcessType() {
        return new ProcessType();
    }

    /**
     * Create an instance of {@link MeasurementContextType }
     * 
     */
    public MeasurementContextType createMeasurementContextType() {
        return new MeasurementContextType();
    }

    /**
     * Create an instance of {@link RangeBoundsType }
     * 
     */
    public RangeBoundsType createRangeBoundsType() {
        return new RangeBoundsType();
    }

    /**
     * Create an instance of {@link TropicalCycloneType }
     * 
     */
    public TropicalCycloneType createTropicalCycloneType() {
        return new TropicalCycloneType();
    }

    /**
     * Create an instance of {@link VolcanoType }
     * 
     */
    public VolcanoType createVolcanoType() {
        return new VolcanoType();
    }

    /**
     * Create an instance of {@link EruptingVolcanoType }
     * 
     */
    public EruptingVolcanoType createEruptingVolcanoType() {
        return new EruptingVolcanoType();
    }

    /**
     * Create an instance of {@link ProcessPropertyType }
     * 
     */
    public ProcessPropertyType createProcessPropertyType() {
        return new ProcessPropertyType();
    }

    /**
     * Create an instance of {@link MeasurementContextPropertyType }
     * 
     */
    public MeasurementContextPropertyType createMeasurementContextPropertyType() {
        return new MeasurementContextPropertyType();
    }

    /**
     * Create an instance of {@link RangeBoundsPropertyType }
     * 
     */
    public RangeBoundsPropertyType createRangeBoundsPropertyType() {
        return new RangeBoundsPropertyType();
    }

    /**
     * Create an instance of {@link TropicalCyclonePropertyType }
     * 
     */
    public TropicalCyclonePropertyType createTropicalCyclonePropertyType() {
        return new TropicalCyclonePropertyType();
    }

    /**
     * Create an instance of {@link VolcanoPropertyType }
     * 
     */
    public VolcanoPropertyType createVolcanoPropertyType() {
        return new VolcanoPropertyType();
    }

    /**
     * Create an instance of {@link EruptingVolcanoPropertyType }
     * 
     */
    public EruptingVolcanoPropertyType createEruptingVolcanoPropertyType() {
        return new EruptingVolcanoPropertyType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ProcessType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "Process", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractFeature")
    public JAXBElement<ProcessType> createProcess(ProcessType value) {
        return new JAXBElement<ProcessType>(_Process_QNAME, ProcessType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MeasurementContextType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link MeasurementContextType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "MeasurementContext", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGML")
    public JAXBElement<MeasurementContextType> createMeasurementContext(MeasurementContextType value) {
        return new JAXBElement<MeasurementContextType>(_MeasurementContext_QNAME, MeasurementContextType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RangeBoundsType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RangeBoundsType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "RangeBounds", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractObject")
    public JAXBElement<RangeBoundsType> createRangeBounds(RangeBoundsType value) {
        return new JAXBElement<RangeBoundsType>(_RangeBounds_QNAME, RangeBoundsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TropicalCycloneType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link TropicalCycloneType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "TropicalCyclone", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractFeature")
    public JAXBElement<TropicalCycloneType> createTropicalCyclone(TropicalCycloneType value) {
        return new JAXBElement<TropicalCycloneType>(_TropicalCyclone_QNAME, TropicalCycloneType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VolcanoType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link VolcanoType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "Volcano", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractFeature")
    public JAXBElement<VolcanoType> createVolcano(VolcanoType value) {
        return new JAXBElement<VolcanoType>(_Volcano_QNAME, VolcanoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EruptingVolcanoType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EruptingVolcanoType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "EruptingVolcano", substitutionHeadNamespace = "http://def.wmo.int/metce/2013", substitutionHeadName = "Volcano")
    public JAXBElement<EruptingVolcanoType> createEruptingVolcano(EruptingVolcanoType value) {
        return new JAXBElement<EruptingVolcanoType>(_EruptingVolcano_QNAME, EruptingVolcanoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "name", scope = VolcanoType.class)
    public JAXBElement<String> createVolcanoTypeName(String value) {
        return new JAXBElement<String>(_VolcanoTypeName_QNAME, String.class, VolcanoType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PointPropertyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PointPropertyType }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "position", scope = VolcanoType.class)
    public JAXBElement<PointPropertyType> createVolcanoTypePosition(PointPropertyType value) {
        return new JAXBElement<PointPropertyType>(_VolcanoTypePosition_QNAME, PointPropertyType.class, VolcanoType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://def.wmo.int/metce/2013", name = "name", scope = TropicalCycloneType.class)
    public JAXBElement<String> createTropicalCycloneTypeName(String value) {
        return new JAXBElement<String>(_VolcanoTypeName_QNAME, String.class, TropicalCycloneType.class, value);
    }

}
