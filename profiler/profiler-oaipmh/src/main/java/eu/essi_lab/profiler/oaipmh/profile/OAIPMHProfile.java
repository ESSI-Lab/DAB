package eu.essi_lab.profiler.oaipmh.profile;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;

/**
 * Concrete classes provide information about a profile supported by the OAIPMH service.<br>
 * In particular, the information specific of a given profile is provided by the following abstract methods:
 * <ul>
 * <li>{@link #getSupportedMetadataFormat()}</li>
 * <li>{@link #getResultSetMapper()}</li>
 * </ul>
 * 
 * @author Fabrizio
 */
public abstract class OAIPMHProfile implements Pluggable {

    /**
     * Returns all the available profiles
     * 
     * @return
     */
    public static List<OAIPMHProfile> getAvailableProfiles() {

	PluginsLoader<OAIPMHProfile> pluginsLoader = new PluginsLoader<>();
	List<OAIPMHProfile> profiles = pluginsLoader.loadPlugins(OAIPMHProfile.class);

	GSLoggerFactory.getLogger(OAIPMHProfile.class).info("Loaded OAI-PMH profiles:");

	profiles.forEach(profile -> GSLoggerFactory.getLogger(OAIPMHProfile.class).info(profile.getClass().getCanonicalName()));

	return profiles;
    }

    /**
     * Merges the supported format types provided by all the registered {@link OAIPMHProfile}s
     */
    public static List<MetadataFormatType> getAllSupportedMetadataFormats() {

	List<OAIPMHProfile> profilers = getAvailableProfiles();

	return profilers.stream().//
		map(OAIPMHProfile::getSupportedMetadataFormat).//
		collect(Collectors.toList());
    }

    /**
     * Returns the supported metadata format
     * 
     * @see #getResultSetMapper()
     */
    public abstract MetadataFormatType getSupportedMetadataFormat();

    /**
     * Returns the {@link XMLResultSetMapper} suitable for the supported metadata format
     * 
     * @see #getSupportedMetadataFormat()
     */
    public abstract DiscoveryResultSetMapper<Element> getResultSetMapper();
}
