package eu.essi_lab.accessor.savahis;

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

public enum SavaHISRiver {
    UNKNOWN(-1, "Unknown"), //
    BOSNA(30, "Bosna"), //
    BOSUT(8, "Bosut"), //
    DONJA_DOBRA(1, "Donja Dobra"), //
    DRINA(3, "Drina"), //
    DRINJACA(25, "Drinjača"), //
    GLINA(37, "Glina"), //
    GORNJA_DOBRA(16, "Gornja Dobra"), //
    ILOVA(26, "Ilova"), //
    JADAR(21, "Jadar"), //
    KOLUBARA(22, "Kolubara"), //
    KORANA(15, "Korana"), //
    KRAPINA(33, "Krapina"), //
    KRIVAJA(31, "Krivaja"), //
    KRKA(3, "Krka"), //
    KUPA_KOLPA(24, "Kupa/Kolpa"), //
    LASVA(14, "Lašva"), //
    LIM(35, "Lim"), //
    LJIG(43, "Ljig"), //
    LJUBLIANICA(3, "Ljublianica"), //
    MILJACKA(27, "Miljacka"), //
    ORLJAVA(20, "Orljava"), //
    PLTIVICA(5, "Plitvica"), //
    SANA(44, "Sana"), //
    SAVA_BOHINJKA(38, "Sava Bohinjka"), //
    SAVA_DOLINKA(9, "Sava Dolinka"), //
    SAVINJA(32, "Savinja"), //
    SORA(7, "Sora"), //
    SOTLA_SUTLA(2, "Sotla/Sutla"), //
    SPRECA(45, "Spreča"), //
    STUDVA(11, "Studva"), //
    TAMNAVA(36, "Tamnava"), //
    TARA(3, "Tara"), //
    TINJA(12, "Tinja"), //
    UKRINA(17, "Ukrina"), //
    UNA(40, "Una"), //
    UNAC(39, "Unac"), //
    USORA(19, "Usora"), //
    VAPA(10, "Vapa"), //
    VRBANJA(13, "Vrbanja"), //
    VRBAS(6, "Vrbas"), //
    CEHOTINA(28, "Ćehotina"), //
    CESMA(4, "Česma"), //
    ZELJEZNICA(42, "Željeznica"); //

    int code;

    String name;

    SavaHISRiver(int code, String name) {
	this.code = code;
	this.name = name;
    }

    public int getCode() {
	return code;
    }

    public String getName() {
	return name;
    }

    public static SavaHISRiver decode(String code) {
	if (code != null)
	    for (SavaHISRiver value : values()) {
		if (code.equals("" + value.getCode())) {
		    return value;
		}
	    }
	return decode("-1");
    }
}
