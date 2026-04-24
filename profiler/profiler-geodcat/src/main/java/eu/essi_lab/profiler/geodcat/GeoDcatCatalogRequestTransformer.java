package eu.essi_lab.profiler.geodcat;

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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * Paginated discovery for the DCAT catalog ({@code startIndex}, {@code count} query parameters).
 */
public class GeoDcatCatalogRequestTransformer extends DiscoveryRequestTransformer {

    public static final int DEFAULT_START_INDEX = 1;
    public static final int DEFAULT_COUNT = 20;
    public static final int MAX_COUNT = 200;

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	if (parser.isValid("startIndex")) {

	    try {

		int s = Integer.parseInt(parser.getValue("startIndex"));
		if (s < 1) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("startIndex must be >= 1");
		    message.setLocator("startIndex");
		    return message;
		}

	    } catch (NumberFormatException e) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Invalid startIndex");
		message.setLocator("startIndex");
		return message;
	    }
	}

	if (parser.isValid("count")) {

	    try {

		int c = Integer.parseInt(parser.getValue("count"));
		if (c < 1) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("count must be >= 1");
		    message.setLocator("count");
		    return message;
		}

	    } catch (NumberFormatException e) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Invalid count");
		message.setLocator("count");
		return message;
	    }
	}

	return message;
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	return null;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.setIncludeOriginal(false);

	return selector;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	int start = DEFAULT_START_INDEX;
	int count = DEFAULT_COUNT;

	if (parser.isValid("startIndex")) {

	    start = Integer.parseInt(parser.getValue("startIndex"));
	}

	if (parser.isValid("count")) {

	    count = Integer.parseInt(parser.getValue("count"));
	}

	count = Math.min(count, MAX_COUNT);

	return new Page(start, count);
    }

    @Override
    public String getProfilerType() {

	return new GeoDcatProfilerSetting().getServiceType();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
