package eu.essi_lab.accessor.imo;

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

public enum ZRXPKeyword {

    SANR("Alphanumerical station number"), //
    SNAME("Station name"), //
    SWATER("River name"), //
    CDASA("Remote call logger/meter (DASA) number"), //
    CDASANAME("Remote call logger/meter (DASA) name"), //
    CCHANNEL("Remote call logger/meter (DASA) channel name"), //
    CCHANNELNO("Remote call logger/meter (DASA) channel number"), //
    CMW("Values per day for equidistant time series values"), //
    CNAME("Parameter name"), //
    CNR("Parameter number"), //
    CUNIT("Unit of the data value column"), //
    REXCHANGE("Import number of import agent for time series"), //
    RINVAL("Value for missing or invalid data record"), //
    RTIMELVL("Time series time level"), //
    XVLID("Time series internal id as defined by KiTSM"), //
    TSPATH("Time series absolute path as defined by KiTSM"), //
    CTAG("Special tag, is used as part ofthe import number beingdefined in the import agents"), //
    CTAGKEY("Special tag, is used as part ofthe import number beingdefined in the import agents"), //
    XTRUNCATE("removes all time series databefore import"), //
    METCODE("metering code for energymarket instance"), //
    METERNUMBER("meter number for energymarket instance"), //
    EDIS("EDIS/OBIS code for energymarket instance"), //
    TZ("time zone of all time stampsin the time series block, bothheader and data"), //
    ZDATE("time stamp of meter readingfor energy market"), //
    ZRXPMODE("mode"), //
    ZRXPVERSION("ZRXP format release"), //
    ZRXPCREATOR("name of the creation tool ofthe current ZRXP file"), //
    LAYOUT("specifies the column layoutfor the ZRXP data"), //
    TASKID("internal information;specifies the task identifier,only first occurrence isconsidered during import"), //
    SOURCESYSTEM("designator of source system,for example SODA"), //
    SOURCEID("time series identifier by thissource"), //
    // the following fields are DEPRECATED, from ZRXP 2
    // commands
    PNPCMD("PNP CMD", "PNP"), //
    VOLATILECMD("VOLATILE CMD", "VOLATILE"), //
    CTABLECMD("CTABLE CMD", "CTABLE"), //
    REXTRCMD("REXTR CMD", "REXTR"), //
    RSTATECMD("RSTATE CMD", "RSTATE"), //
    // headers
    CKONV("CKONV"), //
    CNTYPE("CNTYPE"), //
    CTYPE("CTYPE"), //
    RIMPORT("RIMPORT"), //
    RNR("RNR"), //
    RORPR("RORPR"), //
    RTYPE("RTYPE"), //
    XCLEAN("XCLEAN"), //
    EUNIT("EUNIT"), //
    CINSTANT("CINSTANT"), //
    METERSITE("METERSITE"), //
    REMDST("REMDST"), //
    EQFLAG("EQFLAG"), //

    /** CUSTOM */
    LATITUDE("LATITUDE"), //
    LONGITUDE("LONGITUDE")

    ;

    String[] alias;

    public String[] getAlias() {
	return alias;
    }

    String description;

    private ZRXPKeyword(String description, String... alias) {
	this.alias = new String[alias.length + 1];
	this.alias[0] = name();
	for (int i = 0; i < alias.length; i++) {
	    this.alias[i + 1] = alias[i];
	}
	this.description = description;
    }

    public static ZRXPKeyword fromKeyValue(String keyValue) {
	for (ZRXPKeyword zk : values()) {
	    for (String alias : zk.getAlias()) {
		if (keyValue.startsWith(alias)) {
		    return zk;
		}
	    }
	}
	return null;
    }

    public String getValue(String line) {
	for (String a : alias) {
	    line = line.replace(a, "");
	}
	return line;

    }

    public String getDescription() {
	return description;
    }
}
