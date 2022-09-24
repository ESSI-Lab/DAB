/**
 * 
 */
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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public abstract class SHA1IdentifierMapper extends AbstractResourceMapper {

    /**
     * 
     */
    public SHA1IdentifierMapper() {
    }

    /**
     * This implementation generates the original identifier using the SHA-1 algorithm. The generated identifier
     * starts with {@link StringUtils#SHA1_IDENTIFIER}.<br>
     * This guarantees that resources
     * with same original identifier has also the same original metadata, thus they can be considered equals
     * and the {@link IdentifierDecorator} algorithm can properly work
     * 
     * @param resource
     */
    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	String originalMetadata = resource.getOriginalMetadata().getMetadata();
	originalMetadata = originalMetadata.replace(System.getProperty("line.separator"), "");
	originalMetadata = originalMetadata.replace("\n", "");
	originalMetadata = originalMetadata.replace("\r", "");

	String identifier = null;

	try {
	    identifier = StringUtils.hashSHA1messageDigest(originalMetadata);
	    identifier = StringUtils.SHA1_IDENTIFIER + identifier;

	} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}

	return identifier;
    }

}
