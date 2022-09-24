package eu.essi_lab.augmenter;

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

import java.util.Optional;
import java.util.Random;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class RandomSSCScoreAugmenter extends ResourceAugmenter<AugmenterSetting> {

    /**
     * 
     */
    public RandomSSCScoreAugmenter() {

	super();
    }

    /**
     * @param setting
     */
    public RandomSSCScoreAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	int score = new Random().nextInt(101);
	
	GSLoggerFactory.getLogger(getClass()).trace("Random score assigned: "+score);
	
	resource.getPropertyHandler().setSSCSCore(score);

	return Optional.of(resource);
    }

    @Override
    protected String initName() {

	return "Random SSC Score augmenter";
    }

    @Override
    public String getType() {

	return "RandomSSCScoreAugmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }
}
