package eu.essi_lab.accessor.hiscentral.lombardia;

import eu.essi_lab.session.FileSessionCoordinator;

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
 * Lombardia session coordinator for single-node: delegates to {@link FileSessionCoordinator}.
 */
public class FileLombardiaSessionCoordinator implements LombardiaSessionCoordinator {

    private final FileSessionCoordinator delegate = new FileSessionCoordinator();

    @Override
    public <T> T runWithExclusiveSession(HISCentralLombardiaClient client, SessionWork<T> work) throws Exception {
	return delegate.runWithExclusiveSession(token -> {
	    if (token != null) {
		client.logoutWithToken(token);
	    }
	}, work);
    }
}
