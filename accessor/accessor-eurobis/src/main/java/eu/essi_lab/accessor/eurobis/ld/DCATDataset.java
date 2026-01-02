package eu.essi_lab.accessor.eurobis.ld;

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

import java.io.IOException;
import java.io.InputStream;

/**
 * @author boldrini
 */
public class DCATDataset extends RDFResource {

    @Override
    public RDFElement[] getElements() {
	return new RDFElement[] { //
		RDFElement.TITLE, //
		RDFElement.IDENTIFIER, //
		RDFElement.ALTERNATENAME, //
		RDFElement.ABSTRACT, //
		RDFElement.CREATED, //
		RDFElement.MODIFIED, //
		RDFElement.LICENSE, //
		RDFElement.ISPARTOF, //
		RDFElement.CITATIONS, //
		RDFElement.STARTDATE, //
		RDFElement.ENDDATE, //
		RDFElement.ENDDATEINPROGRESS, //
		RDFElement.BBOX, //
		RDFElement.THEMES, //
		RDFElement.KEYWORDS, //
		RDFElement.KEYWORDLABELSANDURISANDTYPES, //
		RDFElement.PARAMETERS, //
		RDFElement.PARAMETERLABELSANDURIS, //
		RDFElement.INSTRUMENTS, //
		RDFElement.INSTRUMENTLABELSANDURIS, //
		RDFElement.URLS_AND_TYPES, //
		RDFElement.CREATORS
	};
    }

    public DCATDataset(InputStream stream) throws IOException {
	super(stream);
    }

}
