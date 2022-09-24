package eu.essi_lab.ommdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Resource mappers provide mapping from {@link OriginalMetadata} to a {@link GSResource}
 *
 * @author Fabrizio
 */
public interface IResourceMapper extends Pluggable {

    /**
     * Creates a {@link GSResource} mapping the supplied <code>originalMD</code>.<br>
     * <h3>Implementation notes</h3>
     * The created {@link GSResource} <b>MUST</b> have the following characteristics:
     * <ul>
     * <li>the concrete type is determined through a classification according to a list of well-known types
     * (e.g. datasets, services, observation, etc..)</li>
     * <li>the {@link GSResource#getHarmonizedMetadata()} field is correctly set</li>
     * <li>the {@link GSResource#getOriginalMetadata()} field is correctly set</li>
     * <li>the {@link GSResource#getSource()} field is correctly set</li>
     * <li>the {@link GSResource#getOriginalId()} field is correctly (while private and public identifiers are set by
     * the {@link IdentifierDecorator}</li>
     * </ul>
     *
     * @param originalMD a non <code>null</code> uri {@link OriginalMetadata}
     * @param source a non <code>null</code> uri {@link GSSource}
     * @return
     * @throws GSException
     */
    public GSResource map(OriginalMetadata originalMD, GSSource source) throws GSException;

    /**
     * Returns the schema URI that can be handled by this mapper
     *
     * @return
     */
    public String getSupportedOriginalMetadataSchema();

    /**
     * Returns true if supports mapping fo the given original metadata
     * 
     * @param the original metadata record
     * @return
     */
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD);
}
