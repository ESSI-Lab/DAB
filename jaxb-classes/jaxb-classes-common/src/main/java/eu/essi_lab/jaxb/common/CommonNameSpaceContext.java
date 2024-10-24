package eu.essi_lab.jaxb.common;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author Fabrizio
 */
public class CommonNameSpaceContext extends NamespacePrefixMapper implements NamespaceContext {

    protected HashMap<String, String> map;

    public static final String XACML_3_0 = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
    public static final String CSW_NS_URI = "http://www.opengis.net/cat/csw/2.0.2";
    public static final String CSW_SAEON_NS_URI = "http://app01.saeon.ac.za/PLATFORM_TEST/MAP/csw.asp";
    public static final String CMR_NS_URI = "https://cmr.earthdata.nasa.gov/csw/collections";
    public static final String GMD_NS_URI = "http://www.isotc211.org/2005/gmd";
    public static final String GMI_NS_URI = "http://www.isotc211.org/2005/gmi";
    public static final String GMX_NS_URI = "http://www.isotc211.org/2005/gmx";
    public static final String GML_NS_URI = "http://www.opengis.net/gml";
    public static final String DC_NS_URI = "http://purl.org/dc/elements/1.1/";
    public static final String DC_TERMS_NS_URI = "http://purl.org/dc/terms/";
    public static final String SRV_NS_URI = "http://www.isotc211.org/2005/srv";
    public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
    public static final String GTS_NS_URI = "http://www.isotc211.org/2005/gts";
    public static final String APISO_NS_URI = "http://www.opengis.net/cat/csw/apiso/1.0";
    public static final String SDN_NS_URI = "http://www.seadatanet.org";
    public static final String BLUECLOUD_NS_URI = "http://essi-lab.eu/BLUECLOUD";
    public static final String BLUECLOUD_API = "http://essi-lab.eu/BlueCloudAPI";
    public static final String ARGO_NS_URI = "http://essi-lab.eu/ARGO";
    public static final String WEKEO_NS_URI = "http://essi-lab.eu/WEKEO";
    public static final String EUROBIS_NS_URI = "http://essi-lab.eu/EurOBIS";
    public static final String EUROBIS_LD_NS_URI = "http://essi-lab.eu/EurOBIS-LD";
    public static final String EMODNET_PHYSICS_NS_URI = "http://essi-lab.eu/EMODNETPhysics";
    public static final String NIWA_NS_URI = "http://essi-lab.eu/NIWA";
    public static final String ICOS_NS_URI = "http://essi-lab.eu/ICOS";
    public static final String MCP_1_NS_URI = "http://bluenet3.antcrc.utas.edu.au/mcp";
    public static final String MCP_2_NS_URI = "http://schemas.aodn.org.au/mcp-2.0";
    public static final String NODC_NS_URI = "https://www.ngdc.noaa.gov/metadata/published/xsd/schema.xsd";
    public static final String GCO_NS_URI = "http://www.isotc211.org/2005/gco";
    public static final String GSR_NS_URI = "http://www.isotc211.org/2005/gsr";
    public static final String XSI_SCHEMA_INSTANCE_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSI_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    public static final String GML32_NS_URI = "http://www.opengis.net/gml/3.2";
    public static final String OM_NS_URI = "http://www.opengis.net/om/2.0";
    public static final String SAM_NS_URI = "http://www.opengis.net/sampling/2.0";
    public static final String SAMS_NS_URI = "http://www.opengis.net/samplingSpatial/2.0";
    public static final String WML1_NS_URI = "http://www.cuahsi.org/waterML/1.1/";
    public static final String WML1_INA_NS_URI = "https://alerta.ina.gob.ar/wml/";
    public static final String WML1_ISPRA_NS_URI = "http://hydroserver.ddns.net/italia";
    public static final String SAVAHIS_URI = "http://savahis.org";
    public static final String WML2_NS_URI = "http://www.opengis.net/waterml/2.0";
    public static final String WMS_1_1_1_NS_URI = "urn:ogc:serviceType:WebMapService:1.1.1";
    public static final String WMS_1_3_0_NS_URI = "urn:ogc:serviceType:WebMapService:1.3.0";
    public static final String WFS_1_1_0_NS_URI = "urn:ogc:serviceType:WebFeatureService:1.1.0";
    public static final String OGC_NS_URI = "http://www.opengis.net/ogc";
    public static final String OWS_NS_URI = "http://www.opengis.net/ows";
    public static final String OWS_1_1_NS_URI = "http://www.opengis.net/ows/1.1";
    public static final String OAI_NS_URI = "http://www.openarchives.org/OAI/2.0/";
    public static final String OAI_DC_NS_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    public static final String OAI_DATACITE_NS_URI = "https://schema.datacite.org/meta/kernel-4.3/metadata.xsd";
    public static final String PUB_SUB_NS_URI = "http://www.opengis.net/pubsub/1.0";
    public static final String MULTI = "http://essi-lab.eu/multi";
    public static final String OS_1_1_NS_URI = "http://a9.com/-/spec/opensearch/1.1/";
    public static final String GS_DATA_MODEL_SCHEMA_URI = "http://flora.eu/gi-suite/1.0/dataModel/schema";
    public static final String GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE = "http://flora.eu/gi-suite/1.0/dataModel/schema/gs-resource";
    public static final String ENVIRONMENT_CANADA_URI = "http://essi-lab.eu/ENVIRONMENT_CANADA";
    public static final String THUNDER_URI = "http://essi-lab.eu/THUNDER";
    public static final String ECV_INVENTORY = "http://essi-lab.eu/ECV_INVENTORY";

