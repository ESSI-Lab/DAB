package eu.essi_lab.model.resource;

import java.util.Arrays;

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

public enum Country {
    AFGHANISTAN("Afghanistan", "the Islamic Republic of Afghanistan", "AFG", "AF", "004"),
    ALBANIA("Albania", "the Republic of Albania", "ALB", "AL", "008"),
    ALGERIA("Algeria", "the People's Democratic Republic of Algeria", "DZA", "DZ", "012"),
    ANDORRA("Andorra", "the Principality of Andorra", "AND", "AD", "020"),
    ANGOLA("Angola", "the Republic of Angola", "AGO", "AO", "024"),
    ANTIGUA_AND_BARBUDA("Antigua and Barbuda", "Antigua and Barbuda", "ATG", "AG", "028"),
    ARGENTINA("Argentina", "the Argentine Republic", "ARG", "AR", "032"),
    ARMENIA("Armenia", "the Republic of Armenia", "ARM", "AM", "051"),
    AUSTRALIA("Australia", "Australia", "AUS", "AU", "036"),
    AUSTRIA("Austria", "the Republic of Austria", "AUT", "AT", "040"),
    AZERBAIJAN("Azerbaijan", "the Republic of Azerbaijan", "AZE", "AZ", "031"),
    BAHAMAS("Bahamas", "the Commonwealth of the Bahamas", "BHS", "BS", "044"),
    BAHRAIN("Bahrain", "the Kingdom of Bahrain", "BHR", "BH", "048"),
    BANGLADESH("Bangladesh", "the People's Republic of Bangladesh", "BGD", "BD", "050"),
    BARBADOS("Barbados", "Barbados", "BRB", "BB", "052"),
    BELARUS("Belarus", "the Republic of Belarus", "BLR", "BY", "112"),
    BELGIUM("Belgium", "the Kingdom of Belgium", "BEL", "BE", "056"),
    BELIZE("Belize", "Belize", "BLZ", "BZ", "084"),
    BENIN("Benin", "the Republic of Benin", "BEN", "BJ", "204"),
    BHUTAN("Bhutan", "the Kingdom of Bhutan", "BTN", "BT", "064"),
    BOLIVIA_PLURINATIONAL_STATE_OF("Bolivia (Plurinational State of)", "the Plurinational State of Bolivia", "BOL", "BO", "068"),
    BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina", "Bosnia and Herzegovina", "BIH", "BA", "070"),
    BOTSWANA("Botswana", "the Republic of Botswana", "BWA", "BW", "072"),
    BRAZIL("Brazil", "the Federative Republic of Brazil", "BRA", "BR", "Brasil", "076"),
    BRUNEI_DARUSSALAM("Brunei Darussalam", "Brunei Darussalam", "BRN", "BN", "096"),
    BULGARIA("Bulgaria", "the Republic of Bulgaria", "BGR", "BG", "100"),
    BURKINA_FASO("Burkina Faso", "Burkina Faso", "BFA", "BF", "854"),
    BURUNDI("Burundi", "the Republic of Burundi", "BDI", "BI", "108"),
    CABO_VERDE("Cabo Verde", "the Republic of Cabo Verde", "CPV", "CV", "132"),
    CAMBODIA("Cambodia", "the Kingdom of Cambodia", "KHM", "KH", "116"),
    CAMEROON("Cameroon", "the Republic of Cameroon", "CMR", "CM", "120"),
    CANADA("Canada", "Canada", "CAN", "CA", "124"),
    CENTRAL_AFRICAN_REPUBLIC("Central African Republic", "the Central African Republic", "CAF", "CF", "140"),
    CHAD("Chad", "the Republic of Chad", "TCD", "TD", "148"),
    CHILE("Chile", "the Republic of Chile", "CHL", "CL", "152"),
    CHINA("China", "the People's Republic of China", "CHN", "CN", "156"),
    COLOMBIA("Colombia", "the Republic of Colombia", "COL", "CO", "170"),
    COMOROS("Comoros", "the Union of the Comoros", "COM", "KM", "174"),
    CONGO("Congo", "the Republic of the Congo", "COG", "CG", "178"),
    COOK_ISLANDS("Cook Islands", "the Cook Islands ", "COK", "CK", "184"),
    COSTA_RICA("Costa Rica", "the Republic of Costa Rica", "CRI", "CR", "188"),
    CROATIA("Croatia", "the Republic of Croatia", "HRV", "HR", "191"),
    CUBA("Cuba", "the Republic of Cuba", "CUB", "CU", "192"),
    CYPRUS("Cyprus", "the Republic of Cyprus", "CYP", "CY", "196"),
    CZECHIA("Czechia", "the Czech Republic", "CZE", "CZ", "203"),
    CÔTE_DIVOIRE("Côte d'Ivoire", "the Republic of Côte d'Ivoire", "CIV", "CI", "384"),
    DEMOCRATIC_PEOPLES_REPUBLIC_OF_KOREA("Democratic People's Republic of Korea", "the Democratic People's Republic of Korea", "PRK", "KP", "408"),
    DEMOCRATIC_REPUBLIC_OF_THE_CONGO("Democratic Republic of the Congo", "the Democratic Republic of the Congo", "COD", "CD", "180"),
    DENMARK("Denmark", "the Kingdom of Denmark", "DNK", "DK", "208"),
    DJIBOUTI("Djibouti", "the Republic of Djibouti", "DJI", "DJ", "262"),
    DOMINICA("Dominica", "the Commonwealth of Dominica", "DMA", "DM", "212"),
    DOMINICAN_REPUBLIC("Dominican Republic", "the Dominican Republic", "DOM", "DO", "214"),
    ECUADOR("Ecuador", "the Republic of Ecuador", "ECU", "EC", "218"),
    EGYPT("Egypt", "the Arab Republic of Egypt", "EGY", "EG", "818"),
    EL_SALVADOR("El Salvador", "the Republic of El Salvador", "SLV", "SV", "222"),
    EQUATORIAL_GUINEA("Equatorial Guinea", "the Republic of Equatorial Guinea", "GNQ", "GQ", "226"),
    ERITREA("Eritrea", "the State of Eritrea", "ERI", "ER", "232"),
    ESTONIA("Estonia", "the Republic of Estonia", "EST", "EE", "233"),
    ESWATINI("Eswatini", "the Kingdom of Eswatini ", "SWZ", "SZ", "748"),
    ETHIOPIA("Ethiopia", "the Federal Democratic Republic of Ethiopia", "ETH", "ET", "231"),
    FAROE_ISLANDS_("Faroe Islands ", "the Faroe Islands", "FRO", "FO", "234"),
    FIJI("Fiji", "the Republic of Fiji", "FJI", "FJ", "242"),
    FINLAND("Finland", "the Republic of Finland", "FIN", "FI", "246"),
    FRANCE("France", "the French Republic", "FRA", "FR", "250"),
    GABON("Gabon", "the Gabonese Republic", "GAB", "GA", "266"),
    GAMBIA("Gambia", "the Republic of the Gambia", "GMB", "GM", "270"),
    GEORGIA("Georgia", "Georgia", "GEO", "GE", "268"),
    GERMANY("Germany", "the Federal Republic of Germany", "DEU", "DE", "276"),
    GHANA("Ghana", "the Republic of Ghana", "GHA", "GH", "288"),
    GREECE("Greece", "the Hellenic Republic", "GRC", "GR", "300"),
    GRENADA("Grenada", "Grenada", "GRD", "GD", "308"),
    GUATEMALA("Guatemala", "the Republic of Guatemala", "GTM", "GT", "320"),
    GUINEA("Guinea", "the Republic of Guinea", "GIN", "GN", "324"),
    GUINEABISSAU("Guinea-Bissau", "the Republic of Guinea-Bissau", "GNB", "GW", "624"),
    GUYANA("Guyana", "the Co-operative Republic of Guyana", "GUY", "GY", "328"),
	HAITI("Haiti", "the Republic of Haiti", "HTI", "HT", "332"),
    HONDURAS("Honduras", "the Republic of Honduras", "HND", "HN", "340"),
    HUNGARY("Hungary", "Hungary", "HUN", "HU", "348"),
    ICELAND("Iceland", "the Republic of Iceland", "ISL", "IS", "352"),
    INDIA("India", "the Republic of India", "IND", "IN", "356"),
    INDONESIA("Indonesia", "the Republic of Indonesia", "IDN", "ID", "360"),
    IRAN_ISLAMIC_REPUBLIC_OF("Iran (Islamic Republic of)", "the Islamic Republic of Iran", "IRN", "IR", "364"),
    IRAQ("Iraq", "the Republic of Iraq", "IRQ", "IQ", "368"),
    IRELAND("Ireland", "Ireland", "IRL", "IE", "372"),
    ISRAEL("Israel", "the State of Israel", "ISR", "IL", "376"),
    ITALY("Italy", "the Republic of Italy", "ITA", "IT", "380"),
    JAMAICA("Jamaica", "Jamaica", "JAM", "JM", "388"),
    JAPAN("Japan", "Japan", "JPN", "JP", "392"),
    JORDAN("Jordan", "the Hashemite Kingdom of Jordan", "JOR", "JO", "400"),
    KAZAKHSTAN("Kazakhstan", "the Republic of Kazakhstan", "KAZ", "KZ", "398"),
    KENYA("Kenya", "the Republic of Kenya", "KEN", "KE", "404"),
    KIRIBATI("Kiribati", "the Republic of Kiribati", "KIR", "KI", "296"),
    KUWAIT("Kuwait", "the State of Kuwait", "KWT", "KW", "414"),
    KYRGYZSTAN("Kyrgyzstan", "the Kyrgyz Republic", "KGZ", "KG", "417"),
    LAO_PEOPLES_DEMOCRATIC_REPUBLIC("Lao People's Democratic Republic", "the Lao People's Democratic Republic", "LAO", "LA", "418"),
    LATVIA("Latvia", "the Republic of Latvia", "LVA", "LV", "428"),
    LEBANON("Lebanon", "the Lebanese Republic", "LBN", "LB", "422"),
    LESOTHO("Lesotho", "the Kingdom of Lesotho", "LSO", "LS", "426"),
    LIBERIA("Liberia", "the Republic of Liberia", "LBR", "LR", "430"),
    LIBYA("Libya", "the State of Libya", "LBY", "LY", "434"),
    LITHUANIA("Lithuania", "the Republic of Lithuania", "LTU", "LT", "440"),
    LUXEMBOURG("Luxembourg", "the Grand Duchy of Luxembourg", "LUX", "LU", "442"),
    MADAGASCAR("Madagascar", "the Republic of Madagascar", "MDG", "MG", "450"),
    MALAWI("Malawi", "the Republic of Malawi", "MWI", "MW", "454"),
    MALAYSIA("Malaysia", "Malaysia", "MYS", "MY", "458"),
    MALDIVES("Maldives", "the Republic of Maldives", "MDV", "MV", "462"),
    MALI("Mali", "the Republic of Mali", "MLI", "ML", "466"),
    MALTA("Malta", "the Republic of Malta", "MLT", "MT", "470"),
    MARSHALL_ISLANDS("Marshall Islands", "the Republic of the Marshall Islands", "MHL", "MH", "584"),
    MAURITANIA("Mauritania", "the Islamic Republic of Mauritania", "MRT", "MR", "478"),
    MAURITIUS("Mauritius", "the Republic of Mauritius", "MUS", "MU", "480"),
    MEXICO("Mexico", "the United Mexican States", "MEX", "MX", "484"),
    MICRONESIA_FEDERATED_STATES_OF("Micronesia (Federated States of)", "the Federated States of Micronesia", "FSM", "FM", "583"),
    MONACO("Monaco", "the Principality of Monaco", "MCO", "MC", "492"),
    MONGOLIA("Mongolia", "Mongolia", "MNG", "MN", "496"),
    MONTENEGRO("Montenegro", "Montenegro", "MNE", "ME", "499"),
    MOROCCO("Morocco", "the Kingdom of Morocco", "MAR", "MA", "504"),
    MOZAMBIQUE("Mozambique", "the Republic of Mozambique", "MOZ", "MZ", "508"),
    MYANMAR("Myanmar", "the Republic of the Union of Myanmar", "MMR", "MM", "104"),
	NAMIBIA("Namibia", "the Republic of Namibia", "NAM", "NA", "516"),
    NAURU("Nauru", "the Republic of Nauru", "NRU", "NR", "520"),
    NEPAL("Nepal", "the Federal Democratic Republic of Nepal", "NPL", "NP", "524"),
    NETHERLANDS("Netherlands", "the Kingdom of the Netherlands", "NLD", "NL", "528"),
    NEW_ZEALAND("New Zealand", "New Zealand", "NZL", "NZ", "554"),
    NICARAGUA("Nicaragua", "the Republic of Nicaragua", "NIC", "NI", "558"),
    NIGER("Niger", "the Republic of the Niger", "NER", "NE", "562"),
    NIGERIA("Nigeria", "the Federal Republic of Nigeria", "NGA", "NG", "566"),
    NIUE("Niue", "Niue", "NIU", "NU", "570"),
    NORTH_MACEDONIA("North Macedonia", "the Republic of North Macedonia", "MKD", "MK", "807"),
    NORWAY("Norway", "the Kingdom of Norway", "NOR", "NO", "578"),
    OMAN("Oman", "the Sultanate of Oman", "OMN", "OM", "512"),
    PAKISTAN("Pakistan", "the Islamic Republic of Pakistan", "PAK", "PK", "586"),
    PALAU("Palau", "the Republic of Palau", "PLW", "PW", "585"),
    PANAMA("Panama", "the Republic of Panama", "PAN", "PA", "591"),
    PAPUA_NEW_GUINEA("Papua New Guinea", "Independent State of Papua New Guinea", "PNG", "PG", "598"),
    PARAGUAY("Paraguay", "the Republic of Paraguay", "PRY", "PY", "600"),
    PERU("Peru", "the Republic of Peru", "PER", "PE", "604"),
    PHILIPPINES("Philippines", "the Republic of the Philippines", "PHL", "PH", "608"),
    POLAND("Poland", "the Republic of Poland", "POL", "PL", "616"),
    PORTUGAL("Portugal", "the Portuguese Republic", "PRT", "PT", "620"),
    QATAR("Qatar", "the State of Qatar", "QAT", "QA", "634"),
    REPUBLIC_OF_KOREA("Republic of Korea", "the Republic of Korea", "KOR", "KR", "410"),
    REPUBLIC_OF_MOLDOVA("Moldova", "the Republic of Moldova", "MDA", "MD", "498"),
    ROMANIA("Romania", "Romania", "ROU", "RO", "642"),
    RUSSIAN_FEDERATION("Russian Federation", "the Russian Federation", "RUS", "RU", "Russia", "643"),
    RWANDA("Rwanda", "the Republic of Rwanda", "RWA", "RW", "646"),
    SAINT_KITTS_AND_NEVIS("Saint Kitts and Nevis", "Saint Kitts and Nevis", "KNA", "KN", "659"),
    SAINT_LUCIA("Saint Lucia", "Saint Lucia", "LCA", "LC", "662"),
    SAINT_VINCENT_AND_THE_GRENADINES("Saint Vincent and the Grenadines", "Saint Vincent and the Grenadines", "VCT", "VC", "670"),
    SAMOA("Samoa", "the Independent State of Samoa", "WSM", "WS", "882"),
    SAN_MARINO("San Marino", "the Republic of San Marino", "SMR", "SM", "674"),
    SAO_TOME_AND_PRINCIPE("Sao Tome and Principe", "the Democratic Republic of Sao Tome and Principe", "STP", "ST", "678"),
    SAUDI_ARABIA("Saudi Arabia", "the Kingdom of Saudi Arabia", "SAU", "SA", "682"),
    SENEGAL("Senegal", "the Republic of Senegal", "SEN", "SN", "686"),
    SERBIA("Serbia", "the Republic of Serbia", "SRB", "RS", "688"),
    SEYCHELLES("Seychelles", "the Republic of Seychelles", "SYC", "SC", "690"),
    SIERRA_LEONE("Sierra Leone", "the Republic of Sierra Leone", "SLE", "SL", "694"),
    SINGAPORE("Singapore", "the Republic of Singapore", "SGP", "SG", "702"),
    SLOVAKIA("Slovakia", "the Slovak Republic", "SVK", "SK", "703"),
    SLOVENIA("Slovenia", "the Republic of Slovenia", "SVN", "SI", "705"),
    SOLOMON_ISLANDS("Solomon Islands", "Solomon Islands", "SLB", "SB", "090"),
    SOMALIA("Somalia", "the Federal Republic of Somalia", "SOM", "SO", "706"),
    SOUTH_AFRICA("South Africa", "the Republic of South Africa", "ZAF", "ZA", "710"),
    SOUTH_SUDAN("South Sudan", "the Republic of South Sudan", "SSD", "SS", "728"),
    SPAIN("Spain", "the Kingdom of Spain", "ESP", "ES", "724"),
    SRI_LANKA("Sri Lanka", "the Democratic Socialist Republic of Sri Lanka", "LKA", "LK", "144"),
    SUDAN("Sudan", "the Republic of the Sudan", "SDN", "SD", "729"),
    SURINAME("Suriname", "the Republic of Suriname", "SUR", "SR", "740"),
    SWEDEN("Sweden", "the Kingdom of Sweden", "SWE", "SE", "752"),
    SWITZERLAND("Switzerland", "the Swiss Confederation", "CHE", "CH", "756"),
    SYRIAN_ARAB_REPUBLIC("Syrian Arab Republic", "the Syrian Arab Republic", "SYR", "SY", "760"),
    TAJIKISTAN("Tajikistan", "the Republic of Tajikistan", "TJK", "TJ", "762"),
    THAILAND("Thailand", "the Kingdom of Thailand", "THA", "TH", "764"),
    TIMORLESTE("Timor-Leste", "the Democratic Republic of Timor-Leste", "TLS", "TL", "626"),
    TOGO("Togo", "the Togolese Republic", "TGO", "TG", "768"),
    TOKELAU_("Tokelau", "Tokelau", "TKL", "TK", "772"),
    TONGA("Tonga", "the Kingdom of Tonga", "TON", "TO", "776"),
    TRINIDAD_AND_TOBAGO("Trinidad and Tobago", "the Republic of Trinidad and Tobago", "TTO", "TT", "780"),
    TUNISIA("Tunisia", "the Republic of Tunisia", "TUN", "TN", "788"),
    TURKEY("Turkey", "the Republic of Turkey", "TUR", "TR", "792"),
    TURKMENISTAN("Turkmenistan", "Turkmenistan", "TKM", "TM", "795"),
    TUVALU("Tuvalu", "Tuvalu", "TUV", "TV", "798"),
    UGANDA("Uganda", "the Republic of Uganda", "UGA", "UG", "800"),
    UKRAINE("Ukraine", "Ukraine", "UKR", "UA", "804"),
    UNITED_ARAB_EMIRATES("United Arab Emirates", "the United Arab Emirates", "ARE", "AE", "784"),
    UNITED_KINGDOM_OF_GREAT_BRITAIN_AND_NORTHERN_IRELAND("United Kingdom of Great Britain and Northern Ireland","the United Kingdom of Great Britain and Northern Ireland","GBR", "GB", "United Kingdom", "826"),
    UNITED_REPUBLIC_OF_TANZANIA("United Republic of Tanzania", "the United Republic of Tanzania", "TZA", "TZ", "834"),
    UNITED_STATES_OF_AMERICA("United States of America","the United States of America","United States","USA", "US", "840"),
    URUGUAY("Uruguay", "the Eastern Republic of Uruguay", "URY", "UY", "858"),
    UZBEKISTAN("Uzbekistan", "the Republic of Uzbekistan", "UZB", "UZ", "860"),
    VANUATU("Vanuatu", "the Republic of Vanuatu", "VUT", "VU", "548"),
    VENEZUELA_BOLIVARIAN_REPUBLIC_OF("Venezuela (Bolivarian Republic of)", "the Bolivarian Republic of Venezuela", "VEN", "VE", "862"),
    VIET_NAM("Viet Nam", "the Socialist Republic of Viet Nam", "VNM", "VN", "704"),
    YEMEN("Yemen", "the Republic of Yemen", "YEM", "YE", "887"),
    ZAMBIA("Zambia", "the Republic of Zambia", "ZMB", "ZM", "894"),
    ZIMBABWE("Zimbabwe", "the Republic of Zimbabwe", "ZWE", "ZW", "716");


