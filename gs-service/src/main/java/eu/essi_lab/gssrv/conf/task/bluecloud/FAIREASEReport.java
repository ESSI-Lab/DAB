package eu.essi_lab.gssrv.conf.task.bluecloud;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.essi_lab.lib.net.s3.S3TransferWrapper;

public class FAIREASEReport extends MetadataReport {

	public FAIREASEReport(S3TransferWrapper manager, boolean uploadToS3, Integer maxRecords, String[] viewIdentifiers)
			throws Exception {
		super(manager, uploadToS3, maxRecords, viewIdentifiers);
	}

	public static enum FAIREASEViews implements ReportViews {
		SEADATANET_OPEN("seadatanet-open", "SeaDataNet Open"), //
		SEADATANET_PRODUCTS("seadatanet-products", "SeaDataNet Products"), //
		EMODNET_CHEMISTRY("emodnet-chemistry", "EMODnet Chemistry"), //
		ARGO("argo", "ARGO"), //
		EUROBIS("eurobis", "EurOBIS"), //
		// ECOTAXA("ecotaxa", "EcoTaxa"), //
		ELIXIR_ENA("elixir-ena", "ELIXIR-ENA"), //
		WEKEO("wekeo", "WEKEO"), //
		// ICOS("icos-marine", "ICOS Marine"), //
		ICOS_SOCAT("icos-socat", "ICOS SOCAT"), //
		ICOS_DATA_PORTAL("icos-data-portal", "ICOS Data Portal"), //
		CMEMS("marineID", "Copernicus Marine Environment Monitoring Service (CMEMS)"), //
		VITO("FROMREGISTRY--regprefseparator--registrytestid1--regprefseparator--dad0859e-206c-480b-a080-d4eea70ee22d",
				"VITO/Copernicus Global Land Services"), //
		US_NODC("usnodcdbid", "US NODC Collections"), //
		EEA("UUID-456602db-4275-4410-8b68-436fd23ace69", "European Environment Agency SDI Catalog");// ;

		private String label;

		public String getLabel() {
			return label;
		}

		public String getId() {
			return view;
		}

		private String view;

		FAIREASEViews(String view, String label) {
			this.label = label;
			this.view = view;
		}

	}

	public String getS3Folder() {
		return "FAIREASE";
	}

	public String getHostname() {
		return "gs-service-production.geodab.eu";
	}

	public String getProjectName() {
		return "FAIR-EASE";
	}

	public String getReportFilename() {
		return "FAIR_EASE_Report";
	}

	public static void main(String[] args) throws Exception {
		new FAIREASEReport(null, false, 200, new String[] {});
	}

	public List<ReportViews> getViews() {
		return new ArrayList<ReportViews>(Arrays.asList(FAIREASEViews.values()));
	}

	@Override
	protected String getPostRequest() {
		return "blueCloudPostRequest.xml";
	}

	@Override
	public MetadataElement[] getCoreMetadataElements() {
		return new MetadataElement[] { //
				MetadataElement.IDENTIFIER, //
				MetadataElement.TITLE, //
				MetadataElement.KEYWORD, //
				MetadataElement.BOUNDING_BOX, //
				MetadataElement.TEMPORAL_EXTENT, //
				MetadataElement.PARAMETER, //
				MetadataElement.PARAMETER_URI, //
				MetadataElement.INSTRUMENT, //
				MetadataElement.INSTRUMENT_URI, //
				MetadataElement.PLATFORM, //
				MetadataElement.PLATFORM_URI, //
				MetadataElement.ORGANIZATION, //
				MetadataElement.ORGANIZATION_URI, //
				MetadataElement.DATESTAMP, //
				MetadataElement.REVISION_DATE, //
				MetadataElement.RESOURCE_IDENTIFIER };
	}

	@Override
	public MetadataElement[] getOptionalMetadataElements() {
		return new MetadataElement[] { //
				MetadataElement.KEYWORD_TYPE, //
				MetadataElement.CRUISE, //
				MetadataElement.CRUISE_URI, //
				MetadataElement.PROJECT, //
				MetadataElement.PROJECT_URI, //
		};
	}

}
