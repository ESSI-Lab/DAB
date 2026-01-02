/**
 * 
 */
package eu.essi_lab.profiler.wms.capabilities;

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

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.jaxb.wms._1_3_0.Capability;
import eu.essi_lab.jaxb.wms._1_3_0.ContactAddress;
import eu.essi_lab.jaxb.wms._1_3_0.ContactInformation;
import eu.essi_lab.jaxb.wms._1_3_0.ContactPersonPrimary;
import eu.essi_lab.jaxb.wms._1_3_0.DCPType;
import eu.essi_lab.jaxb.wms._1_3_0.Dimension;
import eu.essi_lab.jaxb.wms._1_3_0.EXGeographicBoundingBox;
import eu.essi_lab.jaxb.wms._1_3_0.Get;
import eu.essi_lab.jaxb.wms._1_3_0.HTTP;
import eu.essi_lab.jaxb.wms._1_3_0.Layer;
import eu.essi_lab.jaxb.wms._1_3_0.OnlineResource;
import eu.essi_lab.jaxb.wms._1_3_0.OperationType;
import eu.essi_lab.jaxb.wms._1_3_0.Request;
import eu.essi_lab.jaxb.wms._1_3_0.Service;
import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;
import eu.essi_lab.jaxb.wms.extension.JAXBWMS;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wms.WMSProfilerSetting;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

/**
 * @author boldrini
 */
public class WMSCapabilitiesHandler extends DefaultRequestHandler {

	private static final String WMS_CAPABILITIES_HANDLER_ERROR = "WMS_CAPABILITIES_HANDLER_ERROR";

	@Override
	public ValidationMessage validate(WebRequest request) throws GSException {

		ValidationMessage ret = new ValidationMessage();
		try {
			new WMSGetCapabilitiesRequest(request);
			ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
		} catch (Exception e) {
			ret.setResult(ValidationResult.VALIDATION_FAILED);
		}

		return ret;
	}

