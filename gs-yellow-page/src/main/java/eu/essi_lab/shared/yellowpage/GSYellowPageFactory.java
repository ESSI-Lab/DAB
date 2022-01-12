package eu.essi_lab.shared.yellowpage;

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

import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.shared.SHARED_CONTENT_TYPES;
import eu.essi_lab.shared.SharedRepositoryFactory;
import eu.essi_lab.shared.model.CahcheGSSharedContentCategory;
import eu.essi_lab.shared.model.IGSSharedContentCategory;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.repository.SharedRepository;
public class GSYellowPageFactory {

    private GSYellowPageFactory() {
	//force using static
    }

    public static GSYellowPage getYellowPageReader(SharedRepositoryInfo configuration) {

	SharedRepository repo = SharedRepositoryFactory.getSharedRepository(configuration);

	return getYP(repo);

    }

    static GSYellowPage getYP(SharedRepository repository) {
	IGSSharedContentCategory category = new CahcheGSSharedContentCategory();

	SharedContentType type = new SharedContentType();

	type.setType(SHARED_CONTENT_TYPES.GS_YELLOW_PAGE_TYPE);

	repository.setCategory(category);
	repository.setType(type);

	GSYellowPage yp = new GSYellowPage();

	yp.setRepository(repository);

	return yp;
    }
}
