package eu.essi_lab.jaxb.common;

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

/**
 * @author Fabrizio
 */
public class ObjectFactories {

    private static net.opengis.wfs.v_1_1_0.ObjectFactory wfs = new net.opengis.wfs.v_1_1_0.ObjectFactory();
    private static net.opengis.iso19139.gco.v_20060504.ObjectFactory gco = new net.opengis.iso19139.gco.v_20060504.ObjectFactory();
    private static net.opengis.iso19139.gmd.v_20060504.ObjectFactory gmd = new net.opengis.iso19139.gmd.v_20060504.ObjectFactory();
    private static net.opengis.iso19139.gmx.v_20060504.ObjectFactory gmx = new net.opengis.iso19139.gmx.v_20060504.ObjectFactory();
    private static net.opengis.iso19139.gsr.v_20060504.ObjectFactory gsr = new net.opengis.iso19139.gsr.v_20060504.ObjectFactory();
    private static net.opengis.iso19139.gss.v_20060504.ObjectFactory gss = new net.opengis.iso19139.gss.v_20060504.ObjectFactory();
    private static net.opengis.iso19139.gts.v_20060504.ObjectFactory gts = new net.opengis.iso19139.gts.v_20060504.ObjectFactory();
    private static net.opengis.iso19139.srv.v_20060504.ObjectFactory srv = new net.opengis.iso19139.srv.v_20060504.ObjectFactory();
    private static eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.ObjectFactory gmi = new eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.ObjectFactory();
    private static net.opengis.gml.v_3_2_0.ObjectFactory gml = new net.opengis.gml.v_3_2_0.ObjectFactory();

    private static eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.ObjectFactory dce = new eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.ObjectFactory();
    private static eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.terms.ObjectFactory dct = new eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.terms.ObjectFactory();
    private static eu.essi_lab.jaxb.ows._1_0_0.ObjectFactory ows = new eu.essi_lab.jaxb.ows._1_0_0.ObjectFactory();
    private static eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory csw = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();

    private static eu.essi_lab.ogc.pubsub._1_0.ObjectFactory pubSub = new eu.essi_lab.ogc.pubsub._1_0.ObjectFactory();

    private static oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory xacml = new oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory();

    public static eu.essi_lab.ogc.pubsub._1_0.ObjectFactory PUB_SUB() {
	return pubSub;
    }

    public static oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory XACML() {
	return xacml;
    }

    public static net.opengis.iso19139.gco.v_20060504.ObjectFactory GCO() {
	return gco;
    }

    public static net.opengis.iso19139.gmd.v_20060504.ObjectFactory GMD() {
	return gmd;
    }

    public static net.opengis.gml.v_3_2_0.ObjectFactory GML() {
	return gml;
    }

    public static net.opengis.iso19139.gmx.v_20060504.ObjectFactory GMX() {
	return gmx;
    }

    public static net.opengis.iso19139.gsr.v_20060504.ObjectFactory GSR() {
	return gsr;
    }

    public static net.opengis.iso19139.gss.v_20060504.ObjectFactory GSS() {
	return gss;
    }

    public static net.opengis.iso19139.gts.v_20060504.ObjectFactory GTS() {
	return gts;
    }

    public static net.opengis.iso19139.srv.v_20060504.ObjectFactory SRV() {
	return srv;
    }

    public static eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.ObjectFactory GMI() {
	return gmi;
    }

    public static eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.ObjectFactory DCE() {
	return dce;
    }

    public static eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.terms.ObjectFactory DCT() {
	return dct;
    }

    public static eu.essi_lab.jaxb.ows._1_0_0.ObjectFactory OWS() {
	return ows;
    }

    public static eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory CSW() {
	return csw;
    }

    public static net.opengis.wfs.v_1_1_0.ObjectFactory WFS() {
	return wfs;
    }

}
