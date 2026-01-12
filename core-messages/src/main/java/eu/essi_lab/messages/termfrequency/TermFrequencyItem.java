package eu.essi_lab.messages.termfrequency;

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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;

public class TermFrequencyItem {

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String term;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String label;

    public String getAlternateDecodedTermLanguage() {
	return alternateDecodedTermLanguage;
    }

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String decodedTerm;

    public String getAlternateDecodedTerm() {
	return alternateDecodedTerm;
    }

    public void setAlternateDecodedTerm(String alternateDecodedTerm) {
	this.alternateDecodedTerm = alternateDecodedTerm;
    }

    private String alternateDecodedTerm;

    public void setAlternateDecodedTermLanguage(String alternateDecodedTermLanguage) {
	this.alternateDecodedTermLanguage = alternateDecodedTermLanguage;
    }

    private String alternateDecodedTermLanguage;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private int freq;
    @XmlTransient
    private Map<String, String> nestedProperties = new HashMap<>();

    public TermFrequencyItem() {
	// nothing to init
    }

    public void setTerm(String term) {
	this.term = term;
	try {

	    this.decodedTerm = URLDecoder.decode(term, StandardCharsets.UTF_8);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't decode {}", term, e);
	}
    }

    public void setFreq(int freq) {
	this.freq = freq;
    }

    /**
     * @param decodedTerm
     */
    public void setDecodedTerm(String decodedTerm) {
	this.decodedTerm = decodedTerm;
    }

    @XmlTransient
    public String getTerm() {
	return term;
    }

    @XmlTransient
    public String getLabel() {
	if (label == null) {
	    return term;
	} else {
	    return label;
	}
    }

    public void setLabel(String label) {
	this.label = label;
    }

    @XmlTransient
    public String getDecodedTerm() {

	return decodedTerm;
    }

    @XmlTransient
    public int getFreq() {
	return freq;
    }

    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof TermFrequencyItem item))
	    return false;

	return this.freq == item.freq && //
		this.term.equals(item.term);
    }

    @Override
    public String toString() {

	return "Term: " + getTerm() + "\n" + "Frequency: " + getFreq() + "\n";
    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }

    @XmlTransient
    public Map<String, String> getNestedProperties() {
	return nestedProperties;
    }

    public void setNestedProperties(Map<String, String> nestedProperties) {
	this.nestedProperties = nestedProperties;
    }
}
