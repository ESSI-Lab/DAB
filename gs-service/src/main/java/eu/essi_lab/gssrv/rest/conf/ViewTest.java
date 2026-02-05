package eu.essi_lab.gssrv.rest.conf;

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.messages.bond.spatial.*;
import eu.essi_lab.model.resource.*;

import java.util.*;

public class ViewTest {

    public static void main(String[] args) throws java.lang.Exception {

	String viewId_ = "viewId";
	String viewLabel = "viewLabel";

	LogicalBond andBond = BondFactory.createAndBond();

	// --------------------------
	//
	// resource property bonds
	//

	//
	// resource time stamp bond with all the maths operator
	//

	ResourcePropertyBond sourceIdBond = BondFactory.createSourceIdentifierBond("sourceIdentifier");

	ResourcePropertyBond resTimeStampBond1 = BondFactory.createResourceTimeStampBond(BondOperator.EQUAL,
		ISO8601DateTimeUtils.getISO8601DateTime());

	ResourcePropertyBond resTimeStampBond2 = BondFactory.createResourceTimeStampBond(BondOperator.LESS,
		ISO8601DateTimeUtils.getISO8601DateTime());

	ResourcePropertyBond resTimeStampBond3 = BondFactory.createResourceTimeStampBond(BondOperator.GREATER,
		ISO8601DateTimeUtils.getISO8601DateTime());

	ResourcePropertyBond resTimeStampBond4 = BondFactory.createResourceTimeStampBond(BondOperator.LESS_OR_EQUAL,
		ISO8601DateTimeUtils.getISO8601DateTime());

	ResourcePropertyBond resTimeStampBond5 = BondFactory.createResourceTimeStampBond(BondOperator.GREATER_OR_EQUAL,
		ISO8601DateTimeUtils.getISO8601DateTime());

	andBond.getOperands()
		.add(BondFactory.createOrBond(sourceIdBond, resTimeStampBond1, resTimeStampBond2, resTimeStampBond3, resTimeStampBond4,
			resTimeStampBond5));

	//
	// other resource property bond with boolean value
	//

	ResourcePropertyBond isExecutableBond = BondFactory.createIsExecutableBond(true);
	ResourcePropertyBond isDownloadableBond = BondFactory.createIsDownloadableBond(true);
	ResourcePropertyBond isTimeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	ResourcePropertyBond isGridBond = BondFactory.createIsGridBond(true);
	ResourcePropertyBond isVectorBond = BondFactory.createIsVectorBond(true);
	ResourcePropertyBond isRatingCurveBond = BondFactory.createIsRatingCurveBond(true);
	ResourcePropertyBond isTransformableBond = BondFactory.createIsTransformableBond(true);

	andBond.getOperands().add(BondFactory.createOrBond(isExecutableBond, isDownloadableBond, isTimeSeriesBond, isGridBond, isVectorBond,
		isRatingCurveBond, isTransformableBond));

	// --------------
	//
	// view bond
	//

	ViewBond viewBond = BondFactory.createViewBond("anotherViewIdentifier");

	andBond.getOperands().add(viewBond);

	// --------------
	//
	// not bond
	//

	LogicalBond notBond = BondFactory.createNotBond(BondFactory.createSourceIdentifierBond("excludedSourceIdentifier"));

	andBond.getOperands().add(notBond);

	// ----------------------------------
	//
	// Metadata element properties bonds
	//

	//
	// spatial extent
	//

	SpatialExtent extent = new SpatialExtent();
	extent.setEast(51.1);
	extent.setNorth(38.2);
	extent.setSouth(-34.6);
	extent.setWest(-17.3);

	SpatialBond spatialBond1 = BondFactory.createSpatialEntityBond(BondOperator.INTERSECTS, extent);
	SpatialBond spatialBond2 = BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, extent);
	SpatialBond spatialBond3 = BondFactory.createSpatialEntityBond(BondOperator.DISJOINT, extent);

	andBond.getOperands().add(BondFactory.createOrBond(spatialBond1, spatialBond2, spatialBond3));

	//
	// exists and not exists simple value bond
	//

