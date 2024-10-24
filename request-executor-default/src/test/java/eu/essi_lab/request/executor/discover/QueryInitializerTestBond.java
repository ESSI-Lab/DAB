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
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Energy"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Energy"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Surface radiation budget"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Surface radiation budget"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Earth Radiation budget"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Earth Radiation budget"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Surface Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Surface Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Upper Air Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Upper Air Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Surface Air wind speed"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Surface Air wind speed"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Upper Air wind speed"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Upper Air wind speed"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Albedo"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Albedo"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "heat flux"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "heat flux"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Land surface temperture"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Land surface temperture"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Ocean surface heat flux"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Ocean surface heat flux"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Sea surface temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Sea surface temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Sea subsurface Temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Sea subsurface Temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Surface wind"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Surface wind"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Upper Air wind"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Upper Air wind"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Pressure"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Pressure"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Lightning"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Lightning"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Aerosol"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Aerosol"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Surfac currents"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Surfac currents"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "subsurface currents"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "subsurface currents"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Ocean surface stress"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Ocean surface stress"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Sea state"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Sea state"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Transient facies"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Transient facies"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Carbon cycle"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Carbon cycle"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "GHGs"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "GHGs"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Carbon dioxide"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Carbon dioxide"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Methane"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Methane"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "GHG"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "GHG"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Soil Carbon"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Soil Carbon"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Biomass"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Biomass"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Inorganic carbon"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Inorganic carbon"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Nitrous oxide"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Nitrous oxide"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Hydrosphere"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Hydrosphere"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Precipitation"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Precipitation"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Cloud properties"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Cloud properties"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "water vapour"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "water vapour"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "surface temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "surface temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Soil moisture"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Soil moisture"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "River discharge"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "River discharge"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Lakes"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Lakes"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Groundwater"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Groundwater"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Surface salinity"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Surface salinity"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Subsurface salinity"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Subsurface salinity"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "sea level"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "sea level"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "sea temperature"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "sea temperature"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Snow"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Snow"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Ice"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Ice"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Glaciers"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Glaciers"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Ice sheets"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Ice sheets"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Ice shelves"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Ice shelves"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Permafrost"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Permafrost"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Sea Ice"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Sea Ice"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Biosphere"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Biosphere"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Land cover"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Land cover"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Leaf Area Index"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Leaf Area Index"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "LAI"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "LAI"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE,
			"Fraction of Absorbed Photosynthetically Active Radiation"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD,
			"Fraction of Absorbed Photosynthetically Active Radiation"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "FAPAR"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "FAPAR"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Fire"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Fire"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Plankton"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Plankton"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Oxygen"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Oxygen"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Nutrients"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Nutrients"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Ocean color"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Ocean color"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Marine habitat"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Marine habitat"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Energy"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Energy"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Human use"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Human use"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Natural Resources"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Natural Resources"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "Water Use"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "Water Use"), //
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "GHG fluxes"),
		BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, "GHG fluxes")//

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
