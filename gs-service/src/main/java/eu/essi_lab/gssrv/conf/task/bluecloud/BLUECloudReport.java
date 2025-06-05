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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.essi_lab.lib.net.s3.S3TransferWrapper;

public class BLUECloudReport extends MetadataReport {

     
	public BLUECloudReport(S3TransferWrapper manager, boolean uploadToS3, Integer maxRecords, String[] viewIdentifiers)
			throws Exception {
		super(manager, uploadToS3, maxRecords, viewIdentifiers);
	}

	public List<ReportViews> getViews() {
		return new ArrayList<ReportViews>(Arrays.asList(BlueCloudViews.values()));
	}

	protected String getProtocol() {
		return "http";
	}

	public static enum BlueCloudViews implements ReportViews {

		SEADATANET_OPEN("seadatanet-open", "SeaDataNet Open"), //
		SIOS("sios", "Svalbard Integrated Arctic Earth Observing System (SIOS)"),
		SEADATANET_PRODUCTS("seadatanet-products", "SeaDataNet Products"), //
		EMODNET_CHEMISTRY("emodnet-chemistry", "EMODnet Chemistry"), //
		EMODNET_PHYSICS("emodnet-physics", "EMODnet Physics"), //
		EMSO_ERIC("emso-eric", "EMSO ERIC"), //
		ARGO("argo", "ARGO"), //
		EUROBIS("eurobis", "EurOBIS"), //
		EUROBIS_TEST("eurobis-test", "EurOBIS Test"), //
		CMEMS("marineID", "Copernicus Marine Environment Monitoring Service (CMEMS)"),
		WOD("wod", "World Ocean Database"), ECOTAXA("ecotaxa", "EcoTaxa"), //
		ELIXIR_ENA("elixir-ena", "ELIXIR-ENA"), //
		ELIXIR_MGNIFY("elixir-mgnify", "ELIXIR-MGnify"), //
		WEKEO("wekeo", "WEKEO"), //
		ICOS("icos-marine", "ICOS Marine"), //
		ICOS_SOCAT("icos-socat", "ICOS SOCAT"), //
		ICOS_DATA_PORTAL("icos-data-portal", "ICOS Data Portal"), // ;
		ELIXIR_ENA_TEST("elixir-ena-test", "ELIXIR-ENA-TEST");

		private String label;

		public String getLabel() {
			return label;
		}

		public String getId() {
			return view;
		}

		private String view;

		BlueCloudViews(String view, String label) {
			this.label = label;
			this.view = view;
		}

	}

	public String getS3Folder() {
		return "BlueCloud";
	}

	public String getHostname() {
		return "blue-cloud.geodab.eu";
		// return "localhost:9090";
	}

	public String getProjectName() {
		return "Blue-Cloud";
	}

	public String getReportFilename() {
		return "BlueCloudReport";
	}

	@Override
	protected String getPostRequest() {
		return "blueCloudPostRequest.xml";
	}

	@Override
	public BlueCloudMetadataElement[] getCoreMetadataElements() {
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
				BlueCloudMetadataElement.REVISION_DATE, //
				BlueCloudMetadataElement.RESOURCE_IDENTIFIER//
		};
	}

	@Override
	public BlueCloudMetadataElement[] getOptionalMetadataElements() {
		return new BlueCloudMetadataElement[] { //
				BlueCloudMetadataElement.KEYWORD_TYPE, //
				BlueCloudMetadataElement.CRUISE, //
				BlueCloudMetadataElement.CRUISE_URI, //
				BlueCloudMetadataElement.PROJECT, //
				BlueCloudMetadataElement.PROJECT_URI, //
		};
	}

	public static void main(String[] args) throws Exception {

		new BLUECloudReport(null,false, 200, new String[] { "emodnet-physics" });

	}

}
