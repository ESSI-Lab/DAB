package eu.essi_lab.accessor.imo;

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

public enum ZRXPDataKeyword {

	TIMESTAMP("primary time stamp column","timestamp"), //
	VALUE("primary numeric value column","value"), //
	STATUS("ZRXP v.2 status (deprecated)","status"), //
	PRIMARY_STATUS("primary status column","primary_status"), //
	SYSTEM_STATUS("system status column","system_status"), //
	ADDITIONAL_STATUS("additional status column","additional_status"), //
	INTERPOLATION_TYPE("interpolation type TCA column","interpolation_type"), //
	REMARK("remarks column","remark"), //
	TIMESTAMPOCCURRENCE("time stamp column for occurrence","timestampoccurrence"), //
	OCCURRENCECOUNT("reset number column","occurrencecount"), //
	MEMBER("member column","member"), //
	FORECAST("time stamp column for forecast","forecast"), //
	SIGNATURE("column for signature code of a value","signature"), //
	RESET_NUMBER("reset number column","reset_number"), //
	RESET_TIMESTAMP("reset time stamp column","reset_timestamp"), //
	RELEASELEVEL("release level column","releaselevel"), //
	DISPATCH_INFO("dispatch information column;", "dispatch_info","dispatchinfo");//

	String[] alias;

	public String[] getAlias() {
		return alias;
	}

	String description;

	public String getDescription() {
		return description;
	}

	private ZRXPDataKeyword(String description, String... alias) {
		this.description = description;
		this.alias = new String[alias.length + 1];
		this.alias[0] = name();
		for (int i = 0; i < alias.length; i++) {
			this.alias[i + 1] = alias[i];
		}
	}

	public static ZRXPDataKeyword fromKey(String key) {
		for (ZRXPDataKeyword zk : values()) {
			for (String alias : zk.getAlias()) {
				if (key.equalsIgnoreCase(alias)) {
					return zk;
				}
			}
		}
		return null;
	}
}
