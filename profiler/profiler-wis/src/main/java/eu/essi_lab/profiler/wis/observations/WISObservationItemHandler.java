package eu.essi_lab.profiler.wis.observations;


/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Optional;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.WIS2DataItem;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.whos.WIS2DataPolicy;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Serves a single cached value, encoded by {@link WIS2DataItem}. This is the canonical link of the WIS2 data
 * notification messages, so only the values of the sources published with the <code>core</code> data policy are
 * served, without authentication. The other values are reported as not found
 *
 * @author boldrini
 */
public class WISObservationItemHandler extends DefaultRequestHandler {

    private static final String WIS_OBSERVATION_ITEM_ERROR = "WIS_OBSERVATION_ITEM_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(new WISObservationItemFilter().accept(request) ? ValidationResult.VALIDATION_SUCCESSFUL
		: ValidationResult.VALIDATION_FAILED);

	return ret;
    }

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	Optional<String> item = getItem(webRequest);

	if (item.isEmpty()) {

	    return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity("{}").build();
	}

	return Response.ok(item.get().getBytes(StandardCharsets.UTF_8)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * @param webRequest
     * @return the encoded item, if found and served
     * @throws GSException
     */
    private Optional<String> getItem(WebRequest webRequest) throws GSException {

	String itemId = new WISRequest(webRequest).getCollectionParameter();

	Optional<SimpleEntry<String, Date>> parsed = WIS2DataItem.parseItemId(itemId);
	if (parsed.isEmpty()) {
	    return Optional.empty();
	}

	String dataIdentifier = parsed.get().getKey();
	Date date = parsed.get().getValue();

	try {

	    Optional<DataRecord> record = WISUtils.getDataCacheConnector().//
		    getRecords(date, date, dataIdentifier).//
		    stream().//
		    filter(r -> date.equals(r.getDate())).//
		    findFirst();

	    if (record.isEmpty()) {
		return Optional.empty();
	    }

	    if (WISUtils.getDataPolicy(record.get().getSourceIdentifier()) != WIS2DataPolicy.CORE) {

		GSLoggerFactory.getLogger(getClass()).debug("Item {} of not core source {} requested", itemId,
			record.get().getSourceIdentifier());
		return Optional.empty();
	    }

	    return Optional.of(WIS2DataItem.encode(record.get()));

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WIS_OBSERVATION_ITEM_ERROR, //
		    e);
	}
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	return getItem(webRequest).orElse("{}");
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
