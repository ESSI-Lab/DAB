package eu.essi_lab.accessor.savahis;

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

public enum SavaHISRiverBasin {
    UNKNOWN(-1, "Unknown"), //
    BOSNA(55, "Bosna"), //
    BOSUT(58, "Bosut"), //
    DRINA(49, "Drina"), //
    ILOVA(52, "Ilova"), //
    KOLUBARA(50, "Kolubara"), //
    KRAPINA(57, "Krapina"), //
    KRKA(53, "Krka"), //
    KUPA_KOLPA(63, "Kupa/Kolpa"), //
    LUBLIJANICA(66, "Lublijanica"), //
    LONJA(47, "Lonja"), //
    ORLJAVA(48, "Orljava"), //
    SVA_BOHINJKA(72, "Sava Bohinjka"), //
    SVA_DIRECT(69, "Sava Direct"), //
    SAVA_DOLINKA(61, "Sava Dolinka"), //
    SAVINJA(56, "Savinja"), //
    SORA(71, "Sora"), //
    SOTLA_SUTLA(67, "Sotla/Sutla"), //
    TINJA(73, "Tinja"), //
    UKRINA(70, "Ukrina"), //
    UNA(62, "Una"), //
    VRBAS(51, "Vrbas"); //

    int code;

    String name;

    SavaHISRiverBasin(int code, String name) {
	this.code = code;
	this.name = name;
    }

    public int getCode() {
	return code;
    }

    public String getName() {
	return name;
    }

    public static SavaHISRiverBasin decode(String code) {
	if (code != null)
	    for (SavaHISRiverBasin value : values()) {
		if (code.equals("" + value.getCode())) {
		    return value;
		}
	    }
	return decode("-1");
    }
}
