package eu.essi_lab.augmenter;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public abstract class Augmenter<S extends AugmenterSetting> implements Configurable<S> {

    /**
    * 
    */
    private S setting;

    /**
     * 
     */
    public Augmenter() {

	init(initSetting());
    }

    /**
     * @param setting
     */
    public Augmenter(S setting) {

	init(setting);
    }

    /**
     * @param setting
     */
    protected void init(S setting) {

	this.setting = setting;
	this.setting.setName(initName());
	this.setting.setConfigurableType(getType());
    }

    /**
     * @return
     */
    protected abstract S initSetting();

    /**
     * @return
     */
    protected abstract String initName();

    /**
     * @param setting
     * @return
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static Augmenter<?> createInstance(AugmenterSetting setting) throws RuntimeException {

	try {
	    return setting.createConfigurable();

	} catch (Exception e) {

	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void configure(S setting) {

	this.setting = setting;
    }

    @Override
    public S getSetting() {

	return this.setting;
    }

    /**
     * Augments the given resource (if possible) and returns the related {@link Optional}
     * 
     * @param resource resource to augment
     * @return
     * @throws GSException
     */
    public abstract Optional<GSResource> augment(GSResource resource) throws GSException;

    /**
     * Generates a list (possible empty) of {@link GSKnowledgeResourceDescription}s from the supplied
     * <code>resource</code>
     * 
     * @param resource
     * @return
     * @throws GSException
     */
    public abstract List<GSKnowledgeResourceDescription> generate(GSResource resource) throws GSException;

}
