package eu.essi_lab.accessor.bnhs;

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

import java.net.URL;
import java.util.Optional;

import eu.essi_lab.lib.net.googlesheet.GoogleTableSheetClient;
import eu.essi_lab.model.resource.BNHSProperty;

public class BNHSClient extends GoogleTableSheetClient {

    /**
     * @throws Exception
     */
    public BNHSClient() throws Exception {

	// Default BNHS list spreadsheet
	super("1ni9_BNcgoWD5HcU0sT_E20CwcOpOjsdek7_fQd4DWtI", "Station List - operational", "HYCOSID"); // spreadsheet
    }

    /**
     * @param stylesheetURL
     * @throws Exception
     */
    public BNHSClient(URL stylesheetURL) throws Exception {

	super(extractStylesheetID(stylesheetURL), "Station List - operational", "HYCOSID");
    }

    @Override
    protected String getApplicationName() {
	return "DAB - WHOS broker BNHS accessor";
    }

    @Override
    protected Optional<String> getCredentialsFilePath() {
	return Optional.of("bnhs/serviceAccount.json");
    }

    public String getValue(String hycosId, BNHSProperty column) {
	return super.getValueByKey(hycosId, column.getLabel());
    }

    // public String getValue(Integer row, BNHSProperty column) {
    // return super.getValue(row, column.getLabel());
    // }

}
