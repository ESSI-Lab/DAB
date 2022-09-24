package eu.essi_lab.profiler.csw.profile;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.SchemaComponentType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMD_ResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMI_ResultSetMapper;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWDescribeRecordHandler;

/**
 * Concrete classes provide information about a specific CSW profile (e.g: ISO profile, CORE profile).<br>
 * In particular, the information specific of a given profile is provided by the following abstract methods:
 * <ul>
 * <li>{@link #getSupportedTypeNames()}
 * <ul>
 * <li>the supported type names (e.g: "gmd:MD_Metadata", "gmi:MI_Metadata" for the ISO profile)
 * </ul>
 * </li></li>
 * <li>{@link #getSupportedOutputSchemas()}
 * <ul>
 * <li>the supported output schemas (e.g: "http://www.isotc211.org/2005/gmd", "http://www.isotc211.org/2005/gmi" for the
 * ISO profile)
 * </ul>
 * </li></li>
 * <li>{@link #getSupportedSchemaComponents()}
 * <ul>
 * <li>the supported schema components used by the {@link CSWDescribeRecordHandler} to
 * create the response
 * </ul>
 * </li></li>
 * <li>{@link #getResultSetMapper(String, ElementSetType, List)}
 * <ul>
 * <li>the {@link DiscoveryResultSetMapper}s suitable for the supported output schemas (e.g:
 * {@link GMD_ResultSetMapper},
 * {@link GMI_ResultSetMapper} for the ISO profile)
 * </ul>
 * </li></li>
 * </ul>
 * This abstract class also provides some utility methods which merge all the supported schema components, type names
 * and output schemas
 * <h3>Implementation note</h3><br>
 * Each concrete class <b>MUST</b> provide only the information which concerns the <i>specific</i> implemented profile.
 * So for example a
 * sub-profile of the ISO profile, must provide only the output schema specific of that profile, omitting the ones
 * related to the ISO profile<br>
 * <br>
 * 
 * @author Fabrizio
 */
public abstract class CSWProfile implements Pluggable {

    /**
     * Returns all the available profiles
     * 
     * @return
     */
    public static List<CSWProfile> getAvailableProfiles() {

	PluginsLoader<CSWProfile> pluginsLoader = new PluginsLoader<>();
	List<CSWProfile> profiles = pluginsLoader.loadPlugins(CSWProfile.class);

	GSLoggerFactory.getLogger(CSWProfile.class).info("Loaded CSW profiles:");

	profiles.forEach(profile -> GSLoggerFactory.getLogger(CSWProfile.class).info(profile.getClass().getCanonicalName()));//

	return profiles;
    }

    /**
     * Merges all the available {@link SchemaComponentType}s according to the the registered {@link CSWProfile}s
     * and finds the one which supports the supplied <code>typeName</code>
     * 
     * @return the found {@link SchemaComponentType} or <code>null</code> if none of the registered {@link CSWProfile}s
     *         supports the supplied <code>typeName</code>
     */
    public static SchemaComponentType findSchemaComponent(String typeName) {

	List<CSWProfile> profiles = getAvailableProfiles();
	return profiles.stream().//
		filter(profile -> profile.supportsSchemaComponent(typeName)).//
		map(profile -> profile.createSchemaComponent(typeName)).//
		findFirst().//
		orElse(null);
    }

    /**
     * Merges the supported schema components provided by all the registered {@link CSWProfile}s
     */
    public static List<SchemaComponentType> getAllSupportedSchemaComponents() {

	List<CSWProfile> profiles = getAvailableProfiles();
	return profiles.stream().//
		flatMap(profile -> profile.getSupportedSchemaComponents().stream()).//
		distinct().//
		collect(Collectors.toList());
    }

    /**
     * Merges the supported type names provided by all the registered {@link CSWProfile}s
     */
    public static List<QName> getAllSupportedTypeNames() {

	List<CSWProfile> profiles = getAvailableProfiles();
	return profiles.stream().//
		flatMap(profile -> profile.getSupportedTypeNames().stream()).//
		distinct().//
		collect(Collectors.toList());
    }

