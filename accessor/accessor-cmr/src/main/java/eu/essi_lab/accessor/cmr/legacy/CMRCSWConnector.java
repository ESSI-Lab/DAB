// package eu.essi_lab.accessor.cmr.legacy;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
//
// import org.w3c.dom.Node;
//
// import eu.essi_lab.accessor.cmr.CMRCollectionMapper;
// import eu.essi_lab.accessor.cmr.CMROriginalMDWrapper;
// import eu.essi_lab.accessor.csw.CSWConnector;
// import eu.essi_lab.model.configuration.option.GSConfOptionString;
// import eu.essi_lab.model.exceptions.GSException;
// import eu.essi_lab.model.resource.OriginalMetadata;
//
/// **
// * @author ilsanto
// */
// public class CMRCSWConnector extends CSWConnector {
//
// /**
// *
// */
// private static final long serialVersionUID = 682090189508589496L;
// private static final String BASE_URL_OPTION_KEY = "BASE_URL_OPTION_KEY";
// private static final String CMR_OSDD_BASE_URL_OPTION_KEY = "CMR_OSDD_BASE_URL_OPTION_KEY";
//
// public CMRCSWConnector() {
//
// GSConfOptionString baseurlOption = new GSConfOptionString();
//
// baseurlOption.setKey(BASE_URL_OPTION_KEY);
// /**
// * works with value https://cmr.earthdata.nasa.gov/opensearch/collections.atom?
// * this is used by {@link CMRCollectionMapper} to retrieve the real second-level query url for each collection
// *
// */
// baseurlOption.setLabel("Granule search base url");
//
// getSupportedOptions().put(BASE_URL_OPTION_KEY, baseurlOption);
//
// GSConfOptionString osddbaseurlOption = new GSConfOptionString();
//
// osddbaseurlOption.setKey(CMR_OSDD_BASE_URL_OPTION_KEY);
// /**
// * works with value https://cmr.earthdata.nasa.gov/opensearch/granules/descriptor_document.xml?
// * this is used by {@link CMRCollectionMapper} to retrieve the real second-level query url for each collection of
// non-CWIC records
// *
// */
// osddbaseurlOption.setLabel("Opesnsearc DD base url");
//
// osddbaseurlOption.setValue("https://cmr.earthdata.nasa.gov/opensearch/granules/descriptor_document.xml?");
//
// getSupportedOptions().put(CMR_OSDD_BASE_URL_OPTION_KEY, osddbaseurlOption);
//
// }
//
// @Override
// public String getLabel() {
//
// return "CMR CSW Connector";
// }
//
// @Override
// protected OriginalMetadata createMetadata(Node node) throws GSException {
//
// String configuredBaseURL = ((GSConfOptionString) getSupportedOptions().get(BASE_URL_OPTION_KEY)).getValue();
//
// String cmrOsddBaseURL = ((GSConfOptionString) getSupportedOptions().get(CMR_OSDD_BASE_URL_OPTION_KEY)).getValue();
//
// OriginalMetadata metadataRecord = super.createMetadata(node);
//
// OriginalMetadata wrappedMetadataRecord = new CMROriginalMDWrapper().wrap(metadataRecord, configuredBaseURL,
// cmrOsddBaseURL);
//
// wrappedMetadataRecord.setSchemeURI(CMRCollectionMapper.SCHEMA_URI);
//
// return wrappedMetadataRecord;
// }
//
// }
