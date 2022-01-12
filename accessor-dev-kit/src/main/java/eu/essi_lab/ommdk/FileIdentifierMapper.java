package eu.essi_lab.ommdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public abstract class FileIdentifierMapper extends AbstractResourceMapper {

    /**
     * This implementation generates the original identifier according to the {@link CoreMetadata#getIdentifier()}
     * which is the identifier provided in the original metadata. This approach is valid only if the metadata is
     * compliant with schemas which provide a unique identifier such as ISO 19115/2 or Dublin Core
     * 
     * @param resource
     * @return
     */
    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	return resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();
    }
}
