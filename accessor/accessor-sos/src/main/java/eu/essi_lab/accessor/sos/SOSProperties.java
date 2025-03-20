package eu.essi_lab.accessor.sos;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SOSProperties {

	Properties properties = new Properties();

	public enum SOSProperty {

		// PROCEDURE
		PROCEDURE_TITLE, //
		PROCEDURE_HREF, //
		PROCEDURE_IDENTIFIER, //
		PROCEDURE_TIME_INTERPOLATION, //
		PROCEDURE_TIME_RESOLUTION, //
		PROCEDURE_TIME_AGGREGATION, //

		// FROM CAPABILITIES
		OFFERING, //
		SERVICE_PROVIDER_NAME, //
		SERVICE_PROVIDER_SITE, //
		SERVICE_PROVIDER_ROLE, //
		SERVICE_PROVIDER_IndividualName, //
		SERVICE_PROVIDER_PositionName, //
		SERVICE_PROVIDER_Phone, //
		SERVICE_PROVIDER_AddressDeliveryPoint, //
		SERVICE_PROVIDER_AddressCity, //
		SERVICE_PROVIDER_AddressAdministrativeArea, //
		SERVICE_PROVIDER_AddressPostalCode, //
		SERVICE_PROVIDER_AddressCountry, //
		SERVICE_PROVIDER_AddressEmailAddress, //
		SERVICE_PROVIDER_Keywords, //
		ELEVATION, //

		// FEATURE OF INTEREST
		FOI_ID, //
		FOI_NAME, //
		FOI_COUNTRY, //

		// TEMPORAL EXTENT
		TEMP_EXTENT_BEGIN, //
		TEMP_EXTENT_END, //

		// SPATIAL EXTENT
		LATITUDE, //
		LONGITUDE, //

		// OBSERVED PROPERTY
		OBSERVED_PROPERTY_ID, //
		OBSERVED_PROPERTY_NAME, //
		OBSERVED_PROPERTY_UOM_CODE, //
		OBSERVED_PROPERTY_UOM_HREF, //
		OBSERVED_PROPERTY_UOM_TITLE, //

		// SENSOR PROPERTIES
		SENSOR_Name, //
		SENSOR_Description, //
		SENSOR_Keywords, //
		SENSOR_UniqueId, //
		SENSOR_ManufacturerName, //
		SENSOR_ShortName, //
		SENSOR_LongName, //
		SENSOR_Material, //
		SENSOR_Status, //
		SENSOR_Mobile, //
		SENSOR_ContactManufacturerOrganization, //
		SENSOR_ContactOwnerOrganization, //
		SENSOR_ContactOwnerPhone, //
		SENSOR_ContactOwnerAddressDeliveryPoint, //
		SENSOR_ContactOwnerAddressCity, //
		SENSOR_ContactOwnerAddressCountry, //
		SENSOR_ContactOwnerAddressEmail, //
		SENSOR_ContactOwnerHomepage, //
		SENSOR_DocumentationImage, //
		SENSOR_DocumentationImageDescription, //
		SENSOR_DocumentationImageFormat, //
		DOWNLOAD_PROTOCOL;//
	}

	public SOSProperties(String str) throws IOException {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		InputStream stream = new ByteArrayInputStream(bytes);
		this.properties.load(stream);
	}

	public SOSProperties() {
	}

	public SOSProperties(Properties news) {
		this.properties = news;
	}

	public String asString() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		properties.store(baos, null);
		byte[] bytes = baos.toByteArray();
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public void setProperty(SOSProperty property, String value) {
		if (value != null && !value.equals("")) {
			properties.setProperty(property.name(), value);
		}
	}

	public String getProperty(SOSProperty property) {
		return properties.getProperty(property.name());
	}

	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder();

		properties.keySet().//
				stream().//
				sorted().//
				forEach(key -> {

					String property = properties.getProperty(key.toString());

					if (property != null) {
						builder.append(key);
						builder.append(":");
						builder.append(properties.getProperty(key.toString()));
						builder.append("\n");
					}
				});

		return builder.toString();
	}

	@Override
	public SOSProperties clone() throws CloneNotSupportedException {
		Properties news = new Properties();
		news.putAll(properties);
		SOSProperties ret = new SOSProperties(news);
		return ret;
	}
}
