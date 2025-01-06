package eu.essi_lab.accessor.wof;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.InterpolationType;

public class WML_1_1_INAMapper extends WML_1_1Mapper {

    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.WML1_INA_NS_URI;
    }

    public String getSourceOrganization(TimeSeries series) {
	return "Instituto Nacional del Agua (INA)";
    }

    // private boolean is30DaysSeries(TimeSeries series) {
    // String unit = super.getTimeScaleUnitAbbreviation(series);
    // Number support = super.getTimeScaleTimeSupport(series);
    // return unit.equals("day") && support.longValue() == 30;
    //
    // }
    //
    // @Override
    // public Number getTimeScaleTimeSupport(TimeSeries series) {
    // if (is30DaysSeries(series)) {
    // return 1;
    // }
    // return super.getTimeScaleTimeSupport(series);
    // }
    //
    // @Override
    // public String getTimeScaleUnitName(TimeSeries series) {
    //// if (is30DaysSeries(series)) {
    //// return "month";
    //// }
    // return super.getTimeScaleUnitName(series);
    // }
    //
    // @Override
    // public String getTimeScaleUnitAbbreviation(TimeSeries series) {
    //// if (is30DaysSeries(series)) {
    //// return "mon";
    //// }
    // return super.getTimeScaleUnitAbbreviation(series);
    // }

    public String getUnitName(TimeSeries series) {
	String ret = series.getUnitName();
	if (ret.equals("milímetros por día")) {
	    return "milímetros";
	}
	return ret;
    }

    @Override
    public InterpolationType getInterpolationType(TimeSeries series) {
	InterpolationType interpolation = super.getInterpolationType(series);
	InterpolationType proposal = null;
	String variableCode = series.getVariableCode();
	switch (variableCode) {
	case "50":
	    proposal = InterpolationType.MAX_SUCC;
	    break;
	case "1":
	case "31":
	case "34":
	    proposal = InterpolationType.TOTAL_SUCC;
	    break;
	case "27":
	case "38":
	    proposal = InterpolationType.TOTAL_PREC;
	    break;
	case "48":
	case "39":
	case "52":
	case "40":
	case "51":
	case "49":
	case "33":
	case "61":
	case "56":
	case "59":
	case "54":
	    proposal = InterpolationType.AVERAGE_SUCC;
	    break;
	case "14":
	    proposal = InterpolationType.AVERAGE_PREC;
	    break;
	default:
	    break;
	}
	if (proposal != null && interpolation != null) {
	    switch (proposal) {
	    case MAX_SUCC:
		if (!interpolation.equals(InterpolationType.MAX)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    case TOTAL_PREC:
	    case TOTAL_SUCC:
		if (!interpolation.equals(InterpolationType.TOTAL)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    case AVERAGE_PREC:
	    case AVERAGE_SUCC:
		if (!interpolation.equals(InterpolationType.AVERAGE)) {
		    errorRefine(interpolation, proposal);
		} else {
		    interpolation = proposal;
		}
		break;
	    default:
		break;
	    }
	}
	return interpolation;
    }

    private void errorRefine(InterpolationType interpolation, InterpolationType proposal) {
	GSLoggerFactory.getLogger(getClass()).error("Not possible to refine from: {} to {}", interpolation, proposal);
    }
}
