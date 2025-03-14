/**
 * 
 */
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class SOSRequestBuilder {

	protected String serviceUrl;
	protected String version;

	/**
	 * 
	 */
	public SOSRequestBuilder(String serviceUrl, String version) {

		this.serviceUrl = serviceUrl;
		this.version = version;
	}

	/**
	 * @return
	 */
	public String createBaseEndpoint() {

		if (serviceUrl.startsWith("www")) {
			serviceUrl = "http://" + serviceUrl;
		}

		if (!serviceUrl.endsWith("?") && !serviceUrl.endsWith("&")) {
			if (serviceUrl.contains("?")) {
				return serviceUrl + "&";
			} else {
				return serviceUrl + "?";
			}
		}

		return serviceUrl;
	}

	/**
	 * @return
	 */
	public String createBaseRequest() {
		String baseEndpoint = createBaseEndpoint();
		return baseEndpoint + "SERVICE=SOS&VERSION=" + version;
	}

	/**
	 * @return
	 */
	public String createCapabilitiesRequest() {
		String baseEndpoint = createBaseRequest();
		String ret = baseEndpoint + "&REQUEST=GetCapabilities";
		return ret;

	}

	/**
	 * @param endpoint
	 * @param procedure
	 * @return
	 */
	public String createFeaturesRequest(String procedure) {
		String baseEndpoint = createBaseRequest();
		return baseEndpoint + "&REQUEST=GetFeatureOfInterest&procedure=" + encode(procedure);
	}

	/**
	 * @param endpoint
	 * @param procedure
	 * @param procedureFormat
	 * @return
	 */
	public String createProcedureDescriptionRequest(String procedure, String procedureFormat) {
		String baseEndpoint = createBaseRequest();

		String format = version.equals("1.0.0") ? "outputFormat" : "procedureDescriptionFormat";

		return baseEndpoint + "&REQUEST=DescribeSensor&procedure=" + encode(procedure) + //
				"&" + format + "=" + //
				encode(procedureFormat);
	}

	/**
	 * @param procedure
	 * @param feature
	 * @return
	 */
	public String createDataAvailabilityRequest(String procedure, String feature) {

		return createDataAvailabilityRequest(procedure, feature, null);
	}

	/**
	 * @param procedure
	 * @param feature
	 * @param observedProperty
	 * @return
	 */
	public String createDataAvailabilityRequest(String procedure, String feature, String observedProperty) {
		String baseEndpoint = createBaseRequest();
		String propertyParameter = "";
		String featureParameter = "";
		if (feature != null) {
			featureParameter = "&featureOfInterest=" + encode(feature);
		}
		if (observedProperty != null) {
			propertyParameter = "&observedProperty=" + encode(observedProperty);
		}
		return baseEndpoint + "&REQUEST=GetDataAvailability&procedure=" + encode(procedure) + propertyParameter
				+ featureParameter;
	}

	/**
	 * @param procedure
	 * @param featureIdentifier
	 * @param property
	 * @param begin
	 * @param end
	 * @return
	 */
	public String createDataRequest(String procedure, String featureIdentifier, String property, Date begin, Date end) {
		String baseEndpoint = createBaseRequest();
		String temporalFilter = "";
		if (begin != null && end != null) {

			String beginStr = ISO8601DateTimeUtils.getISO8601DateTime(begin);

			String filter = "om:phenomenonTime,";
			if (version.equals("1.0.0")) {
				filter = "";
			}

			if (end.equals(begin)) {

				filter += beginStr; // an instant has been required

			} else {

				String endStr = ISO8601DateTimeUtils.getISO8601DateTime(end);
				filter += beginStr + "/" + endStr; // a period has been required
			}

			if (version.equals("1.0.0")) {

				temporalFilter = "&eventtime=" + encode(filter);

			} else {

				temporalFilter = "&temporalFilter=" + encode(filter);
			}
		}

		if (version.equals("1.0.0")) {

			return baseEndpoint + //
					"&REQUEST=GetObservation" + //
					"&responseFormat=" + encode("text/xml;subtype=\"om/1.0.0\"") + //
					"&offering=" + encode(featureIdentifier) + //
					"&observedProperty=" + encode(property) + //
					"&procedure=" + encode(procedure) + //
					temporalFilter;
		}

		return baseEndpoint + //
				"&REQUEST=GetObservation" + //
				"&featureOfInterest=" + encode(featureIdentifier) + //
				"&observedProperty=" + encode(property) + //
				"&procedure=" + encode(procedure) + //
				temporalFilter;
	}

	/**
	 * @param url
	 * @return
	 */
	protected String removeCredentialsInRequests(String url) {
		return url;
	}

	/**
	 * @param url
	 * @return
	 */
	protected String addCredentialsInRequests(String url) {
		return url;
	}

	/**
	 * @param str
	 * @return
	 */
	protected String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

}
