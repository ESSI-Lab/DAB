package eu.essi_lab.session;

import java.io.IOException;

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

/**
 * Abstraction for storing and retrieving session tokens. Implementations may use local file
 * (single-node) or Redis (multi-node). The namespace identifies the target system (e.g. lombardia,
 * marche).
 */
public interface TokenStore {

    String readToken() throws IOException;

    void writeToken(String token) throws IOException;

    void deleteToken(String token) throws IOException;
}
