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

import eu.essi_lab.lib.net.protocols.impl.*;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Fabrizio
 */
public enum NetProtocolWrapper {
    HMFS(new HMFS_Protocol()),//
    USGS_IV(new USGS_IV_Protocol()),//
    USGS_DV(new USGS_DV_Protocol()),//
    BCO_DMO(new BCO_DMOProtocol()),//
    EARTH_ENGINE(new EARTH_ENGINEProtocol()),//
    ECANADA(new ECANADAProtocol()),//
    EGASKRO(new EGASKROProtocol()),//
    ESRIMapServer_10_0_0(new ESRIMapServer_10_0_0Protocol()),//
    ESRIMapServer(new ESRIMapServerProtocol()),//
    FILE(new FILEProtocol()),//
    FTP(new FTPProtocol()),//
    GEOSS_HelperApp(new GEOSS_HelperAppProtocol()),//
    GEOSS_ONLINE_HelperApp(new GEOSS_ONLINE_HelperAppProtocol()),//
    GI_AXE(new GI_AXEProtocol()),//
    HTTP(new HTTPProtocol()),//
    HYDRO_DB(new HYDRO_DBProtocol()),//
    CUAHSI_WATER_ONE_FLOW_1_1(new CUAHSI_WaterOneFlow_1_1Protocol()),//
    CEHQ(new CEHQProtocol()),//
    DINAGUAWS(new DinaguawsProtocol()),//
    DINAGUASTATUSWS(new DinaguaStatuswsProtocol()),//
    NIWA(new NiwaProtocol()),//
    ARPA_LOMBARDIA(new ARPALombardiaProtocol()),//
    WIS(new WISProtocol()),//
    MCH(new MCHProtocol()),//
    HYRAX(new HYRAXProtocol()),//
    ISO(new ISOProtocol()),//
    BNDMET(new BNDMETProtocol()),//
    APITEMPO(new APITEMPOProtocol()),//
    INUMET(new INUMETProtocol()),//
    NRFA(new NRFAProtocol()),//
    DMH(new DMHProtocol()),//
    KISTERS(new KISTERSProtocol()),//
    NASAGSFCOZONE(new NASAGSFCOZONEProtocol()),//
    NCML(new NCMLProtocol()),//
    NERRS(new NERRSProtocol()),//
    NETCDFSubset(new NETCDFSubsetProtocol()),//
    OPENDAP(new OPENDAPProtocol()),//
    OPENSEARCH(new OPENSEARCHProtocol()),//
    ODATA_SYKE(new ODATA_SYKEProtocol()),//
    RASAQM(new RASAQMProtocol()),//
    SAVAHIS(new SAVAHISProtocol()),//
    SOS_1_0_0(new SOS_1_0_0Protocol()),//
    SOS_2_0_0_HydroProfile(new SOS_2_0_0_HydroProfileProtocol()),//
    SOS_2_0_0(new SOS_2_0_0Protocol()),//
    SOS_2_0_0_BOM(new SOS_2_0_0_BOMProtocol()),//
    SOS_2_0_0_TAHMO(new SOS_2_0_0_TAHMOProtocol()),//
    SOS(new SOSProtocol()),//
    TILED_SERVICE(new TILED_SERVICEProtocol()),//
    TRAJECTORY(new TRAJECTORYProtocol()),//
    UDDC(new UDDCProtocol()),//
    UNKNOWN(new UNKNOWNProtocol()),//
    USGS_LANDSAT(new USGS_LANDSATProtocol()),//
    WCS_1_0_0(new WCS_1_0_0Protocol()),//
    WCS_1_0_0_TDS(new WCS_1_0_0_TDSProtocol()),//
    WCS_1_0(new WCS_1_0Protocol()),//
    WCS_1_1_1(new WCS_1_1_1Protocol()),//
    WCS_1_1_2(new WCS_1_1_2Protocol()),//
    WCS_1_1(new WCS_1_1Protocol()),//
    WCS_2_0(new WCS_2_0Protocol()),//
    WCS_2_0_1(new WCS_2_0_1Protocol()),//
    WCS_EDO(new WCS_EDOProtocol()),//
    WCS(new WCSProtocol()),//
    WFS_1_0_0(new WFS_1_0_0Protocol()),//
    WFS_1_1_0(new WFS_1_1_0Protocol()),//
    WFS_2_0_0(new WFS_2_0_0Protocol()),//
    WFS(new WFSProtocol()),//
    WMS_1_1_1(new WMS_1_1_1Protocol()),//
    WMS_1_3_0(new WMS_1_3_0Protocol()),//
    WMS_Q_1_3_0(new WMS_Q_1_3_0Protocol()),//
    WMS(new WMSProtocol()),//
    WMTS_1_0_0(new WMTS_1_0_0Protocol()),//

    SENSOR_THINGS_1_1_HYDRO_SERVER_2(new SensorThings_1_1_HydroServer2_Protocol()),//
    SENSOR_THINGS_1_1_STA_4_HYDRO(new SensorThings_1_1_STA4Hydro_Protocol()),//
    SENSOR_THINGS_1_1_CITIOBS(new SensorThings_1_1_CITIOBS_Protocol()),//
    SENSOR_THINGS_1_1_FRAUNHOFER_AIR_QUALITY(new SensorThings_1_1_FraunhoferAirQuality_Protocol()),//
    SENSOR_THINGS_1_0_BRGM_WQ(new SensorThings_1_0_BRGM_WQ_Protocol());

    private final NetProtocol protocol;

    /**
     * @param protocol
     */
    NetProtocolWrapper(NetProtocol protocol) {

	this.protocol = protocol;
    }

    /**
     * @return
     */
    public String getCommonURN() {

	return get().getCommonURN();
    }

    /**
     * @return
     */
    public NetProtocol get() {

	return protocol;
    }

    /**
     *
     * @param protocol
     * @param wrapper
     * @return
     */
    public static boolean check(String protocol, NetProtocolWrapper wrapper){

	return NetProtocolWrapper.of(protocol).//
		map(p -> wrapper.get().equals(p)).//
		orElse(false);
    }

    /**
     * @param identifier
     * @return
     */
    public static Optional<NetProtocolWrapper> value(String identifier) {

	return Arrays.stream(NetProtocolWrapper.values()).//
		filter(np -> Arrays.asList(np.get().getURNs()).contains(identifier)).//
		findFirst();
    }

    /**
     * @param identifier
     * @return
     */
    public static Optional<NetProtocol> of(String identifier) {

	return value(identifier).map(NetProtocolWrapper::get);
    }
}
