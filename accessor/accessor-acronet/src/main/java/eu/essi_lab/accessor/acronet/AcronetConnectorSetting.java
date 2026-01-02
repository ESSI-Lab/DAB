package eu.essi_lab.accessor.acronet;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Roberto
 */
public class AcronetConnectorSetting extends HarvestedConnectorSetting {

    
    
    public AcronetConnectorSetting() {

	
    }

    /**
     * @param object
     */
    public AcronetConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AcronetConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return AcronetConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Acronet Connector settings";
    }
}
