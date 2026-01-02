package eu.essi_lab.profiler.os.handler.discover.eiffel;

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

import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.DiscoveryMessage.EiffelAPIDiscoveryOption;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.os.OSProfilerSetting;
import eu.essi_lab.profiler.os.handler.discover.OSRequestTransformer;

/**
 * 
 */
public class EiffelRequestTransformer extends OSRequestTransformer {

    /**
     * @param setting
     */
    public EiffelRequestTransformer(OSProfilerSetting setting) {

	super(setting);
    }

    @Override
    protected void handleView(WebRequest request, StorageInfo storageUri, RequestMessage message) throws GSException {

	Optional<EiffelAPIDiscoveryOption> eiffelOption = EiffelDiscoveryHelper.readEiffelOption(message.getWebRequest(), getSetting().get());

	GSLoggerFactory.getLogger(getClass()).debug("Enabling EIFFEL API discovery option: " + eiffelOption.get());

	((DiscoveryMessage) message).enableEiffelAPIDiscoveryOption(eiffelOption.get());

	setView(EiffelAPIDiscoveryOption.EIFFEL_S3_VIEW_ID, storageUri, message);
    }

}
