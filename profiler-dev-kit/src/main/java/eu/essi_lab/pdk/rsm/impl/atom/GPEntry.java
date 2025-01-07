/**
 * 
 */
package eu.essi_lab.pdk.rsm.impl.atom;

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
import java.util.Objects;

import org.jdom2.Attribute;
import org.jdom2.Element;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.atom.CustomEntry;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;

/**
 * @author Fabrizio
 */
public class GPEntry extends CustomEntry {

    /**
     * 
     */
    private static final String GEO_RSS_NS_URI = "http://www.georss.org/georss";

    /**
     * 
     */
    private static final String IIA_NS_URI = "http://iia.cnr.it";

    /**
    * 
    */
    private static final long serialVersionUID = -5381072846971414110L;

    /**
     * @param serviceType
     */
    public void setSimpleSummary(String summary) {

	if (Objects.nonNull(summary)) {

	    addSimpleElement("summary", summary.trim());
	}
    }

    /**
     * @param box
     */
    public void setBoundingBox(String box) {

	if (Objects.nonNull(box)) {

	    addSimpleElement("box", box, GEO_RSS_NS_URI);
	}
    }

    /**
     * @param box
     */
    public void setRights(String rights) {

	if (Objects.nonNull(rights)) {

	    addSimpleElement("rights", rights.trim());
	}
    }

    /**
     * @param box
     */
    public void setLogo(String logo) {

	if (Objects.nonNull(logo)) {

	    addSimpleElement("logo", logo);
	}
    }

    /**
     * @param content
     */
    public void setContent(String content) {

	if (Objects.nonNull(content)) {

	    addSimpleElement("content", content.trim());
	}
    }

    /**
     * @param beginPosition
     */
    public void setStartTime(String beginPosition) {

	addSimpleElement("dtstart", beginPosition, "http://www.w3.org/2002/12/cal/ical#");

	addSimpleElement("start", beginPosition, "http://a9.com/-/opensearch/extensions/time/1.0/");
    }

    /**
     * @param endPosition
     */
    public void setEndTime(String endPosition) {

	addSimpleElement("dtend", endPosition, "http://www.w3.org/2002/12/cal/ical#");

	addSimpleElement("stop", endPosition, "http://a9.com/-/opensearch/extensions/time/1.0/");
    }

    /**
     * @param parentID
     */
    public void setParentId(String parentID) {

	if (Objects.nonNull(parentID)) {

	    addSimpleElement("parentID", parentID.trim(), NameSpace.GS_DATA_MODEL_SCHEMA_URI);
	}
    }

    /**
     * @param harvested
     */
    public void setHarvested(Boolean harvested) {

	addSimpleElement("harvested", harvested.toString(), NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param magLevel
     */
    public void setMagnitude(String magLevel) {

	addSimpleElement("mag-level", magLevel, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param country
     */
    public void setCountry(String country) {

	addSimpleElement("poi-country", country, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param availableGranules
     */
    public void setAvailableGranules(String availableGranules) {

	addSimpleElement("available-granules", availableGranules, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param collectionQueryables
     */
    public void setCollectionQueryables(String collectionQueryables) {

	addSimpleElement("satelliteCollectionQueryable", collectionQueryables, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param worldCerealQueryables
     */
    public void setWorldCerealQueryables(String collectionQueryables) {

	addSimpleElement("worldCerealCollectionQueryables", collectionQueryables, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param worldCerealQueryables
     */
    public void setWorldCerealConfidence(String type, String value) {

	addSimpleElement(type, value, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param label
     * @param code
     */
    public void addWorldCerealType(List<WorldCerealItem> list, String type) {

	if (Objects.nonNull(type) && list != null) {

	    Element worldCerealElement = createElement(type);
	    for (WorldCerealItem item : list) {

		Attribute attr = new Attribute("code", item.getCode());
		Element el = createSimpleElement("label", item.getLabel(), attr);
		addContentTo(worldCerealElement, el);
		// Element labelElement = createElement("label");
		// addContentTo(worldCerealElement, labelElement, item.getLabel());
		// addContentTo(worldCerealElement, "code", item.getTerm());
	    }

	    addElement(worldCerealElement);

	}
    }

    /**
     * @param label
     * @param term
     */
    public void addCategory(String label, String term) {

	if (Objects.nonNull(label) && Objects.nonNull(term)) {

	    Element category = createElement("category");
	    addAttributeTo(category, "label", label.trim());
	    addAttributeTo(category, "term", term.trim());

	    addElement(category);

	}
    }

    /**
     * @param label
     * @param href
     */
    public void addLicense(String label, String href) {

	if (Objects.nonNull(label) && Objects.nonNull(href)) {

	    Element el = createElement("license");
	    if (Objects.nonNull(label)) {
		addContentTo(el, "type", label);
	    }

	    if (Objects.nonNull(href)) {
		addContentTo(el, "reference", href);
	    }

	    addElement(el);

	}
    }

    /**
     * @param citation
     */
    public void addCitation(String citation) {

	if (Objects.nonNull(citation)) {

	    addSimpleElement("citation", citation);
	}
    }

    /**
     * @param id
     * @param title
     */
    public void addSourceInfo(String id, String title) {

	if (Objects.nonNull(id) && Objects.nonNull(title)) {

	    addSimpleElement("sourceId", id.trim());
	    addSimpleElement("sourceTitle", title.trim());

	}
    }

    /**
     * @param minimumValue
     * @param maximumValue
     * @return
     */
    public void addVerticalExent(Double minimumValue, Double maximumValue) {

	Element vertical = createElement("verticalextent", GEO_RSS_NS_URI);

	if (Objects.nonNull(minimumValue)) {
	    addContentTo(vertical, createSimpleElement("minimum", minimumValue.toString(), IIA_NS_URI));
	}

	if (Objects.nonNull(maximumValue)) {
	    addContentTo(vertical, createSimpleElement("maximum", maximumValue.toString(), IIA_NS_URI));
	}

	addElement(vertical);
    }

    /**
     * @param orgName
     * @param indName
     * @param email
     * @param role
     */
    public void addContributor(String orgName, String indName, String email, String role) {

	Element contributor = createElement("contributor");

	if (Objects.nonNull(orgName)) {
	    addContentTo(contributor, "orgName", orgName);
	}

	if (Objects.nonNull(indName)) {
	    addContentTo(contributor, "indName", indName);
	}

	if (Objects.nonNull(email)) {
	    addContentTo(contributor, "email", email);
	}

	if (Objects.nonNull(role)) {
	    addContentTo(contributor, "role", role);
	}

	addElement(contributor);
    }

    /**
     * @param tag
     */
    public void setSatelliteTag(SatelliteTag tag) {

	addElement(tag.getAcquisitionElement());
    }
    
    /**
     * @param insitu
     */
    public void setInSitu(Boolean isInSitu) {

	addSimpleElement("inSitu", isInSitu.toString(), NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * GEO Mountains
     */

    /**
     * @param GEO Mountain id
     */
    public void setGEOMountainsId(String id) {

	addSimpleElement("poi-id", id, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    public void setGEOMountainsOrg(String org) {

	addSimpleElement("poi-organization", org, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    public void setGEOMountainsNetwork(String network) {

	addSimpleElement("poi-network", network, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    public void setGEOMountainsCategory(String cat) {

	addSimpleElement("poi-category", cat, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    public void setGEOMountainsParameters(List<String> list) {
	String stringList = String.join(",", list);
	addSimpleElement("poi-parameters", stringList, NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

}
