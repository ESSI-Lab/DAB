package eu.essi_lab.accessor.waf.dirlisting;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.NameSpace;
 
/**
 * @author Fabrizio
 */
public class DirectoryListingConnectorSetting extends HarvestedConnectorSetting {

    /**
     * 
     */
    private static final String METADATA_SCHEMA_OPTION_KEY = "metadataSchema";

    /**
     * 
     */
    public DirectoryListingConnectorSetting() {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.CSW_NS_URI);
	ret.add(CommonNameSpaceContext.OAI_DC_NS_URI);
	ret.add(CommonNameSpaceContext.GMD_NS_URI);
	ret.add(CommonNameSpaceContext.GMI_NS_URI);
	ret.add(NameSpace.GS_DATA_MODEL_SCHEMA_URI);
	ret.add(CommonNameSpaceContext.MULTI);

	Option<String> option = StringOptionBuilder.get().//
		withKey(METADATA_SCHEMA_OPTION_KEY).//
		withLabel("Metadata schema").//
		withSingleSelection().//
		withValues(ret).//
		withSelectedValue(CommonNameSpaceContext.GMD_NS_URI).//
		build();

	addOption(option);
    }

    /**
     * @return
     */
    public String getSelectedSchema() {

	return getOption(METADATA_SCHEMA_OPTION_KEY, String.class).get().getSelectedValue();
    }

    @Override
    protected String initConnectorType() {

	return DirectoryListingConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Directory Listing Connector options";
    }
}
