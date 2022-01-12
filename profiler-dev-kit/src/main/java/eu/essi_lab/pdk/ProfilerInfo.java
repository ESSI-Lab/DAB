package eu.essi_lab.pdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public class ProfilerInfo {

    private String name;
    private String type;
    private String version;
    private String path;

    /**
     * Get the path where the "GI-suite service" is expected to receive the {@link Profiler} requests from the suitable
     * clients
     * 
     * @return a non <code>null</code> string which contains only alphabetic characters
     */
    public String getServicePath() {
	return path;
    }

    /**
     * Set the path where the "GI-suite service" is expected to receive the {@link Profiler} requests from the suitable
     * clients
     * 
     * @param path a non <code>null</code> string which contains only alphabetic characters
     */
    public void setServicePath(String path) {
	this.path = path;
    }

    /**
     * Returns the {@link Profiler} name
     * 
     * @return a non <code>null</code> string
     */
    public String getServiceName() {
	return name;
    }

    /**
     * Set the {@link Profiler} name
     * 
     * @param name a non <code>null</code> string
     */
    public void setServiceName(String name) {
	this.name = name;
    }

    /**
     * Returns the type of the {@link Profiler} service
     * 
     * @return a non <code>null</code> string
     */
    public String getServiceType() {
	return type;
    }

    /**
     * Set the type of the {@link Profiler} service (e.g: "OAI-PMH", "OpenSearch", etc..)
     * 
     * @param type
     */
    public void setServiceType(String type) {
	this.type = type;
    }

    /**
     * Get the version of the {@link Profiler} service
     * 
     * @return a non <code>null</code> string
     */
    public String getServiceVersion() {
	return version;
    }

    /**
     * Set the version of the {@link Profiler} service (e.g: "OAI-PMH", "OpenSearch", etc..)
     * 
     * @param version a non <code>null</code> string
     */
    public void setServiceVersion(String version) {
	this.version = version;
    }
}
