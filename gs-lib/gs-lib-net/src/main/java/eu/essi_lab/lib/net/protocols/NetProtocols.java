package eu.essi_lab.lib.net.protocols;

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

import java.lang.reflect.Field;

import eu.essi_lab.lib.net.protocols.impl.APITEMPOProtocol;
import eu.essi_lab.lib.net.protocols.impl.ARPALombardiaProtocol;
import eu.essi_lab.lib.net.protocols.impl.BCO_DMOProtocol;
import eu.essi_lab.lib.net.protocols.impl.BNDMETProtocol;
import eu.essi_lab.lib.net.protocols.impl.CEHQProtocol;
import eu.essi_lab.lib.net.protocols.impl.CUAHSI_WaterOneFlow_1_1Protocol;
import eu.essi_lab.lib.net.protocols.impl.DMHProtocol;
import eu.essi_lab.lib.net.protocols.impl.DinaguaStatuswsProtocol;
import eu.essi_lab.lib.net.protocols.impl.DinaguawsProtocol;
import eu.essi_lab.lib.net.protocols.impl.EARTH_ENGINEProtocol;
import eu.essi_lab.lib.net.protocols.impl.ECANADAProtocol;
import eu.essi_lab.lib.net.protocols.impl.EGASKROProtocol;
import eu.essi_lab.lib.net.protocols.impl.ESRIMapServerProtocol;
import eu.essi_lab.lib.net.protocols.impl.ESRIMapServer_10_0_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.FILEProtocol;
import eu.essi_lab.lib.net.protocols.impl.FTPProtocol;
import eu.essi_lab.lib.net.protocols.impl.GEOSS_HelperAppProtocol;
import eu.essi_lab.lib.net.protocols.impl.GEOSS_ONLINE_HelperAppProtocol;
import eu.essi_lab.lib.net.protocols.impl.GI_AXEProtocol;
import eu.essi_lab.lib.net.protocols.impl.HTTPProtocol;
import eu.essi_lab.lib.net.protocols.impl.HYDRO_DBProtocol;
import eu.essi_lab.lib.net.protocols.impl.HYRAXProtocol;
import eu.essi_lab.lib.net.protocols.impl.INUMETProtocol;
import eu.essi_lab.lib.net.protocols.impl.ISOProtocol;
import eu.essi_lab.lib.net.protocols.impl.KISTERSProtocol;
import eu.essi_lab.lib.net.protocols.impl.MCHProtocol;
import eu.essi_lab.lib.net.protocols.impl.NASAGSFCOZONEProtocol;
import eu.essi_lab.lib.net.protocols.impl.NCMLProtocol;
import eu.essi_lab.lib.net.protocols.impl.NERRSProtocol;
import eu.essi_lab.lib.net.protocols.impl.NETCDFSubsetProtocol;
import eu.essi_lab.lib.net.protocols.impl.NRFAProtocol;
import eu.essi_lab.lib.net.protocols.impl.ODATA_SYKEProtocol;
import eu.essi_lab.lib.net.protocols.impl.OPENDAPProtocol;
import eu.essi_lab.lib.net.protocols.impl.OPENSEARCHProtocol;
import eu.essi_lab.lib.net.protocols.impl.RASAQMProtocol;
import eu.essi_lab.lib.net.protocols.impl.SAVAHISProtocol;
import eu.essi_lab.lib.net.protocols.impl.SOSProtocol;
import eu.essi_lab.lib.net.protocols.impl.SOS_1_0_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.SOS_2_0_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.SOS_2_0_0_BOMProtocol;
import eu.essi_lab.lib.net.protocols.impl.SOS_2_0_0_HydroProfileProtocol;
import eu.essi_lab.lib.net.protocols.impl.SOS_2_0_0_TAHMOProtocol;
import eu.essi_lab.lib.net.protocols.impl.SensorThings_1_0_BRGM_WQ_Protocol;
import eu.essi_lab.lib.net.protocols.impl.SensorThings_1_1_CITIOBS_Protocol;
import eu.essi_lab.lib.net.protocols.impl.SensorThings_1_1_FraunhoferAirQuality_Protocol;
import eu.essi_lab.lib.net.protocols.impl.SensorThings_1_1_HydroServer2_Protocol;
import eu.essi_lab.lib.net.protocols.impl.SensorThings_1_1_STA4Hydro_Protocol;
import eu.essi_lab.lib.net.protocols.impl.TILED_SERVICEProtocol;
import eu.essi_lab.lib.net.protocols.impl.TRAJECTORYProtocol;
import eu.essi_lab.lib.net.protocols.impl.UDDCProtocol;
import eu.essi_lab.lib.net.protocols.impl.UNKNOWNProtocol;
import eu.essi_lab.lib.net.protocols.impl.USGS_LANDSATProtocol;
import eu.essi_lab.lib.net.protocols.impl.WCSProtocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_1_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_1_0_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_1_0_0_TDSProtocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_1_1Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_1_1_1Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_1_1_2Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_2_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_2_0_1Protocol;
import eu.essi_lab.lib.net.protocols.impl.WCS_EDOProtocol;
import eu.essi_lab.lib.net.protocols.impl.WFSProtocol;
import eu.essi_lab.lib.net.protocols.impl.WFS_1_0_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WFS_1_1_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WFS_2_0_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WISProtocol;
import eu.essi_lab.lib.net.protocols.impl.WMSProtocol;
import eu.essi_lab.lib.net.protocols.impl.WMS_1_1_1Protocol;
import eu.essi_lab.lib.net.protocols.impl.WMS_1_3_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WMS_Q_1_3_0Protocol;
import eu.essi_lab.lib.net.protocols.impl.WMTS_1_0_0Protocol;

