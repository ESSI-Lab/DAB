/**
 * 
 */
package eu.essi_lab.accessor.sos._1_0_0;

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

/**
 * @author Fabrizio
 */
public class SOSProcedure {

    // urn:ogc:def:procedure:x-istsos:1.0:whostest123
    private String href;

    // urn:ogc:def:offering:x-istsos:1.0\:measurement
    private String offering;

    private String sensorML;

    /**
     * 
     */
    public SOSProcedure() {

    }

    /**
     * @param href
     * @param title
     * @param offering
     */
    public SOSProcedure(String href, String offering) {
	this.href = href;
	this.offering = offering;
    }

    /**
     * @return the sensorML
     */
    public final String getSensorML() {
	return sensorML;
    }

    /**
     * @param sensorML
     */
    public final void setSensorML(String sensorML) {
	this.sensorML = sensorML;
    }

    /**
     * @return the href
     */
    public final String getHref() {
	return href;
    }

    /**
     * @param href
     */
    public final void setHref(String href) {
	this.href = href;
    }

    /**
     * @return the offering
     */
    public final String getOffering() {
	return offering;
    }

    /**
     * @param offering
     */
    public final void setOffering(String offering) {
	this.offering = offering;
    }

}
