package eu.essi_lab.gssrv.conf.task.bluecloud;

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

public class BlueCloudElements {

    public static BlueCloudMetadataElement[] getOptionalMetadataElements() {
	return new BlueCloudMetadataElement[] { //
		BlueCloudMetadataElement.KEYWORD_TYPE, //
		BlueCloudMetadataElement.CRUISE, //
		BlueCloudMetadataElement.CRUISE_URI, //
		BlueCloudMetadataElement.PROJECT, //
		BlueCloudMetadataElement.PROJECT_URI, //
	};
    }

    public static BlueCloudMetadataElement[] getCoreMetadataElements() {
	return new BlueCloudMetadataElement[] { //
		BlueCloudMetadataElement.IDENTIFIER, //
		BlueCloudMetadataElement.TITLE, //
		BlueCloudMetadataElement.KEYWORD, //
		BlueCloudMetadataElement.KEYWORD_URI, //
		BlueCloudMetadataElement.BOUNDING_BOX, //
		BlueCloudMetadataElement.TEMPORAL_EXTENT, //
		BlueCloudMetadataElement.PARAMETER, //
		BlueCloudMetadataElement.PARAMETER_URI, //
		BlueCloudMetadataElement.INSTRUMENT, //
		BlueCloudMetadataElement.INSTRUMENT_URI, //
		BlueCloudMetadataElement.PLATFORM, //
		BlueCloudMetadataElement.PLATFORM_URI, //
		BlueCloudMetadataElement.ORGANIZATION, //
		BlueCloudMetadataElement.ORGANIZATION_URI, //
		BlueCloudMetadataElement.ORGANIZATION_ROLE, //
		BlueCloudMetadataElement.DATESTAMP, //
		BlueCloudMetadataElement.REFERENCE_DATE, //
		BlueCloudMetadataElement.RESOURCE_IDENTIFIER//
	};
    }
}
