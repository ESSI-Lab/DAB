package eu.essi_lab.model.ontology;

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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class GSKnowledgeSchemeLoader {

    /**
     * @return
     */
    public static Optional<GSKnowledgeScheme> loadScheme(String nameSpace) {

	Iterator<GSKnowledgeScheme> it = ServiceLoader.load(GSKnowledgeScheme.class).iterator();

	return StreamUtils.iteratorToStream(it).filter(s -> s.getNamespace().equals(nameSpace)).findFirst();
    }

    /**
     * @return
     */
    public static List<GSKnowledgeScheme> loadSchemes() {

	Iterator<GSKnowledgeScheme> it = ServiceLoader.load(GSKnowledgeScheme.class).iterator();

	return StreamUtils.iteratorToStream(it).collect(Collectors.toList());
    }
}
