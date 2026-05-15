package eu.essi_lab.cdk;

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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.setting.connector.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.pluggable.*;

/**
 * Generic Connector Interface.
 *
 * @author roncella
 */
public interface IDriverConnector<T extends ConnectorSetting> extends Configurable<T>, Pluggable {

    /**
     * @param source
     * @return
     */
    boolean supports(GSSource source);

    /**
     * @param url
     */
    void setSourceURL(String url);

    /**
     * @return
     */
    String getSourceURL();

    /**
     * @return
     */
    default boolean supportsPreview() {

	return true;
    }

}
