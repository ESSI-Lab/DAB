package eu.essi_lab.messages;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DataDescriptorRuntimeInfo.TargetProvider;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author Fabrizio
 */
public class AccessMessage extends RequestMessage {

    /**
     *
     */
    private static final long serialVersionUID = 7939395851033735840L;
    private static final String DESCRIPTOR = "descriptor";
    private static final String ONLINE_ID = "onlineId";
    private static final String DOWNLOAD_COUNT = "downloadCount";

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = super.provideInfo();

	map.put(RuntimeInfoElement.ACCESS_MESSAGE_TIME_STAMP.getName(), //
		Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds()));

	map.put(RuntimeInfoElement.ACCESS_MESSAGE_TIME_STAMP_MILLIS.getName(), //
		Arrays.asList(String.valueOf(System.currentTimeMillis())));

	getView().ifPresent(v -> map.put(RuntimeInfoElement.ACCESS_MESSAGE_VIEW_ID.getName(), Arrays.asList(v.getId())));

	DataDescriptor descriptor = getTargetDataDescriptor();

	DataDescriptorRuntimeInfo.publishDataDescriptorInfo(TargetProvider.ACCESS_MESSAGE, descriptor, map);

	return map;
    }

    @Override
    public String getName() {

	return "ACCESS_MESSAGE";
    }

    /**
     * @return
     */
    public DataDescriptor getTargetDataDescriptor() {

	return getPayload().get(DESCRIPTOR, DataDescriptor.class);
    }

    /**
     * @param descriptor
     */
    public void setTargetDataDescriptor(DataDescriptor descriptor) {

	getPayload().add(new GSProperty<DataDescriptor>(DESCRIPTOR, descriptor));
    }

    /**
     * @return
     */
    public String getOnlineId() {

	return getPayload().get(ONLINE_ID, String.class);
    }

    /**
     * @param id
     */
    public void setOnlineId(String id) {

	getPayload().add(new GSProperty<String>(ONLINE_ID, id));
    }

    /**
     * @return
     */
    public Optional<Integer> getDownloadCount() {

	return Optional.ofNullable(getPayload().get(DOWNLOAD_COUNT, Integer.class));
    }

    /**
     * @param id
     */
    public void setDownloadCount(int count) {

	getPayload().add(new GSProperty<Integer>(DOWNLOAD_COUNT, count));
    }
}
