package eu.essi_lab.accessor.hiscentral.utils;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.InterpolationType;

public class HISCentralUtils {

    public static void addDefaultAttributeDescription(Dataset dataset, CoverageDescription coverageDescription) {

	String parameterName = coverageDescription.getAttributeTitle();

	Optional<String> uom = dataset.getExtensionHandler().getAttributeUnits();

	String units = uom.isPresent() ? " (" + uom.get() + ")" : "";

	String attributeDescription = parameterName + units;

	coverageDescription.setAttributeDescription(attributeDescription);

    }

    /**
     * Parse the given string to findout a token associated to an interpolation type
     * 
     * @param string
     * @return a couple composed by the interpolation type and the token associated to the interpolation found in the
     *         stirng
     */
    public static DescriptionParsingResult parseDescription(String string) {

	for (InterpolationType interpolation : InterpolationType.values()) {
	    List<String> associatedTerms = getAssociatedTerms(interpolation);
	    for (String associatedTerm : associatedTerms) {
		if (string.toLowerCase().contains(associatedTerm.toLowerCase())) {
		    int i = string.toLowerCase().indexOf(associatedTerm.toLowerCase());
		    if (i > 0 && string.charAt(i - 1) != ' ') {
			continue;
		    }
		    int e = i + associatedTerm.length();
		    if (e < string.length() && string.charAt(e) != ' ') {
			continue;
		    }
		    String term = string.substring(i, i + associatedTerm.length());
		    return new DescriptionParsingResult(string, interpolation, term);
		}
	    }
	}
	return new DescriptionParsingResult(string, null, null);

    }

    private static List<String> getAssociatedTerms(InterpolationType interpolation) {
	List<String> ret = new ArrayList<>();
	ret.add(interpolation.getLabel());
	switch (interpolation) {
	case AVERAGE:
	    ret.add("media");
	    ret.add("medio");
	    ret.add("medie");
	    ret.add("medi");
	    break;
	case MAX:
	    ret.add("massimo");
	    ret.add("massima");
	    ret.add("massimi");
	    ret.add("massime");
	    ret.add("max");
	    break;
	case MIN:
	    ret.add("minimo");
	    ret.add("minima");
	    ret.add("minime");
	    ret.add("minimi");
	    ret.add("min");
	    break;
	case TOTAL:
	    ret.add("totali");
	    ret.add("totale");
	    break;
	default:
	    break;
	}
	return ret;
    }

    public static void main(String[] args) {
	String s = "Durata del vento a 10m da E";
	s = "Radiazione globale totale";
	s = "Media del vento filato a 2m";
	s = "Temperatura aria a 2m";
	DescriptionParsingResult parsed = HISCentralUtils.parseDescription(s);
	System.out.println(parsed.getInterpolation());
	System.out.println(parsed.getRest());
    }
}