	SimpleValueBond existsSimpleValueBond = BondFactory.createExistsSimpleValueBond(MetadataElement.TITLE);
	SimpleValueBond notExistsSimpleValueBond = BondFactory.createNotExistsSimpleValueBond(MetadataElement.ABSTRACT);

	andBond.getOperands().add(BondFactory.createOrBond(existsSimpleValueBond, notExistsSimpleValueBond));

	//
	// string metadata elements with equal, not equal, text search operators (to be completed ?)
	//

	SimpleValueBond titleBond1 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title");
	SimpleValueBond titleBond2 = BondFactory.createSimpleValueBond(BondOperator.NOT_EQUAL, MetadataElement.TITLE, "title");
	SimpleValueBond titleBond3 = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "*tit*");

	andBond.getOperands().add(BondFactory.createOrBond(titleBond1, titleBond2, titleBond3));

	//
	// string metadata elements with math operators
	//

	SimpleValueBond tempBond1 = BondFactory.createSimpleValueBond(BondOperator.LESS, MetadataElement.TEMP_EXTENT_BEGIN,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond2 = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond3 = BondFactory.createSimpleValueBond(BondOperator.GREATER, MetadataElement.TEMP_EXTENT_BEGIN,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond4 = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond10 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TEMP_EXTENT_BEGIN,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond11 = BondFactory.createSimpleValueBond(BondOperator.NOT_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond5 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TEMP_EXTENT_END,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond6 = BondFactory.createSimpleValueBond(BondOperator.LESS, MetadataElement.TEMP_EXTENT_END,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond7 = BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_END,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond8 = BondFactory.createSimpleValueBond(BondOperator.GREATER, MetadataElement.TEMP_EXTENT_END,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond9 = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_END,
		"2000-01-01T00:00:00Z");
	SimpleValueBond tempBond12 = BondFactory.createSimpleValueBond(BondOperator.NOT_EQUAL, MetadataElement.TEMP_EXTENT_END,
		"2000-01-01T00:00:00Z");

	andBond.getOperands()
		.add(BondFactory.createOrBond(tempBond1, tempBond2, tempBond3, tempBond4, tempBond5, tempBond6, tempBond7, tempBond8,
			tempBond9, tempBond9, tempBond10, tempBond11, tempBond12));

	//
	// long, integer and double metadata element with math operators

	SimpleValueBond longBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.DATA_SIZE, 10L);

	SimpleValueBond intBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.DENOMINATOR, 10);

	SimpleValueBond doubleBond1 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.DISTANCE_VALUE, 10.0);

	SimpleValueBond doubleBond2 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.BAND_RESOLUTION, 10.0);

	SimpleValueBond doubleBond3 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.BAND_PEAK_RESPONSE_WL, 10.0);

	SimpleValueBond doubleBond4 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ILLUMINATION_ZENITH_ANGLE,
		10.0);

	SimpleValueBond doubleBond5 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.CLOUD_COVER_PERC, 10.0);

	SimpleValueBond doubleBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ILLUMINATION_AZIMUTH_ANGLE,
		10.0);

	SimpleValueBond doubleBond6 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ESSI_SPATIAL_RESOLUTION_X,
		10.0);

	SimpleValueBond doubleBond7 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ESSI_SPATIAL_RESOLUTION_Y,
		10.0);

	SimpleValueBond doubleBond8 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ESSI_TEMPORAL_RESOLUTION, 10.0);

	andBond.getOperands()
		.add(BondFactory.createOrBond(longBond, intBond, doubleBond, doubleBond1, doubleBond2, doubleBond3, doubleBond4,
			doubleBond5, doubleBond6, doubleBond7, doubleBond8));

	//
	//
	//

	View view = new ViewFactory().createView(viewId_, viewLabel, andBond);

	view.setSourceDeployment("sourceDeployment");
	view.setCreator("creator");
	view.setVisibility(View.ViewVisibility.PUBLIC);
	view.setOwner("owner");
	view.setCreationTime(new Date());
	view.setExpirationTime(new Date());

	//
	//
	//

	ViewFactory.createMarshaller().marshal(view, System.out);

    }

}
