// package eu.essi_lab.accessor.cmr.legacy;

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
//
// import javax.xml.bind.JAXBElement;
//
// import org.w3c.dom.Node;
//
// import eu.essi_lab.accessor.cmr.cwic.CWICCMRCollectionMapper;
// import eu.essi_lab.jaxb.common.ObjectFactories;
// import eu.essi_lab.jaxb.csw._2_0_2.Constraint;
// import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
// import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
// import eu.essi_lab.jaxb.filter._1_1_0.ComparisonOpsType;
// import eu.essi_lab.jaxb.filter._1_1_0.FilterType;
// import eu.essi_lab.jaxb.filter._1_1_0.LiteralType;
// import eu.essi_lab.jaxb.filter._1_1_0.PropertyIsLikeType;
// import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
// import eu.essi_lab.messages.listrecords.ListRecordsRequest;
// import eu.essi_lab.messages.listrecords.ListRecordsResponse;
// import eu.essi_lab.model.exceptions.GSException;
// import eu.essi_lab.model.resource.OriginalMetadata;
//
/// **
// * @author ilsanto
// */
// public class CWICCMRCSWConnector extends CMRCSWConnector {
//
// @Override
// public String getLabel() {
//
// return "CWIC CSW CMR Connector";
// }
//
// @Override
// protected GetRecords createGetRecords(Integer startPosition, Integer requestSize) throws GSException {
//
// GetRecords cswRequest = super.createGetRecords(startPosition, requestSize);
//
// return createCwicGetRecords(cswRequest);
//
// }
//
// protected GetRecords createCwicGetRecords(GetRecords cswRequest) {
//
// eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory filterFactory = new eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory();
//
// QueryType query = ((JAXBElement<QueryType>) cswRequest.getAbstractQuery()).getValue();
//
// Constraint constraint = ObjectFactories.CSW().createConstraint();
//
// FilterType filter = filterFactory.createFilterType();
//
// PropertyIsLikeType proislike = filterFactory.createPropertyIsLikeType();
//
// LiteralType literalValue = filterFactory.createLiteralType();
//
// literalValue.getContent().add("true");
//
// proislike.setLiteral(literalValue);
//
// PropertyNameType proertyName = filterFactory.createPropertyNameType();
//
// proertyName.getContent().add("IsCwic");
//
// proislike.setPropertyName(proertyName);
//
// JAXBElement<ComparisonOpsType> jaxb = filterFactory.createComparisonOps(proislike);
//
// filter.setComparisonOps(jaxb);
//
// constraint.setFilter(filter);
//
// query.setConstraint(constraint);
//
// return cswRequest;
//
// }
//
// @Override
// public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
// ListRecordsResponse<OriginalMetadata> list = new ListRecordsResponse<OriginalMetadata>();
// try {
// list = super.listRecords(request);
// } catch (GSException e) {
// String token = request.getResumptionToken();
// if (token != null) {
// Integer i = Integer.parseInt(token);
// i = i + 100;
// request.setResumptionToken(String.valueOf(i));
// list = super.listRecords(request);
// return list;
// }
// }
// return list;
// }
//
// @Override
// protected OriginalMetadata createMetadata(Node node) throws GSException {
//
// OriginalMetadata om = super.createMetadata(node);
//
// om.setSchemeURI(CWICCMRCollectionMapper.SCHEMA_URI);
//
// return om;
// }
//
// }
