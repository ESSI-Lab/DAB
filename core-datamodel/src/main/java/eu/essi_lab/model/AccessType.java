package eu.essi_lab.model;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public enum AccessType {
    /* @formatter:off */

    /**
     * This access type is used when the {@link IOnlineResource} is not recognized.
     */
    UNKNOWN("http://www.essi-lab.eu/broker/accesstypes/unknown", ""),

    /**
     * This access type is used when the {@link IOnlineResource} provides a linkage value that can't be used to download
     * the data.
     * It could be a link to an information site or a report about the dataset.
     */
    NO_ACCESS("http://www.essi-lab.eu/broker/accesstypes/noaccess", ""),

    /**
     * This access type is used when the {@link IOnlineResource} provides:
     * <li>a linkage value which is the "base URL" of the service providing the data</li>
     * <li>an {@link ESSIProtocol} to communicate to the service</li>
     * <li>NO data id on the service</li>
     */
    SERVICE_ACCESS("http://www.essi-lab.eu/broker/accesstypes/service", "download"),

    /**
     * This access type is used when the {@link IOnlineResource} provides a linkage value which can be used "as it is"
     * to download the data. If the direct link points to multiple data, the name element is a comma-separated list of
     * the data IDs.
     */
    @Deprecated
    SIMPLE_ACCESS("http://www.essi-lab.eu/broker/accesstypes/simple", "download"),

    /**
     * This access type is used when the {@link IOnlineResource} provides a linkage value which can be used "as it is"
     * to download the data. If the direct link points to multiple data, the name element is a comma-separated list of
     * the data IDs.
     */
    DIRECT_ACCESS("http://www.essi-lab.eu/broker/accesstypes/direct", "download"),

    /**
     * This access type is used when the {@link IOnlineResource} provides:
     * <li>a linkage value which is the "base URL" of the service providing the data</li>
     * <li>an {@link ESSIProtocol} to communicate to the service</li>
     * <li>the data id on the service</li>
     */
    COMPLEX_ACCESS("http://www.essi-lab.eu/broker/accesstypes/complex", "download");
    /* @formatter:on */

    private String label;
    private String code;

    private AccessType(String s, String c) {
	this.label = s;

	this.code = c;
    }

    /**
     * Decodes the input text and returns the first {@link AccessType} for which
     * {@link AccessType#getDescriptionAnchor()} equals (ignoring case) the provided text.
     * 
     * @return {@link AccessType}
     */
    public static AccessType decode(String text) {
	if (text == null || text.equals("")) {
	    return AccessType.UNKNOWN;
	}

	AccessType[] values = values();
	for (int i = 0; i < values.length; i++) {
	    String v = values[i].getDescriptionAnchor();

	    if (v != null && v.toLowerCase().equals(text.toLowerCase())) {
		return values[i];
	    }

	}

	return AccessType.UNKNOWN;

    }

    public String getDescriptionAnchor() {

	return this.label;
    }

    public String getFunctionCode() {

	return this.code;
    }
}