    public static final String SOS_2_0 = "http://www.opengis.net/spec/SOS/2.0/";
    public static final String SOS_1_0_0 = "http://www.opengis.net/spec/SOS/1.0.0/";

    public static final String ANA_URI = "http://essi-lab.eu/ANA";
    public static final String ANA_SAR_URI = "http://essi-lab.eu/ANA-SAR";
    public static final String CEHQ_URI = "https://www.cehq.gouv.qc.ca/hydrometrie";
    public static final String WIGOS = "http://def.wmo.int/wmdr/2017";
    public static final String INMET_CSV_URI = "http://essi-lab.eu/INMET";
    public static final String BNDMET_URI = "https://portal.inmet.gov.br/manual/manual-bndmet";
    public static final String APITEMPO_URI = "https://apitempo.inmet.gov.br/plata/";
    public static final String INUMET_URI = "https://api.inumet.gub.uy";
    public static final String NRFA_URI = "https://nrfaapps.ceh.ac.uk/nrfa/ws";
    public static final String BUFR_URI = "http://essi-lab.eu/BUFR";
    public static final String INPE_URI = "http://www.dgi.inpe.br/CDSR/";
    public static final String SENTINEL2_URI = "urn:sentinel2:shub";
    public static final String NVE_URI = "https://hydapi.nve.no/api/v1";
    public static final String DMH_URI = "https://meteorologia.gov.py";

    public static final String SMHI_URI = "https://opendata-download-hydroobs.smhi.se/api/version/1.0";
    public static final String IMO_URI = "http://customer.vedur.is";
    public static final String RIHMI_URI = "http://ws.meteo.ru";
    public static final String HIS_CENTRAL_SHAPEFILE = "http://shapefile.his-central.it";
    public static final String RIHMI_HISTORICAL_URI = "http://ws.meteo.ru/hydro/rest/GetHydroAveMonDischargesRF/xml/";
    public static final String BNHS_URI = "http://wmo.int/bnhs";
    public static final String RASAQM_URI = "http://www.feerc.ru/geoss/rasaqm";

    private static final String GEORSS = "http://www.georss.org/georss";
    public static final String DINAGUA_URI = "https://www.ambiente.gub.uy/axis2/services/Dinaguaws";
    public static final String WOD = "https://data.nodc.noaa.gov/woa/WOD";
    public static final String THREDDS_NS_URI = "http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0";
    public static final String INVENIO_NS_URI = "http://essi-lab.eu/invenio";

    public static final String HISCENTRAL_BASILICATA_NS_URI = "http://essi-lab.eu/his-central/basilicata";
    public static final String HISCENTRAL_SARDEGNA_NS_URI = "http://essi-lab.eu/his-central/sardegna";
    public static final String HISCENTRAL_TOSCANA_NS_URI = "http://essi-lab.eu/his-central/toscana";
    public static final String HISCENTRAL_UMBRIA_NS_URI = "http://essi-lab.eu/his-central/umbria";

    public static final String HISCENTRAL_EMILIA_NS_URI = "http://essi-lab.eu/his-central/emilia";