/**
 * @author Fabrizio
 */
public class NetProtocols {
    public static NetProtocol HMFS = new HMFS_Protocol();
    public static NetProtocol USGS_IV = new USGS_IV_Protocol();
    public static NetProtocol USGS_DV = new USGS_DV_Protocol();
    public static NetProtocol BCO_DMO = new BCO_DMOProtocol();
    public static NetProtocol EARTH_ENGINE = new EARTH_ENGINEProtocol();
    public static NetProtocol ECANADA = new ECANADAProtocol();
    public static NetProtocol EGASKRO = new EGASKROProtocol();
    public static NetProtocol ESRIMapServer_10_0_0 = new ESRIMapServer_10_0_0Protocol();
    public static NetProtocol ESRIMapServer = new ESRIMapServerProtocol();
    public static NetProtocol FILE = new FILEProtocol();
    public static NetProtocol FTP = new FTPProtocol();
    public static NetProtocol GEOSS_HelperApp = new GEOSS_HelperAppProtocol();
    public static NetProtocol GEOSS_ONLINE_HelperApp = new GEOSS_ONLINE_HelperAppProtocol();
    public static NetProtocol GI_AXE = new GI_AXEProtocol();
    public static NetProtocol HTTP = new HTTPProtocol();
    public static NetProtocol HYDRO_DB = new HYDRO_DBProtocol();
    public static NetProtocol CUAHSI_WATER_ONE_FLOW_1_1 = new CUAHSI_WaterOneFlow_1_1Protocol();
    public static NetProtocol CEHQ = new CEHQProtocol();
    public static NetProtocol DINAGUAWS = new DinaguawsProtocol();
    public static NetProtocol DINAGUASTATUSWS = new DinaguaStatuswsProtocol();
    public static NetProtocol NIWA = new NiwaProtocol();
    public static NetProtocol ARPA_LOMBARDIA = new ARPALombardiaProtocol();
    public static NetProtocol WIS = new WISProtocol();
    public static NetProtocol MCH = new MCHProtocol();
    public static NetProtocol HYRAX = new HYRAXProtocol();
    public static NetProtocol ISO = new ISOProtocol();
    public static NetProtocol BNDMET = new BNDMETProtocol();
    public static NetProtocol APITEMPO = new APITEMPOProtocol();
    public static NetProtocol INUMET = new INUMETProtocol();
    public static NetProtocol NRFA = new NRFAProtocol();
    public static NetProtocol DMH = new DMHProtocol();
    public static NetProtocol KISTERS = new KISTERSProtocol();
    public static NetProtocol NASAGSFCOZONE = new NASAGSFCOZONEProtocol();
    public static NetProtocol NCML = new NCMLProtocol();
    public static NetProtocol NERRS = new NERRSProtocol();
    public static NetProtocol NETCDFSubset = new NETCDFSubsetProtocol();
    public static NetProtocol OPENDAP = new OPENDAPProtocol();
    public static NetProtocol OPENSEARCH = new OPENSEARCHProtocol();
    public static NetProtocol ODATA_SYKE = new ODATA_SYKEProtocol();
    public static NetProtocol RASAQM = new RASAQMProtocol();
    public static NetProtocol SAVAHIS = new SAVAHISProtocol();
    public static NetProtocol SOS_1_0_0 = new SOS_1_0_0Protocol();
    public static NetProtocol SOS_2_0_0_HydroProfile = new SOS_2_0_0_HydroProfileProtocol();
    public static NetProtocol SOS_2_0_0 = new SOS_2_0_0Protocol();
    public static NetProtocol SOS_2_0_0_BOM = new SOS_2_0_0_BOMProtocol();
    public static NetProtocol SOS_2_0_0_TAHMO = new SOS_2_0_0_TAHMOProtocol();
    public static NetProtocol SOS = new SOSProtocol();
    public static NetProtocol TILED_SERVICE = new TILED_SERVICEProtocol();
    public static NetProtocol TRAJECTORY = new TRAJECTORYProtocol();
    public static NetProtocol UDDC = new UDDCProtocol();
    public static NetProtocol UNKNOWN = new UNKNOWNProtocol();
    public static NetProtocol USGS_LANDSAT = new USGS_LANDSATProtocol();
    public static NetProtocol WCS_1_0_0 = new WCS_1_0_0Protocol();
    public static NetProtocol WCS_1_0_0_TDS = new WCS_1_0_0_TDSProtocol();
    public static NetProtocol WCS_1_0 = new WCS_1_0Protocol();
    public static NetProtocol WCS_1_1_1 = new WCS_1_1_1Protocol();
    public static NetProtocol WCS_1_1_2 = new WCS_1_1_2Protocol();
    public static NetProtocol WCS_1_1 = new WCS_1_1Protocol();
    public static NetProtocol WCS_2_0 = new WCS_2_0Protocol();
    public static NetProtocol WCS_2_0_1 = new WCS_2_0_1Protocol();
    public static NetProtocol WCS_EDO = new WCS_EDOProtocol();
    public static NetProtocol WCS = new WCSProtocol();
    public static NetProtocol WFS_1_0_0 = new WFS_1_0_0Protocol();
    public static NetProtocol WFS_1_1_0 = new WFS_1_1_0Protocol();
    public static NetProtocol WFS_2_0_0 = new WFS_2_0_0Protocol();
    public static NetProtocol WFS = new WFSProtocol();
    public static NetProtocol WMS_1_1_1 = new WMS_1_1_1Protocol();
    public static NetProtocol WMS_1_3_0 = new WMS_1_3_0Protocol();
    public static NetProtocol WMS_Q_1_3_0 = new WMS_Q_1_3_0Protocol();
    public static NetProtocol WMS = new WMSProtocol();
    public static NetProtocol WMTS_1_0_0 = new WMTS_1_0_0Protocol();

    public static NetProtocol SENSOR_THINGS_1_1_HYDRO_SERVER_2 = new SensorThings_1_1_HydroServer2_Protocol();
    public static NetProtocol SENSOR_THINGS_1_1_STA_4_HYDRO = new SensorThings_1_1_STA4Hydro_Protocol();
    public static NetProtocol SENSOR_THINGS_1_1_CITIOBS = new SensorThings_1_1_CITIOBS_Protocol();
    public static NetProtocol SENSOR_THINGS_1_1_FRAUNHOFER_AIR_QUALITY = new SensorThings_1_1_FraunhoferAirQuality_Protocol();
    public static NetProtocol SENSOR_THINGS_1_0_BRGM_WQ = new SensorThings_1_0_BRGM_WQ_Protocol();

    public static NetProtocol decodeFromIdentifier(String identifier) {
	Field[] fields = NetProtocols.class.getFields();
	for (Field field : fields) {
	    try {
		Object instance = field.get(null);
		if (instance instanceof NetProtocol) {
		    NetProtocol protocol = (NetProtocol) instance;
		    String[] urns = protocol.getURNs();
		    for (String urn : urns) {
			if (identifier.equals(urn)) {
			    return protocol;
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return null;
    }
}
