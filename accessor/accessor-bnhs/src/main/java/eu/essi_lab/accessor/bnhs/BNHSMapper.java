package eu.essi_lab.accessor.bnhs;

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

import java.util.HashMap;
import java.util.Map;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

public class BNHSMapper extends OriginalIdentifierMapper {

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return "BNHS";
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	Dataset dataset = new Dataset();
	dataset.setSource(source);

	String md = originalMD.getMetadata();
	String[] split = md.split(BNHSPropertyReader.SEPARATOR);
	Map<BNHSProperty, String> table = new HashMap<>();
	int max = split.length - 1;
	for (int i = 0; i < max; i += 2) {
	    String key = split[i];
	    String value = split[i + 1];
	    BNHSProperty column = BNHSProperty.decode(key);
	    if (key != null) {
		table.put(column, value);
	    }
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIPlatform platform = new MIPlatform();

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	String id = table.get(BNHSProperty.HYCOSID);
	if (id != null) {
	    id = "wmo:hycos:id:" + id;
	    platform.setMDIdentifierCode(id);
	}

	String stationName = table.get(BNHSProperty.STATION_NAME);
	if (stationName != null) {
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " station");
	}

	String latitude = table.get(BNHSProperty.LATITUDE);
	String longitude = table.get(BNHSProperty.LONGITUDE);
	if (latitude != null && longitude != null) {
	    double lat = Double.parseDouble(latitude);
	    double lon = Double.parseDouble(longitude);
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	dataset.getPropertyHandler().setIsTimeseries(true);

	dataset.getExtensionHandler().setCountry(table.get(BNHSProperty.COUNTRY));

	return dataset;
    }

}