    public static final String HISCENTRAL_MARCHE_NS_URI = "http://essi-lab.eu/his-central/marche";
    public static final String HISCENTRAL_FRIULI_NS_URI = "http://essi-lab.eu/his-central/friuli";
    public static final String HISCENTRAL_VENETO_NS_URI = "http://essi-lab.eu/his-central/veneto";
    public static final String HISCENTRAL_LAZIO_NS_URI = "http://essi-lab.eu/his-central/lazio";
    public static final String HISCENTRAL_VALDAOSTA_NS_URI = "http://essi-lab.eu/his-central/valdaosta";
    public static final String HISCENTRAL_PIEMONTE_NS_URI = "http://essi-lab.eu/his-central/piemonte";
    public static final String HISCENTRAL_LIGURIA_NS_URI = "http://essi-lab.eu/his-central/liguria";
    public static final String HISCENTRAL_BOLZANO_NS_URI = "http://essi-lab.eu/his-central/bolzano";
    public static final String HISCENTRAL_PUGLIA_NS_URI = "http://essi-lab.eu/his-central/puglia";
    public static final String WMS_1_3_0_QWeMS_NS_URI = "urn:ogc:serviceType:WebMapService:QWeMS:1.3.0";
    public static final String POLYTOPE = "http://essi-lab.eu/Polytope";
    public static final String POLYTOPE_METEOTRACKER = "http://essi-lab.eu/Polytope/Meteotracker";
    public static final String POLYTOPE_IONBEAM = "http://essi-lab.eu/Polytope/IonBeam";

    public static final String DWS_URI = "urn:essi:serviceType:DWS";

    public static final String AUTOMATIC_SYSTEM_PARAGUAY_URI = "https://automaticas.meteorologia.gov.py";
    public static final String SIGEDAC_PARAGUAY_URI = "https://sigedac.meteorologia.gov.py";
    public static final String SIGEDAC_RIVER_URI = "https://sigedac.river.meteorologia.gov.py";

    public static final String DIF_URI = "http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/dif.xsd";

    public static final String METEOTRACKER = "https://app.meteotracker.com";

    public static final String TRIGGER = "https://trigger-io.difa.unibo.it";
    
    public static final String SMARTCITIZENKIT = "https://api.smartcitizen.me";
    
    public static final String ACRONET = "https://webdrops.cimafoundation.org/";
    public static final String GEOMOUNTAIN = "http://essi-lab.eu/geomountain";
    
    public static final String EEA_NS_URI = "http://essi-lab.eu/eea";
    
    public static final String AGAME_NS_URI = "http://essi-lab.eu/agame";

