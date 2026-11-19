package eu.essi_lab.lib.skos.llm.model;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
 * Mirrors the standard SKOS semantic relation vocabulary, plus an OTHER bucket for relations the model proposes that don't map cleanly onto
 * SKOS (e.g. "is a synonym in domain X", "commonly co-occurs with").
 * The {@code skosProperty} value is what a caller would use if they wanted to round-trip a result back into an actual SKOS graph (e.g.
 * skos:broader).
 */
public enum SKOSRelationType {

    BROADER("skos:broader"),//
    NARROWER("skos:narrower"),//
    RELATED("skos:related"),//
    EXACT_MATCH("skos:exactMatch"),//
    CLOSE_MATCH("skos:closeMatch"),//
    BROAD_MATCH("skos:broadMatch"),//
    NARROW_MATCH("skos:narrowMatch"),//
    RELATED_MATCH("skos:relatedMatch"),//

    CUSTOM(null),//

    /**
     * Non-SKOS relation; see {@link eu.essi_lab.lib.skos.llm.model.RelatedTerm#otherRelationLabel()}.
     */
    OTHER(null);

    /**
     *
     */
    private final String skosProperty;

    /**
     * @param skosProperty
     */
    SKOSRelationType(String skosProperty) {

        this.skosProperty = skosProperty;
    }

    /**
     * @return
     */
    public String skosProperty() {

        return skosProperty;
    }
}