    private String shortName;
    private String officialName;
    private String iso3;
    private String iso2;
    private String[] synonyms;

    public String[] getSynonyms() {
	return Arrays.copyOf(synonyms, synonyms.length - 1);
    }

    public void setSynonyms(String[] synonyms) {
	this.synonyms = synonyms;
    }

    public String getShortName() {
	return shortName;
    }

    public String getOfficialName() {
	return officialName;
    }

    public String getISO3() {
	return iso3;
    }

    public String getISO2() {
	return iso2;
    }
    
    public String getNumericCode() { 
	return synonyms[synonyms.length - 1]; 
    }

    private Country(String shortName, String officialName, String iso3, String iso2, String... synonyms) {
	this.shortName = shortName;
	this.officialName = officialName;
	this.iso3 = iso3;
	this.iso2 = iso2;
	this.synonyms = synonyms;
    }

    public static Country decode(String country) {
	if (country == null) {
	    return null;
	}
	for (Country c : values()) {
	    if (country.equalsIgnoreCase(c.getShortName()) || country.equalsIgnoreCase(c.getOfficialName())
		    || country.equalsIgnoreCase(c.getISO3()) || country.equalsIgnoreCase(c.getISO2())) {
		return c;
	    }
	}
	for (Country c : values()) {
	    String[] ss = c.getSynonyms();
	    if (ss != null) {
		for (String s : ss) {
		    if (s.equalsIgnoreCase(country)) {
			return c;
		    }
		}
	    }
	}
	if (country.contains("-")) {
	    // e.g. IT - Italy
	    String[] split = country.split("-");
	    Country c = decode(split[0].trim());
	    if (c != null) {
		return c;
	    }
	    c = decode(split[1].trim());
	    if (c != null) {
		return c;
	    }
	}
	return null;
    }

    public static void main(String[] args) {
	Country us = Country.decode("US");
	System.out.println(us.getShortName());
    }
}
