package eu.essi_lab.pdk.rsm.impl.json.sem;

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

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.GSKnowledgeSchemeLoader;
import eu.essi_lab.model.ontology.d2k.D2KGSKnowledgeScheme;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.rsm.SemanticResponseMapper;

/**
 * @author Fabrizio
 */
public class JSONSemanticResponseMapper extends SemanticResponseMapper<JSONObject> {

    private static final String MISSING_DESCRIPTION = "Missing description";

    private static final String MISSING_NAME = "Missing name";
    private static final String MISSING_LANGUAGE = "Missing language";
    private static final String MISSING_ID = "Missing id";

    private static final String DESCRIPTION_KEY = "description";
    private static final String NAME_KEY = "name";

    private static final String ID_KEY = "id";
    private static final String SOURCE_KEY = "source";

    @Override
    protected JSONObject mapEntryPoint(GSKnowledgeResourceDescription entryPoint) {

	JSONObject object = new JSONObject();

	object.put(DESCRIPTION_KEY, !entryPoint.getValues(D2KGSPredicate.DEFINITION).isEmpty()
		? entryPoint.getValues(D2KGSPredicate.DEFINITION).get(0).stringValue() : MISSING_DESCRIPTION);

	object.put(NAME_KEY, !entryPoint.getValues(D2KGSPredicate.LABEL).isEmpty()
		? entryPoint.getValues(D2KGSPredicate.LABEL).get(0).stringValue() : MISSING_NAME);

	object.put(ID_KEY, entryPoint.getResource().stringValue());

	return object;
    }

    @Override
    protected JSONObject mapParentConcept(GSKnowledgeResourceDescription parentConcept) {

	return mapChildConcept(parentConcept).getJSONObject("concept");
    }

    protected JSONObject mapChildConcept(GSKnowledgeResourceDescription childConcept) {

	//
	// get the scheme
	//
	String namespace = childConcept.getResource().getNamespace();
	GSKnowledgeScheme scheme = GSKnowledgeSchemeLoader.loadScheme(namespace).orElse(D2KGSKnowledgeScheme.getInstance());

	JSONObject out = new JSONObject();

	JSONObject concept = new JSONObject();
	out.put("concept", concept);

	concept.put(ID_KEY, childConcept.getResource().stringValue() != null ? childConcept.getResource().stringValue() : MISSING_ID);

	//
	// labels -> name
	//
	setProperty(childConcept, scheme, childConcept.getValues(scheme.getLabelPredicate()), concept, NAME_KEY, MISSING_NAME);

	//
	// abstracts -> description
	//
	setProperty(childConcept, scheme, childConcept.getValues(scheme.getAbstractPredicate()), concept, DESCRIPTION_KEY,
		MISSING_DESCRIPTION);

	concept.put("image_url", "No URL provided");

	JSONObject geo = new JSONObject();
	concept.put("geo", geo);

	JSONObject source = new JSONObject();

	source.put(DESCRIPTION_KEY, MISSING_DESCRIPTION);
	source.put(NAME_KEY, MISSING_NAME);
	source.put(ID_KEY, MISSING_ID);

	Optional.ofNullable(childConcept.getResource().getSource()).ifPresent(ontology -> {

	    source.put(DESCRIPTION_KEY, ontology.getDescription() != null ? ontology.getDescription() : MISSING_DESCRIPTION);
	    source.put(NAME_KEY, ontology.getName() != null ? ontology.getName() : MISSING_NAME);
	    source.put(ID_KEY, ontology.getId() != null ? ontology.getId() : MISSING_ID);

	});

	concept.put(SOURCE_KEY, source);

	JSONArray jsonArray = new JSONArray();
	concept.put("linkedKnwoledgeResourceTypes", jsonArray);

	JSONObject relationToParent = new JSONObject();

	Optional.ofNullable(childConcept.getRelationToParent()).ifPresent(rtp -> {

	    relationToParent.put(ID_KEY, rtp.getRelation().stringValue());

	    relationToParent.put(NAME_KEY, rtp.getRelationName());

	    relationToParent.put(DESCRIPTION_KEY, rtp.getRelationDescription());

	    JSONObject rsource = new JSONObject();

	    rsource.put(DESCRIPTION_KEY, MISSING_DESCRIPTION);
	    rsource.put(NAME_KEY, MISSING_NAME);
	    rsource.put(ID_KEY, MISSING_ID);

	    Optional.ofNullable(rtp.getOntology()).ifPresent(ontology -> {

		rsource.put(DESCRIPTION_KEY, ontology.getDescription() != null ? ontology.getDescription() : MISSING_DESCRIPTION);
		rsource.put(NAME_KEY, ontology.getName() != null ? ontology.getName() : MISSING_NAME);
		rsource.put(ID_KEY, ontology.getId() != null ? ontology.getId() : MISSING_ID);

	    });

	    relationToParent.put(SOURCE_KEY, rsource);
	});

	out.put("relationToParent", relationToParent);

	return out;
    }

    /**
     * @param childConcept
     * @param scheme
     * @param values
     * @param concept
     * @param key
     * @param missing
     */
    private void setProperty(//
	    GSKnowledgeResourceDescription childConcept, //
	    GSKnowledgeScheme scheme, //
	    List<Value> values, //
	    JSONObject concept, //
	    String key, //
	    String missing) {

	if (values.isEmpty()) {

	    concept.put(key, missing);

	} else {

	    JSONArray descriptions = new JSONArray();
	    concept.put(key, descriptions);

	    for (Value val : values) {

		String labelValue = ((Literal) val).getLabel();
		String language = ((Literal) val).getLanguage().orElse(MISSING_LANGUAGE);

		JSONObject languageObject = new JSONObject();
		languageObject.put("label", labelValue);
		languageObject.put("language", language);

		descriptions.put(languageObject);
	    }
	}
    }

    @Override
    public MappingSchema getMappingSchema() {

	return null;
    }
}