    /**
     * Merges the supported output schemas provided by all the registered {@link CSWProfile}s
     */
    public static List<String> getAllSupportedOutputSchemas() {

	List<CSWProfile> profiles = getAvailableProfiles();
	return profiles.stream().//
		flatMap(profile -> profile.getSupportedOutputSchemas().stream()).//
		distinct().//
		collect(Collectors.toList());
    }

    /**
     * Returns the list of output schemas supported by this profile
     * 
     * @return a {@link List} (possible empty but non <code>null</code>) with the output schemas supported by this
     *         profile
     */
    public abstract List<String> getSupportedOutputSchemas();

    /**
     * Returns the list of type names supported by this profile
     * 
     * @return a {@link List} (possible empty but non <code>null</code>) with the type names supported by this
     *         profile
     */
    public abstract List<QName> getSupportedTypeNames();

    /**
     * Returns the list of {@link SchemaComponentType}s supported by this profile
     * 
     * @return a {@link List} (possible empty but non <code>null</code>) with the {@link SchemaComponentType}s supported
     *         by this profile
     */
    public abstract List<SchemaComponentType> getSupportedSchemaComponents();

    /**
     * Creates the {@link SchemaComponentType} basing on the supplied <code>typeName</code>. E.g: if
     * <code>typeName</code> is
     * "gmd:MD_Metadata", the profile creates a {@link SchemaComponentType} of the "gmd:MD_Metadata" element in the
     * "http://www.isotc211.org/2005/gmd"
     * name space
     * 
     * @param typeName a non <code>null</code> string identifying a type name (e.g: "gmd:MD_Metadata")
     * @return the created {@link SchemaComponentType} or <code>null</code> it <code>typeName</code> is not supported
     * @see #getSupportedTypeNames()
     */
    public abstract SchemaComponentType createSchemaComponent(String typeName);

    /**
     * Returns the {@link DiscoveryResultSetMapper} suitable for supplied <code>outputSchema</code>
     * 
     * @param outputSchema
     * @param setType a type identifying a set of elements (e.g. brief, summary, full)
     * @param elementNames a set of element names (e.g. title, abstract, fileIdentifier)
     * @return the suitable {@link DiscoveryResultSetMapper} or <code>null</code> if <code>outputSchema</code> is not
     *         supported
     * @see #getSupportedOutputSchemas()
     */
    public abstract DiscoveryResultSetMapper<Element> getResultSetMapper(String outputSchema, ElementSetType setType,
	    List<QName> elementNames);

    /**
     * Verifies whether the supplied <code>typeName</code> is supported by this profile
     * 
     * @param typeName a non <code>null</code> {@link String} representing the typeName to check
     * @return <code>true</code> if the supplied <code>typeName</code> is supported by this profile,
     *         <code>false</code> otherwise
     */
    public boolean supportsSchemaComponent(String typeName) {

	return createSchemaComponent(typeName) != null;
    }

    /**
     * Verifies whether the supplied <code>outputSchema</code> is supported by this profile. This default implementation
     * verifies if <code>typeName</code> is contained in the list returned by {@link #getSupportedOutputSchemas()}
     * 
     * @param outputSchema a non <code>null</code> {@link String} representing the output schema to check
     * @return <code>true</code> if the supplied <code>outputSchema</code> is supported by this profile,
     *         <code>false</code>
     *         otherwise
     */
    public boolean supportsOutputSchema(String outputSchema) {

	return getSupportedOutputSchemas().contains(outputSchema);
    }

    /**
     * Verifies whether the supplied <code>typeName</code> is supported by this profile. This default implementation
     * verifies if <code>typeName</code> is contained in the list returned by {@link #getSupportedTypeNames()}
     * 
     * @param typeName a non <code>null</code> {@link QName} representing the type name to check
     * @return <code>true</code> if the supplied <code>typeName</code> is supported by this profile, <code>false</code>
     *         otherwise
     */
    public boolean supportsTypeName(QName typeName) {

	return getSupportedTypeNames().contains(typeName);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
