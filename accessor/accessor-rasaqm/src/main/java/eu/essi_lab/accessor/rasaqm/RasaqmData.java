package eu.essi_lab.accessor.rasaqm;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class RasaqmData {

    @XmlElement
    private Date date;
    @XmlElement
    private BigDecimal value;

    public RasaqmData() {
	// JAXB no args constructor
    }
    
    public RasaqmData(Date date, BigDecimal value) {
	this.date = date;
	this.value = value;
    }

    @XmlTransient
    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    @XmlTransient
    public BigDecimal getValue() {
	return value;
    }

    public void setValue(BigDecimal value) {
	this.value = value;
    }

}
