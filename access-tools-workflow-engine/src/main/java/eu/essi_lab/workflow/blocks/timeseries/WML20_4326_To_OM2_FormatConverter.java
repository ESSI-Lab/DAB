package eu.essi_lab.workflow.blocks.timeseries;

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

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.DataFormatConverter;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.timeseries.WML20_To_OM2_Processor;

/**
 * @author boldrini
 */
public class WML20_4326_To_OM2_FormatConverter extends DataFormatConverter {

    public WML20_4326_To_OM2_FormatConverter() {
	super(DataType.TIME_SERIES, //
		CRS.EPSG_4326(), //
		DataFormat.WATERML_2_0(), // INPUT FORMAT
		DataFormat.O_M() // OUTPUT FORMAT
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new WML20_To_OM2_Processor();
    }
}
