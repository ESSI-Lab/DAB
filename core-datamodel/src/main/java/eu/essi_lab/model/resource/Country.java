package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
    AFGHANISTAN("Afghanistan", "the Islamic Republic of Afghanistan", "AFG", "AF"), //
    ALBANIA("Albania", "the Republic of Albania", "ALB", "AL"), //
    ALGERIA("Algeria", "the People's Democratic Republic of Algeria", "DZA", "DZ"), //
    ANDORRA("Andorra", "the Principality of Andorra", "AND", "AD"), //
    ANGOLA("Angola", "the Republic of Angola", "AGO", "AO"), //
    ANTIGUA_AND_BARBUDA("Antigua and Barbuda", "Antigua and Barbuda", "ATG", "AG"), //
    ARGENTINA("Argentina", "the Argentine Republic", "ARG", "AR"), //
    ARMENIA("Armenia", "the Republic of Armenia", "ARM", "AM"), //
    AUSTRALIA("Australia", "Australia", "AUS", "AU"), //
    AUSTRIA("Austria", "the Republic of Austria", "AUT", "AT"), //
    AZERBAIJAN("Azerbaijan", "the Republic of Azerbaijan", "AZE", "AZ"), //
    BAHAMAS("Bahamas", "the Commonwealth of the Bahamas", "BHS", "BS"), //
    BAHRAIN("Bahrain", "the Kingdom of Bahrain", "BHR", "BH"), //
    BANGLADESH("Bangladesh", "the People's Republic of Bangladesh", "BGD", "BD"), //
    BARBADOS("Barbados", "Barbados", "BRB", "BB"), //
    BELARUS("Belarus", "the Republic of Belarus", "BLR", "BY"), //
    BELGIUM("Belgium", "the Kingdom of Belgium", "BEL", "BE"), //
    BELIZE("Belize", "Belize", "BLZ", "BZ"), //
    BENIN("Benin", "the Republic of Benin", "BEN", "BJ"), //
    BHUTAN("Bhutan", "the Kingdom of Bhutan", "BTN", "BT"), //
    BOLIVIA_PLURINATIONAL_STATE_OF("Bolivia (Plurinational State of)", "the Plurinational State of Bolivia", "BOL", "BO"), //
    BOSNIA_AND_HERZEGOVINA("Bosnia and Herzegovina", "Bosnia and Herzegovina", "BIH", "BA"), //
    BOTSWANA("Botswana", "the Republic of Botswana", "BWA", "BW"), //
    BRAZIL("Brazil", "the Federative Republic of Brazil", "BRA", "BR"), //
    BRUNEI_DARUSSALAM("Brunei Darussalam", "Brunei Darussalam", "BRN", "BN"), //
    BULGARIA("Bulgaria", "the Republic of Bulgaria", "BGR", "BG"), //
    BURKINA_FASO("Burkina Faso", "Burkina Faso", "BFA", "BF"), //
    BURUNDI("Burundi", "the Republic of Burundi", "BDI", "BI"), //
    CABO_VERDE("Cabo Verde", "the Republic of Cabo Verde", "CPV", "CV"), //
    CAMBODIA("Cambodia", "the Kingdom of Cambodia", "KHM", "KH"), //
    CAMEROON("Cameroon", "the Republic of Cameroon", "CMR", "CM"), //
    CANADA("Canada", "Canada", "CAN", "CA"), //
    CENTRAL_AFRICAN_REPUBLIC("Central African Republic", "the Central African Republic", "CAF", "CF"), //
    CHAD("Chad", "the Republic of Chad", "TCD", "TD"), //
    CHILE("Chile", "the Republic of Chile", "CHL", "CL"), //
    CHINA("China", "the People's Republic of China", "CHN", "CN"), //
    COLOMBIA("Colombia", "the Republic of Colombia", "COL", "CO"), //
    COMOROS("Comoros", "the Union of the Comoros", "COM", "KM"), //
    CONGO("Congo", "the Republic of the Congo", "COG", "CG"), //
    COOK_ISLANDS("Cook Islands", "the Cook Islands ", "COK", "CK"), //
    COSTA_RICA("Costa Rica", "the Republic of Costa Rica", "CRI", "CR"), //
    CROATIA("Croatia", "the Republic of Croatia", "HRV", "HR"), //
    CUBA("Cuba", "the Republic of Cuba", "CUB", "CU"), //
    CYPRUS("Cyprus", "the Republic of Cyprus", "CYP", "CY"), //
    CZECHIA("Czechia", "the Czech Republic", "CZE", "CZ"), //
    CÔTE_DIVOIRE("Côte d'Ivoire", "the Republic of Côte d'Ivoire", "CIV", "CI"), //
    DEMOCRATIC_PEOPLES_REPUBLIC_OF_KOREA("Democratic People's Republic of Korea", "the Democratic People's Republic of Korea", "PRK", "KP"), //
    DEMOCRATIC_REPUBLIC_OF_THE_CONGO("Democratic Republic of the Congo", "the Democratic Republic of the Congo", "COD", "CD"), //
    DENMARK("Denmark", "the Kingdom of Denmark", "DNK", "DK"), //
    DJIBOUTI("Djibouti", "the Republic of Djibouti", "DJI", "DJ"), //
    DOMINICA("Dominica", "the Commonwealth of Dominica", "DMA", "DM"), //
    DOMINICAN_REPUBLIC("Dominican Republic", "the Dominican Republic", "DOM", "DO"), //
    ECUADOR("Ecuador", "the Republic of Ecuador", "ECU", "EC"), //
    EGYPT("Egypt", "the Arab Republic of Egypt", "EGY", "EG"), //
    EL_SALVADOR("El Salvador", "the Republic of El Salvador", "SLV", "SV"), //
    EQUATORIAL_GUINEA("Equatorial Guinea", "the Republic of Equatorial Guinea", "GNQ", "GQ"), //
    ERITREA("Eritrea", "the State of Eritrea", "ERI", "ER"), //
    ESTONIA("Estonia", "the Republic of Estonia", "EST", "EE"), //
    ESWATINI("Eswatini", "the Kingdom of Eswatini ", "SWZ", "SZ"), //
    ETHIOPIA("Ethiopia", "the Federal Democratic Republic of Ethiopia", "ETH", "ET"), //
    FAROE_ISLANDS_("Faroe Islands ", "the Faroe Islands", "FRO", "FO"), //
    FIJI("Fiji", "the Republic of Fiji", "FJI", "FJ"), //
    FINLAND("Finland", "the Republic of Finland", "FIN", "FI"), //
    FRANCE("France", "the French Republic", "FRA", "FR"), //
    GABON("Gabon", "the Gabonese Republic", "GAB", "GA"), //
    GAMBIA("Gambia", "the Republic of the Gambia", "GMB", "GM"), //
    GEORGIA("Georgia", "Georgia", "GEO", "GE"), //
    GERMANY("Germany", "the Federal Republic of Germany", "DEU", "DE"), //
    GHANA("Ghana", "the Republic of Ghana", "GHA", "GH"), //
    GREECE("Greece", "the Hellenic Republic", "GRC", "GR"), //
    GRENADA("Grenada", "Grenada", "GRD", "GD"), //
    GUATEMALA("Guatemala", "the Republic of Guatemala", "GTM", "GT"), //
    GUINEA("Guinea", "the Republic of Guinea", "GIN", "GN"), //
    GUINEABISSAU("Guinea-Bissau", "the Republic of Guinea-Bissau", "GNB", "GW"), //
    GUYANA("Guyana", "the Co-operative Republic of Guyana", "GUY", "GY"), //
    HAITI("Haiti", "the Republic of Haiti", "HTI", "HT"), //
    HONDURAS("Honduras", "the Republic of Honduras", "HND", "HN"), //
    HUNGARY("Hungary", "Hungary", "HUN", "HU"), //
    ICELAND("Iceland", "the Republic of Iceland", "ISL", "IS"), //
    INDIA("India", "the Republic of India", "IND", "IN"), //
    INDONESIA("Indonesia", "the Republic of Indonesia", "IDN", "ID"), //
    IRAN_ISLAMIC_REPUBLIC_OF("Iran (Islamic Republic of)", "the Islamic Republic of Iran", "IRN", "IR"), //
    IRAQ("Iraq", "the Republic of Iraq", "IRQ", "IQ"), //
    IRELAND("Ireland", "Ireland", "IRL", "IE"), //
    ISRAEL("Israel", "the State of Israel", "ISR", "IL"), //
    ITALY("Italy", "the Republic of Italy", "ITA", "IT"), //
    JAMAICA("Jamaica", "Jamaica", "JAM", "JM"), //
    JAPAN("Japan", "Japan", "JPN", "JP"), //
    JORDAN("Jordan", "the Hashemite Kingdom of Jordan", "JOR", "JO"), //
    KAZAKHSTAN("Kazakhstan", "the Republic of Kazakhstan", "KAZ", "KZ"), //
    KENYA("Kenya", "the Republic of Kenya", "KEN", "KE"), //
    KIRIBATI("Kiribati", "the Republic of Kiribati", "KIR", "KI"), //
    KUWAIT("Kuwait", "the State of Kuwait", "KWT", "KW"), //
    KYRGYZSTAN("Kyrgyzstan", "the Kyrgyz Republic", "KGZ", "KG"), //
    LAO_PEOPLES_DEMOCRATIC_REPUBLIC("Lao People's Democratic Republic", "the Lao People's Democratic Republic", "LAO", "LA"), //
    LATVIA("Latvia", "the Republic of Latvia", "LVA", "LV"), //
    LEBANON("Lebanon", "the Lebanese Republic", "LBN", "LB"), //
    LESOTHO("Lesotho", "the Kingdom of Lesotho", "LSO", "LS"), //
    LIBERIA("Liberia", "the Republic of Liberia", "LBR", "LR"), //
    LIBYA("Libya", "the State of Libya", "LBY", "LY"), //
    LITHUANIA("Lithuania", "the Republic of Lithuania", "LTU", "LT"), //
    LUXEMBOURG("Luxembourg", "the Grand Duchy of Luxembourg", "LUX", "LU"), //
    MADAGASCAR("Madagascar", "the Republic of Madagascar", "MDG", "MG"), //
    MALAWI("Malawi", "the Republic of Malawi", "MWI", "MW"), //
    MALAYSIA("Malaysia", "Malaysia", "MYS", "MY"), //
    MALDIVES("Maldives", "the Republic of Maldives", "MDV", "MV"), //
    MALI("Mali", "the Republic of Mali", "MLI", "ML"), //
    MALTA("Malta", "the Republic of Malta", "MLT", "MT"), //
    MARSHALL_ISLANDS("Marshall Islands", "the Republic of the Marshall Islands", "MHL", "MH"), //
    MAURITANIA("Mauritania", "the Islamic Republic of Mauritania", "MRT", "MR"), //
    MAURITIUS("Mauritius", "the Republic of Mauritius", "MUS", "MU"), //
    MEXICO("Mexico", "the United Mexican States", "MEX", "MX"), //
    MICRONESIA_FEDERATED_STATES_OF("Micronesia (Federated States of)", "the Federated States of Micronesia", "FSM", "FM"), //
    MONACO("Monaco", "the Principality of Monaco", "MCO", "MC"), //
    MONGOLIA("Mongolia", "Mongolia", "MNG", "MN"), //
    MONTENEGRO("Montenegro", "Montenegro", "MNE", "ME"), //
    MOROCCO("Morocco", "the Kingdom of Morocco", "MAR", "MA"), //
    MOZAMBIQUE("Mozambique", "the Republic of Mozambique", "MOZ", "MZ"), //
    MYANMAR("Myanmar", "the Republic of the Union of Myanmar", "MMR", "MM"), //
    NAMIBIA("Namibia", "the Republic of Namibia", "NAM", "NA"), //
    NAURU("Nauru", "the Republic of Nauru", "NRU", "NR"), //
    NEPAL("Nepal", "the Federal Democratic Republic of Nepal", "NPL", "NP"), //
    NETHERLANDS("Netherlands", "the Kingdom of the Netherlands", "NLD", "NL"), //
    NEW_ZEALAND("New Zealand", "New Zealand", "NZL", "NZ"), //
    NICARAGUA("Nicaragua", "the Republic of Nicaragua", "NIC", "NI"), //
    NIGER("Niger", "the Republic of the Niger", "NER", "NE"), //
    NIGERIA("Nigeria", "the Federal Republic of Nigeria", "NGA", "NG"), //
    NIUE("Niue", "Niue", "NIU", "NU"), //
    NORTH_MACEDONIA("North Macedonia", "the Republic of North Macedonia ", "MKD", "MK"), //
    NORWAY("Norway", "the Kingdom of Norway", "NOR", "NO"), //
    OMAN("Oman", "the Sultanate of Oman", "OMN", "OM"), //
    PAKISTAN("Pakistan", "the Islamic Republic of Pakistan", "PAK", "PK"), //
    PALAU("Palau", "the Republic of Palau", "PLW", "PW"), //
    PANAMA("Panama", "the Republic of Panama", "PAN", "PA"), //
    PAPUA_NEW_GUINEA("Papua New Guinea", "Independent State of Papua New Guinea", "PNG", "PG"), //
    PARAGUAY("Paraguay", "the Republic of Paraguay", "PRY", "PY"), //
    PERU("Peru", "the Republic of Peru", "PER", "PE"), //
    PHILIPPINES("Philippines", "the Republic of the Philippines", "PHL", "PH"), //
    POLAND("Poland", "the Republic of Poland", "POL", "PL"), //
    PORTUGAL("Portugal", "the Portuguese Republic", "PRT", "PT"), //
    QATAR("Qatar", "the State of Qatar", "QAT", "QA"), //
    REPUBLIC_OF_KOREA("Republic of Korea", "the Republic of Korea", "KOR", "KR"), //
    REPUBLIC_OF_MOLDOVA("Moldova", "the Republic of Moldova", "MDA", "MD"), //
    ROMANIA("Romania", "Romania", "ROU", "RO"), //
    RUSSIAN_FEDERATION("Russian Federation", "the Russian Federation", "RUS", "RU","Russia"), //
    RWANDA("Rwanda", "the Republic of Rwanda", "RWA", "RW"), //
    SAINT_KITTS_AND_NEVIS("Saint Kitts and Nevis", "Saint Kitts and Nevis", "KNA", "KN"), //
    SAINT_LUCIA("Saint Lucia", "Saint Lucia", "LCA", "LC"), //
    SAINT_VINCENT_AND_THE_GRENADINES("Saint Vincent and the Grenadines", "Saint Vincent and the Grenadines", "VCT", "VC"), //
    SAMOA("Samoa", "the Independent State of Samoa", "WSM", "WS"), //
    SAN_MARINO("San Marino", "the Republic of San Marino", "SMR", "SM"), //
    SAO_TOME_AND_PRINCIPE("Sao Tome and Principe", "the Democratic Republic of Sao Tome and Principe", "STP", "ST"), //
    SAUDI_ARABIA("Saudi Arabia", "the Kingdom of Saudi Arabia", "SAU", "SA"), //
    SENEGAL("Senegal", "the Republic of Senegal", "SEN", "SN"), //
    SERBIA("Serbia", "the Republic of Serbia", "SRB", "RS"), //
    SEYCHELLES("Seychelles", "the Republic of Seychelles", "SYC", "SC"), //
    SIERRA_LEONE("Sierra Leone", "the Republic of Sierra Leone", "SLE", "SL"), //
    SINGAPORE("Singapore", "the Republic of Singapore", "SGP", "SG"), //
    SLOVAKIA("Slovakia", "the Slovak Republic", "SVK", "SK"), //
    SLOVENIA("Slovenia", "the Republic of Slovenia", "SVN", "SI"), //
    SOLOMON_ISLANDS("Solomon Islands", "Solomon Islands", "SLB", "SB"), //
    SOMALIA("Somalia", "the Federal Republic of Somalia", "SOM", "SO"), //
    SOUTH_AFRICA("South Africa", "the Republic of South Africa", "ZAF", "ZA"), //
    SOUTH_SUDAN("South Sudan", "the Republic of South Sudan", "SSD", "SS"), //
    SPAIN("Spain", "the Kingdom of Spain", "ESP", "ES"), //
    SRI_LANKA("Sri Lanka", "the Democratic Socialist Republic of Sri Lanka", "LKA", "LK"), //
    SUDAN("Sudan", "the Republic of the Sudan", "SDN", "SD"), //
    SURINAME("Suriname", "the Republic of Suriname", "SUR", "SR"), //
    SWEDEN("Sweden", "the Kingdom of Sweden", "SWE", "SE"), //
    SWITZERLAND("Switzerland", "the Swiss Confederation", "CHE", "CH"), //
    SYRIAN_ARAB_REPUBLIC("Syrian Arab Republic", "the Syrian Arab Republic", "SYR", "SY"), //
    TAJIKISTAN("Tajikistan", "the Republic of Tajikistan", "TJK", "TJ"), //
    THAILAND("Thailand", "the Kingdom of Thailand", "THA", "TH"), //
    TIMORLESTE("Timor-Leste", "the Democratic Republic of Timor-Leste", "TLS", "TL"), //
    TOGO("Togo", "the Togolese Republic", "TGO", "TG"), //
    TOKELAU_("Tokelau ", "Tokelau", "TKL", "TK"), //
    TONGA("Tonga", "the Kingdom of Tonga", "TON", "TO"), //
    TRINIDAD_AND_TOBAGO("Trinidad and Tobago", "the Republic of Trinidad and Tobago", "TTO", "TT"), //
    TUNISIA("Tunisia", "the Republic of Tunisia", "TUN", "TN"), //
    TURKEY("Turkey", "the Republic of Turkey", "TUR", "TR"), //
    TURKMENISTAN("Turkmenistan", "Turkmenistan", "TKM", "TM"), //
    TUVALU("Tuvalu", "Tuvalu", "TUV", "TV"), //
    UGANDA("Uganda", "the Republic of Uganda", "UGA", "UG"), //
    UKRAINE("Ukraine", "Ukraine", "UKR", "UA"), //
    UNITED_ARAB_EMIRATES("United Arab Emirates", "the United Arab Emirates", "ARE", "AE"), //
    UNITED_KINGDOM_OF_GREAT_BRITAIN_AND_NORTHERN_IRELAND("United Kingdom of Great Britain and Northern Ireland",
	    "the United Kingdom of Great Britain and Northern Ireland", "GBR", "GB","United Kingdom"), //
    UNITED_REPUBLIC_OF_TANZANIA("United Republic of Tanzania", "the United Republic of Tanzania", "TZA", "TZ"), //
    UNITED_STATES_OF_AMERICA("United States of America", "the United States of America", "USA", "US"), //
    URUGUAY("Uruguay", "the Eastern Republic of Uruguay", "URY", "UY"), //
    UZBEKISTAN("Uzbekistan", "the Republic of Uzbekistan", "UZB", "UZ"), //
    VANUATU("Vanuatu", "the Republic of Vanuatu", "VUT", "VU"), //
    VENEZUELA_BOLIVARIAN_REPUBLIC_OF("Venezuela (Bolivarian Republic of)", "the Bolivarian Republic of Venezuela", "VEN", "VE"), //
    VIET_NAM("Viet Nam", "the Socialist Republic of Viet Nam", "VNM", "VN"), //
    YEMEN("Yemen", "the Republic of Yemen", "YEM", "YE"), //
    ZAMBIA("Zambia", "the Republic of Zambia", "ZMB", "ZM"), //
    ZIMBABWE("Zimbabwe", "the Republic of Zimbabwe", "ZWE", "ZW");

    private String shortName;
    private String officialName;
    private String iso3;
    private String iso2;
    private String[] synonyms;

    public String[] getSynonyms() {
	return synonyms;
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
	return null;
    }
    
    public static void main(String[] args) {
	Country us = Country.decode("US");
	System.out.println(us.getShortName());
    }
}
