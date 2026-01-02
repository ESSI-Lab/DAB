package eu.essi_lab.lib.odip.rosetta;

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

import java.util.Set;

public abstract class RosettaStone {

    /**
     * Gets the translations of the given term
     * 
     * @param term
     * @return
     */
    public abstract Set<String> getTranslations(String term);

    /**
     * Gets the terms narrower wrt the given term
     * 
     * @param term
     * @return
     */
    public abstract Set<String> getNarrower(String term);

    /**
     * Gets the terms broader wrt the given term
     * 
     * @param term
     * @return
     */
    public abstract Set<String> getBroader(String term);

    /**
     * NERC terms can be easily translated to SDN terms, using this method
     * 
     * @param term
     * @return
     */
    public String translateNERCtoSDN(String term) {
	term = term.replace("http://vocab.nerc.ac.uk/collection/", "");
	if (term.endsWith("/")) {
	    term = term.substring(0, term.length() - 1);
	}
	String list = term.substring(0, term.indexOf("/"));
	String value = term.substring(term.lastIndexOf("/") + 1);
	return "http://www.seadatanet.org/urnurl/SDN:" + list + "::" + value + "/";
    }

    /**
     * SDN terms can be easily translated to NERC terms, using this method
     * 
     * @param term
     * @return
     */
    public String translateSDNtoNERC(String term) {
	term = term.replace("http://www.seadatanet.org/urnurl/SDN:", "");
	if (term.endsWith("/")) {
	    term = term.substring(0, term.length() - 1);
	}
	String list = term.substring(0, term.indexOf("::"));
	String value = term.substring(term.lastIndexOf(":") + 1);
	return "http://vocab.nerc.ac.uk/collection/" + list + "/current/" + value + "/";
    }

}
