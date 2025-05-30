package eu.essi_lab.gssrv.rest;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;

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

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.pdk.Profiler;

/**
 * A filter to select the {@link Profiler} suitable to handle a {@link WebRequest}
 * 
 * @author Fabrizio
 */
public interface ProfilerSettingFilter {

    /**
     * Tests whether or not the specified <code>setting</code> refers
     * to a {@link Profiler} that can handles a given {@link WebRequest}
     * 
     * @param setting
     * @return
     */
    public boolean accept(ProfilerSetting setting);
}
