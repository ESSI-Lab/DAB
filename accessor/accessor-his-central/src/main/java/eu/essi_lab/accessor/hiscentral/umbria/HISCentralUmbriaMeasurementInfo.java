package eu.essi_lab.accessor.hiscentral.umbria;

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

public class HISCentralUmbriaMeasurementInfo {

    private String type;
    private String date;
    private String resourceId;


     public HISCentralUmbriaMeasurementInfo(String type, String date, String resourceId){
	 this.type = type;
        this.date = date;
        this.resourceId = resourceId;
     }

    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	 this.date = date;
    }

    public String getResourceId() {
	 return resourceId;
    }

    public void setResourceId(String resourceId) {
	 this.resourceId = resourceId;
    }

    public String getType() {
	 return type;
    }

    public void setType(String type) {
	 this.type = type;
    }
}