	@Override
	public String getStringResponse(WebRequest webRequest) throws GSException {

		try {
			//
			// creates the bonds
			//
			Set<Bond> operands = new HashSet<>();

			// we are interested only on downloadable datasets
			ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
			operands.add(accessBond);

			// we are interested only on downloadable datasets
			ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
			operands.add(downBond);

			// we are interested only on GRIDDED datasets
			ResourcePropertyBond gridBond = BondFactory.createIsGridBond(true);
			operands.add(gridBond);

			LogicalBond andBond = BondFactory.createAndBond(operands);

			Set<Bond> operands2 = new HashSet<>();

			// we are interested only on downloadable datasets
			operands2.add(accessBond);

			// we are interested only on downloadable datasets
			operands2.add(downBond);

			// we are interested only on GRIDDED datasets
			ResourcePropertyBond vectorBond = BondFactory.createIsVectorBond(true);
			operands2.add(vectorBond);

			LogicalBond andBond2 = BondFactory.createAndBond(operands2);

			LogicalBond finalBond = BondFactory.createOrBond(andBond, andBond2);

			//
			// creates the message
			//
			DiscoveryMessage discoveryMessage = new DiscoveryMessage();

			List<GSSource> allSources = ConfigurationWrapper.getAllSources();

			// set the required properties
			discoveryMessage.setSources(allSources);
			discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

			discoveryMessage.setWebRequest(webRequest);

			ResourceSelector selector = new ResourceSelector();
			selector.setSubset(ResourceSubset.CORE_EXTENDED);
			selector.setIndexesPolicy(IndexesPolicy.NONE);
			discoveryMessage.setResourceSelector(selector);

			// set the view
			Optional<String> viewId = webRequest.extractViewId();
			if (viewId.isPresent()) {

				WebRequestTransformer.setView(//
						viewId.get(), //
						discoveryMessage.getDataBaseURI(), //
						discoveryMessage);
			}

			// set the user bond
			discoveryMessage.setUserBond(finalBond);

			// pagination works with grouped results. in this case there is one result item
			// for each source.
			// in order to be sure to get all the items in the same statistics response,
			// we set the count equals to number of sources
			Page page = new Page();
			page.setStart(1);
			page.setSize(1000);

			discoveryMessage.setPage(page);

			// computes union of bboxes

			ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
			IDiscoveryExecutor executor = loader.iterator().next();

			ResultSet<GSResource> response = executor.retrieve(discoveryMessage);

			WMSCapabilities capabilities = new WMSCapabilities();

			// SERVICE IDENTIFICATION

			Service service = new Service();
			capabilities.setService(service);

			service.setAbstract("GI-suite brokering service Web Map Service");

			ContactInformation contactInfo = new ContactInformation();
			contactInfo.setContactVoiceTelephone("+390555226591");
			ContactPersonPrimary contactPerson = new ContactPersonPrimary();
			contactPerson.setContactOrganization("CNR-IIA");
			contactPerson.setContactPerson("Paolo Mazzetti");
			contactInfo.setContactPersonPrimary(contactPerson);
			contactInfo.setContactPosition("Head of the Division of Florence of CNR-IIA");
			contactInfo.setContactElectronicMailAddress("info@essi-lab.eu");
			ContactAddress address = new ContactAddress();
			address.setAddress("Area di Ricerca di Firenze, Via Madonna del Piano, 10");
			address.setCity("Sesto Fiorentino");
			address.setCountry("Italy");
			address.setPostCode("50019");
			address.setStateOrProvince("Firenze");
			contactInfo.setContactAddress(address);
			service.setContactInformation(contactInfo);

			Capability cap = new Capability();

			Request request = new Request();

			OperationType capOperation = new OperationType();
			DCPType capDCPT = new DCPType();
			HTTP capHttp = new HTTP();
			Get capGet = new Get();
			OnlineResource capOnlineGet = new OnlineResource();
			String capURL = "";
			try {
				UriInfo uri = webRequest.getUriInfo();
				capURL = uri.getBaseUri().toString() + "/" + new WMSProfilerSetting().getServicePath();
			} catch (Exception e) {
			}
			capOnlineGet.setHref(capURL);
			capGet.setOnlineResource(capOnlineGet);
			capHttp.setGet(capGet);
			capDCPT.setHTTP(capHttp);
			capOperation.getDCPTypes().add(capDCPT);

			request.setGetCapabilities(capOperation);

			OperationType getMapOperation = new OperationType();
			getMapOperation.getFormats().add(DataFormat.IMAGE_PNG().getIdentifier());
			DCPType mapDCPT = new DCPType();
			HTTP mapHttp = new HTTP();
			Get mapGet = new Get();
			OnlineResource mapOnlineGet = new OnlineResource();
			String mapURL = "";
			try {
				UriInfo uri = webRequest.getUriInfo();
				mapURL = uri.getBaseUri().toString() + "/" + new WMSProfilerSetting().getServicePath();
			} catch (Exception e) {
			}
			mapOnlineGet.setHref(mapURL);
			mapGet.setOnlineResource(mapOnlineGet);
			mapHttp.setGet(mapGet);
			mapDCPT.setHTTP(mapHttp);
			getMapOperation.getDCPTypes().add(mapDCPT);

			request.setGetMap(getMapOperation);
			cap.setRequest(request);

			capabilities.setCapability(cap);
			Layer rootLayer = new Layer();
			rootLayer.setTitle("GI-suite root layer");
			cap.setLayer(rootLayer);
			rootLayer.getCRS().add(CRS.EPSG_4326().getIdentifier());
			
			List<GSResource> resources = response.getResultsList().stream().
			sorted((r1,r2) -> r1.getHarmonizedMetadata().getCoreMetadata().getTitle().compareTo(r2.getHarmonizedMetadata().getCoreMetadata().getTitle())).
			collect(Collectors.toList());
			
			for (GSResource resource : resources) {
				ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
				List<DataComplianceReport> reports = handler.getReports();
				if (!reports.isEmpty()) {
					Layer layer = new Layer();
					layer.setTitle(resource.getHarmonizedMetadata().getCoreMetadata().getTitle().trim());
					layer.setAbstract(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract().trim());
					DataComplianceReport report = reports.get(0);
					String onlineId = report.getOnlineId();
					layer.setName(onlineId);

					GeographicBoundingBox boundingBox = resource.getHarmonizedMetadata().getCoreMetadata()
							.getBoundingBox();

					EXGeographicBoundingBox bbox = new EXGeographicBoundingBox();

					if (boundingBox != null) {

						bbox.setNorthBoundLatitude(boundingBox.getNorth());
						bbox.setSouthBoundLatitude(boundingBox.getSouth());
						bbox.setWestBoundLongitude(boundingBox.getWest());
						bbox.setEastBoundLongitude(boundingBox.getEast());
					} else {
						bbox.setNorthBoundLatitude(90);
						bbox.setSouthBoundLatitude(-90);
						bbox.setWestBoundLongitude(-180);
						bbox.setEastBoundLongitude(180);
					}
					DataDescriptor descriptor = report.getFullDataDescriptor();
					if (descriptor != null) {
						DataDimension temporal = descriptor.getTemporalDimension();
						if (temporal != null) {
							ContinueDimension dimension = temporal.getContinueDimension();
							if (dimension != null) {
								Number lower = dimension.getLower();
								Number upper = dimension.getUpper();
								Number resolution = dimension.getResolution();
								Dimension timeDimension = new Dimension();
								timeDimension.setName("time");
								Unit uom = new Unit("ISO8601");
								String start = ISO8601DateTimeUtils.getISO8601DateTime(new Date(lower.longValue()));
								String end = ISO8601DateTimeUtils.getISO8601DateTime(new Date(upper.longValue()));
								timeDimension.setDefault(end);
								timeDimension
										.setValue(start + "/" + end + "/PT" + (resolution.longValue() / 1000) + "S");
								dimension.setUom(uom);
								layer.getDimensions().add(timeDimension);
							}
						}
					}

					layer.setEXGeographicBoundingBox(bbox);
					layer.setMinScaleDenominator(1.);
					layer.setMaxScaleDenominator(100000000.);
					rootLayer.getLayers().add(layer);
				}

			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAXBWMS.getInstance().getMarshaller().marshal(capabilities, baos);
			String ret = IOUtils.toString(baos.toByteArray(), "UTF-8");
			baos.close();
			return ret;

		} catch (Exception e) {
			e.printStackTrace();

			throw GSException.createException(//
					getClass(), //
					ErrorInfo.ERRORTYPE_INTERNAL, //
					ErrorInfo.SEVERITY_ERROR, //
					WMS_CAPABILITIES_HANDLER_ERROR, //
					e);
		}

	}

	@Override
	public MediaType getMediaType(WebRequest webRequest) {

		return MediaType.APPLICATION_XML_TYPE;
	}
}
