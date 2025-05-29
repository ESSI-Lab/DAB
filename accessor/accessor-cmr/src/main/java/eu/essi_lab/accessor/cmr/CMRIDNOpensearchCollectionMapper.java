package eu.essi_lab.accessor.cmr;

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

import org.slf4j.Logger;

import eu.essi_lab.accessor.mapper.OpensearchCollectionMapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author roncella
 */
public class CMRIDNOpensearchCollectionMapper extends OpensearchCollectionMapper {

    public static final String SCHEMA_URI = "http://essi-lab.eu/cmr/collections/IDN";
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public final static String IDN_BASE_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?";

    @Override
    protected boolean skipCwic() {
	return true;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CMRIDNOpensearchCollectionMapper.SCHEMA_URI;
    }

    @Override
    protected String getGranulesBaseURL() {
	return IDN_BASE_TEMPLATE_URL;
    }

}