    public CommonNameSpaceContext() {

	map = new HashMap<String, String>();
	map.put(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
	map.put("xacml", XACML_3_0);
	map.put("sdn", SDN_NS_URI);
	map.put("mcp1", MCP_1_NS_URI);
	map.put("mcp2", MCP_2_NS_URI);
	map.put("gco", GCO_NS_URI);
	map.put("gmd", GMD_NS_URI);
	map.put("gml", GML_NS_URI);
	map.put("gmx", GMX_NS_URI);
	map.put("gmi", GMI_NS_URI);
	map.put("xlink", XLINK_NS_URI);
	map.put("srv", SRV_NS_URI);
	map.put("csw", CSW_NS_URI);
	map.put("dcterms", DC_TERMS_NS_URI);
	map.put("dc", DC_NS_URI);
	map.put("apiso", APISO_NS_URI);
	map.put("gts", GTS_NS_URI);
	map.put("gsr", GSR_NS_URI);
	map.put("xsi", XSI_SCHEMA_INSTANCE_NS_URI);
	map.put("xs", XSI_SCHEMA_NS_URI);
	map.put("gml32", GML32_NS_URI);
	map.put("om", OM_NS_URI);
	map.put("sam", SAM_NS_URI);
	map.put("sams", SAMS_NS_URI);
	map.put("wml1", WML1_NS_URI);
	map.put("wml2", WML2_NS_URI);
	map.put("ogc", OGC_NS_URI);
	map.put("ows", OWS_NS_URI);
	map.put("ows11", OWS_1_1_NS_URI);
	map.put("oai", OAI_NS_URI);
	map.put("pubSub", PUB_SUB_NS_URI);
	map.put("opensearch", OS_1_1_NS_URI);
	map.put("os", OS_1_1_NS_URI);
	map.put("georss", GEORSS);
	map.put("eccanada", ENVIRONMENT_CANADA_URI);
	map.put("ana", ANA_URI);
	map.put("inmet", INMET_CSV_URI);
	map.put("sentinel", SENTINEL2_URI);
	map.put("bluecloud", BLUECLOUD_NS_URI);
	map.put("argo", ARGO_NS_URI);
	map.put("wekeo", WEKEO_NS_URI);
	map.put("icos", ICOS_NS_URI);
	map.put("eurobis", EUROBIS_NS_URI);
	map.put("catalog", THREDDS_NS_URI);
	map.put("invenio", INVENIO_NS_URI);
	map.put("hiscentraltoscana", HISCENTRAL_TOSCANA_NS_URI);
	map.put("hiscentralumbria", HISCENTRAL_UMBRIA_NS_URI);
	map.put("hiscentralemilia", HISCENTRAL_EMILIA_NS_URI);
	map.put("hiscentralmarche", HISCENTRAL_MARCHE_NS_URI);
	map.put("hiscentralfriuli", HISCENTRAL_FRIULI_NS_URI);
	map.put("hiscentralveneto", HISCENTRAL_VENETO_NS_URI);
	map.put("hiscentrallazio", HISCENTRAL_LAZIO_NS_URI);
	map.put("hiscentralvaldaosta", HISCENTRAL_VALDAOSTA_NS_URI);
	map.put("hiscentralpiemonte", HISCENTRAL_PIEMONTE_NS_URI);
	map.put("hiscentralliguria", HISCENTRAL_LIGURIA_NS_URI);
	map.put("hiscentralbolzano", HISCENTRAL_BOLZANO_NS_URI);
	map.put("hiscentralsardegna", HISCENTRAL_SARDEGNA_NS_URI);
	map.put("hiscentralbasilicata", HISCENTRAL_BASILICATA_NS_URI);
	map.put("hiscentralpuglia", HISCENTRAL_PUGLIA_NS_URI);

	map.put("polytopemeteotracker", POLYTOPE_METEOTRACKER);
	map.put("bluecloudapi", BLUECLOUD_API);
	map.put("dwsuri", DWS_URI);
	map.put("asparaguayuri", AUTOMATIC_SYSTEM_PARAGUAY_URI);
	map.put("sigedacparaguayuri", SIGEDAC_PARAGUAY_URI);
	map.put("sigedacriveruri", SIGEDAC_RIVER_URI);
	map.put("dif", DIF_URI);
	map.put("meteotracker", METEOTRACKER);
	map.put("trigger", TRIGGER);
	map.put("smartcitizenkit", SMARTCITIZENKIT);
	map.put("acronet", ACRONET);
	map.put("geomountain", GEOMOUNTAIN);
	map.put("eea", EEA_NS_URI);
	map.put("agame", AGAME_NS_URI);

    }

    /**
     * Creates an instance of {@link XMLDocumentReader} with {@link CommonNameSpaceContext} set
     * 
     * @see #setNamespaceContext(javax.xml.namespace.NamespaceContext)
     * @param document
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public static XMLDocumentReader createCommonReader(Document document) {

	XMLDocumentReader reader = new XMLDocumentReader(document);

	reader.setNamespaceContext(new CommonNameSpaceContext());

	return reader;
    }

    /**
     * Creates an instance of {@link XMLDocumentReader} with {@link CommonNameSpaceContext} set
     * 
     * @see #setNamespaceContext(javax.xml.namespace.NamespaceContext)
     * @param document
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public static XMLDocumentReader createCommonReader(String document) throws SAXException, IOException {

	XMLDocumentReader reader = new XMLDocumentReader(document);

	reader.setNamespaceContext(new CommonNameSpaceContext());

	return reader;
    }

    /**
     * Creates an instance of {@link XMLDocumentReader} with {@link CommonNameSpaceContext} set
     * 
     * @see #setNamespaceContext(javax.xml.namespace.NamespaceContext)
     * @param stream
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public static XMLDocumentReader createCommonReader(InputStream stream) throws SAXException, IOException {

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	reader.setNamespaceContext(new CommonNameSpaceContext());

	return reader;
    }

    @Override
    public String getNamespaceURI(String prefix) {

	return map.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {

	return map.keySet().stream().//
		filter(p -> map.get(p).equals(namespaceURI)).//
		findFirst().//
		orElse(null);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {

	return map.keySet().stream().//
		filter(p -> map.get(p).equals(namespaceURI)).//
		collect(Collectors.toList()).//
		iterator();
    }

    @Override
    public String getPreferredPrefix(String namespaceURI, String suggestion, boolean requirePrefix) {

	return getPrefix(namespaceURI);
    }

    @Override
    public String[] getPreDeclaredNamespaceUris2() {

	return new String[] { "xsi", "http://www.w3.org/2001/XMLSchema-instance", "xs", "http://www.w3.org/2001/XMLSchema" };
    }

    /**
     * @return
     */

    public HashMap<String, String> getMap() {
	return map;
    }
}
