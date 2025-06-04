package eu.essi_lab.accessor.hiscentral.lombardia;

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

import java.math.BigDecimal;
import java.util.Date;

import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_VALIDITY_FLAG;

public class Dato {
    private Date date;
    private ID_VALIDITY_FLAG validityFlag;
    private BigDecimal value;

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public void setDate(String date) {
	if (date != null && !date.isEmpty()) {
	    try {
		this.date = HISCentralLombardiaClient.sdf.parse(date);
	    } catch (Exception e) {
	    }
	}
    }

    public ID_VALIDITY_FLAG getValidityFlag() {
	return validityFlag;
    }

    public void setValidityFlag(ID_VALIDITY_FLAG validityFlag) {
	this.validityFlag = validityFlag;
    }

    public void setValidityFlag(String validityFlag) {
	this.validityFlag = HISCentralLombardiaClient.ID_VALIDITY_FLAG.decode(validityFlag);
    }

    public BigDecimal getValue() {
	return value;
    }

    public void setValue(BigDecimal value) {
	this.value = value;
    }

    public void setValue(String value) {
	if (value != null && !value.isEmpty()) {
	    try {
		this.value = new BigDecimal(value);
	    } catch (Exception e) {
	    }
	}
    }

    public Dato(Date date, ID_VALIDITY_FLAG validityFlag, BigDecimal value) {
	super();
	this.date = date;
	this.validityFlag = validityFlag;
	this.value = value;
    }

    public Dato() {

    }
}
