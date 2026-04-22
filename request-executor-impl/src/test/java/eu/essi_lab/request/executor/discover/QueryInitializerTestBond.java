package eu.essi_lab.request.executor.discover;

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.resource.MetadataElement;

public class QueryInitializerTestBond {

    public static Bond getBond() {

	LogicalBond keywordTitleBond = createKeywordTitleBond();

	Bond sourceBond = createSourceBond(200);

	LogicalBond ret = BondFactory.createAndBond(keywordTitleBond, sourceBond);

	ret = BondFactory.createAndBond(ret, createSourceBond(150));

	return ret;
    }

    private static LogicalBond createKeywordTitleBond() {
	return BondFactory.createOrBond(//
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Energy"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Energy"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Surface radiation budget"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Surface radiation budget"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Earth Radiation budget"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Earth Radiation budget"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Surface Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Surface Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Upper Air Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Upper Air Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Surface Air wind speed"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Surface Air wind speed"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Upper Air wind speed"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Upper Air wind speed"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Albedo"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Albedo"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "heat flux"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "heat flux"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Land surface temperture"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Land surface temperture"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Ocean surface heat flux"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Ocean surface heat flux"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Sea surface temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Sea surface temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Sea subsurface Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Sea subsurface Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Surface wind"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Surface wind"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Upper Air wind"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Upper Air wind"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Pressure"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Pressure"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Lightning"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Lightning"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Aerosol"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Aerosol"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Surfac currents"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Surfac currents"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "subsurface currents"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "subsurface currents"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Ocean surface stress"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Ocean surface stress"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Sea state"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Sea state"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Transient facies"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Transient facies"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Carbon cycle"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Carbon cycle"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "GHGs"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "GHGs"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Carbon dioxide"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Carbon dioxide"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Methane"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Methane"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "GHG"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "GHG"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Soil Carbon"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Soil Carbon"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Biomass"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Biomass"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Inorganic carbon"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Inorganic carbon"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Nitrous oxide"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Nitrous oxide"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Hydrosphere"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Hydrosphere"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Precipitation"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Precipitation"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Cloud properties"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Cloud properties"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "water vapour"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "water vapour"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "surface temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "surface temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Soil moisture"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Soil moisture"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "River discharge"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "River discharge"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Lakes"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Lakes"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Groundwater"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Groundwater"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Surface salinity"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Surface salinity"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Subsurface salinity"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Subsurface salinity"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "sea level"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "sea level"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "sea temperature"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "sea temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Snow"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Snow"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Ice"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Ice"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Glaciers"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Glaciers"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Ice sheets"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Ice sheets"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Ice shelves"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Ice shelves"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Permafrost"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Permafrost"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Sea Ice"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Sea Ice"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Biosphere"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Biosphere"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Land cover"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Land cover"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Leaf Area Index"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Leaf Area Index"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "LAI"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "LAI"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE,
			"Fraction of Absorbed Photosynthetically Active Radiation"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD,
			"Fraction of Absorbed Photosynthetically Active Radiation"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "FAPAR"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "FAPAR"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Fire"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Fire"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Plankton"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Plankton"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Oxygen"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Oxygen"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Nutrients"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Nutrients"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Ocean color"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Ocean color"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Marine habitat"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Marine habitat"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Energy"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Energy"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Human use"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Human use"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Natural Resources"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Natural Resources"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "Water Use"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "Water Use"), //
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "GHG fluxes"),
		BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.KEYWORD, "GHG fluxes")//

	);
    }

    private static Bond createSourceBond(int j) {
	List<Bond> operands = new ArrayList<Bond>();
	for (int i = 0; i < j; i++) {
	    Bond sourceBond = BondFactory.createSourceIdentifierBond("s-" + i);
	    operands.add(sourceBond);
	}
	return BondFactory.createOrBond(operands);
    }

}
