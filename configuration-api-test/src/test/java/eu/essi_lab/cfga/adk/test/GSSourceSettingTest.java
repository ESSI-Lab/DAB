package eu.essi_lab.cfga.adk.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class GSSourceSettingTest {

    @Test
    public void discoveryOptionsTest() {

	GSSourceSetting setting = new GSSourceSetting();

	Assert.assertEquals(ResultsPriority.UNSET, setting.getResultsPriority());
	Assert.assertFalse(setting.getOrderingProperty().isPresent());
	Assert.assertEquals(OrderingDirection.ASCENDING, setting.getOrderingDirection());

	setting.setDiscoveryOptions(ResultsPriority.ALL, null, null);

	Assert.assertEquals(ResultsPriority.ALL, setting.getResultsPriority());
	Assert.assertFalse(setting.getOrderingProperty().isPresent());
	Assert.assertEquals(OrderingDirection.ASCENDING, setting.getOrderingDirection());

	setting.setDiscoveryOptions(null, MetadataElement.ABSTRACT, null);

	Assert.assertEquals(ResultsPriority.UNSET, setting.getResultsPriority());
	Assert.assertEquals(MetadataElement.ABSTRACT, setting.getOrderingProperty().get());
	Assert.assertEquals(OrderingDirection.ASCENDING, setting.getOrderingDirection());

	// if direction is set but the property  is missing, the default ASCENDING is returned
	setting.setDiscoveryOptions(null, null, OrderingDirection.DESCENDING);

	Assert.assertEquals(ResultsPriority.UNSET, setting.getResultsPriority());
	Assert.assertFalse(setting.getOrderingProperty().isPresent());
	Assert.assertEquals(OrderingDirection.ASCENDING, setting.getOrderingDirection());

	setting.setDiscoveryOptions(null, null, null);

	Assert.assertEquals(ResultsPriority.UNSET, setting.getResultsPriority());
	Assert.assertFalse(setting.getOrderingProperty().isPresent());
	Assert.assertEquals(OrderingDirection.ASCENDING, setting.getOrderingDirection());
    }

    /**
     * 
     */
    @Test
    public void test() {

	GSSourceSetting sourceSetting = new GSSourceSetting();

	defaultValuesTest(sourceSetting);
	defaultValuesTest(new GSSourceSetting(sourceSetting.getObject()));
	defaultValuesTest(new GSSourceSetting(sourceSetting.getObject().toString()));
	defaultValuesTest(SettingUtils.downCast(sourceSetting, GSSourceSetting.class, true));

	sourceSetting.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);
	sourceSetting.setSourceIdentifier("id");
	sourceSetting.setSourceEndpoint("endpoint");
	sourceSetting.setSourceLabel("label");
	sourceSetting.setDiscoveryOptions(ResultsPriority.COLLECTION, MetadataElement.ABSTRACT, OrderingDirection.DESCENDING);
	sourceSetting.addSourceDeployment("dep1");
	sourceSetting.addSourceDeployment("dep2");
	sourceSetting.addSourceDeployment("dep3");
	sourceSetting.setSourceComment("comment");

	setValuesTest(sourceSetting);
	setValuesTest(new GSSourceSetting(sourceSetting.getObject()));
	setValuesTest(new GSSourceSetting(sourceSetting.getObject().toString()));
	setValuesTest(SettingUtils.downCast(sourceSetting, GSSourceSetting.class, true));

	//
	//
	//

	GSSourceSetting clone = new GSSourceSetting(sourceSetting.clone().getObject().toString());
	SelectionUtils.deepClean(clone);

	setValuesTest(clone);
	setValuesTest(new GSSourceSetting(clone.getObject()));
	setValuesTest(new GSSourceSetting(clone.getObject().toString()));
	setValuesTest(SettingUtils.downCast(clone, GSSourceSetting.class, true));

	//
	//
	//

	GSSource gsSource = new GSSource();
	gsSource.setBrokeringStrategy(BrokeringStrategy.MIXED);
	gsSource.setUniqueIdentifier("id2");
	gsSource.setEndpoint("endpoint2");
	gsSource.setLabel("label2");
	gsSource.setOrderingDirection(OrderingDirection.ASCENDING);
	gsSource.setResultsPriority(ResultsPriority.DATASET);
	gsSource.setOrderingProperty(MetadataElement.TITLE.getName());
	gsSource.setVersion("3.0.0");

	sourceSetting.setSource(gsSource);

	setValuesTest2(sourceSetting);
	setValuesTest2(new GSSourceSetting(sourceSetting.getObject()));
	setValuesTest2(new GSSourceSetting(sourceSetting.getObject().toString()));
	setValuesTest2(SettingUtils.downCast(sourceSetting, GSSourceSetting.class, true));

	//
	//
	//

	clone = new GSSourceSetting(sourceSetting.clone().getObject().toString());
	SelectionUtils.deepClean(clone);

	setValuesTest2(clone);
	setValuesTest2(new GSSourceSetting(clone.getObject()));
	setValuesTest2(new GSSourceSetting(clone.getObject().toString()));
	setValuesTest2(SettingUtils.downCast(clone, GSSourceSetting.class, true));

	//
	//
	//

	try {
	    sourceSetting.setSource(null);
	    fail("Ex not thrown");
	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    private void defaultValuesTest(GSSourceSetting sourceSetting) {

	Assert.assertFalse(sourceSetting.getBrokeringStrategy().isPresent());

	Assert.assertEquals(ResultsPriority.UNSET, sourceSetting.getResultsPriority());
	Assert.assertEquals(OrderingDirection.ASCENDING, sourceSetting.getOrderingDirection());
	Assert.assertFalse(sourceSetting.getOrderingProperty().isPresent());

	Assert.assertNull(sourceSetting.getSourceLabel());
	Assert.assertNull(sourceSetting.getSourceEndpoint());
	Assert.assertNull(sourceSetting.getSourceIdentifier());

	Assert.assertFalse(sourceSetting.canBeDisabled());
	Assert.assertFalse(sourceSetting.isEditable());

	Assert.assertFalse(sourceSetting.getSourceComment().isPresent());
	
	Assert.assertTrue(sourceSetting.getSourceDeployment().isEmpty());

	//
	//
	//

	GSSource asSource = sourceSetting.asSource();

	Assert.assertNull(asSource.getBrokeringStrategy());
	Assert.assertNull(asSource.getVersion());
	Assert.assertEquals(ResultsPriority.UNSET, asSource.getResultsPriority());
	Assert.assertEquals(OrderingDirection.ASCENDING, asSource.getOrderingDirection());

	Assert.assertNull(asSource.getLabel());
	Assert.assertNull(asSource.getEndpoint());
	Assert.assertNull(asSource.getUniqueIdentifier());

	Assert.assertNull(asSource.getOrderingProperty());
	
	Assert.assertTrue(asSource.getDeployment().isEmpty());
    }

    private void setValuesTest(GSSourceSetting sourceSetting) {

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, sourceSetting.getBrokeringStrategy().get());
	// Assert.assertEquals("2.0.0", sourceSetting.getSourceVersion().get());
	Assert.assertEquals(ResultsPriority.COLLECTION, sourceSetting.getResultsPriority());
	Assert.assertEquals(OrderingDirection.DESCENDING, sourceSetting.getOrderingDirection());

	Assert.assertEquals("label", sourceSetting.getSourceLabel());
	Assert.assertEquals("endpoint", sourceSetting.getSourceEndpoint());
	Assert.assertEquals("id", sourceSetting.getSourceIdentifier());

	Assert.assertEquals(MetadataElement.ABSTRACT, sourceSetting.getOrderingProperty().get());

	Assert.assertEquals("comment", sourceSetting.getSourceComment().get());
	
	Assert.assertEquals(3, sourceSetting.getSourceDeployment().size());

	Assert.assertEquals("dep1", sourceSetting.getSourceDeployment().get(0));
	Assert.assertEquals("dep2", sourceSetting.getSourceDeployment().get(1));
	Assert.assertEquals("dep3", sourceSetting.getSourceDeployment().get(2));

	//
	//
	//

	GSSource asSource = sourceSetting.asSource();

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, asSource.getBrokeringStrategy());

	Assert.assertEquals(ResultsPriority.COLLECTION, asSource.getResultsPriority());
	Assert.assertEquals(OrderingDirection.DESCENDING, asSource.getOrderingDirection());

	Assert.assertEquals("label", asSource.getLabel());
	Assert.assertEquals("endpoint", asSource.getEndpoint());
	Assert.assertEquals("id", asSource.getUniqueIdentifier());

	Assert.assertEquals(MetadataElement.ABSTRACT.getName(), asSource.getOrderingProperty());
    }

    private void setValuesTest2(GSSourceSetting sourceSetting) {

	Assert.assertEquals(BrokeringStrategy.MIXED, sourceSetting.getBrokeringStrategy().get());

	// Assert.assertEquals("3.0.0", sourceSetting.getSourceVersion().get());
	Assert.assertEquals(ResultsPriority.DATASET, sourceSetting.getResultsPriority());
	Assert.assertEquals(OrderingDirection.ASCENDING, sourceSetting.getOrderingDirection());

	Assert.assertEquals("label2", sourceSetting.getSourceLabel());
	Assert.assertEquals("endpoint2", sourceSetting.getSourceEndpoint());
	Assert.assertEquals("id2", sourceSetting.getSourceIdentifier());

	Assert.assertEquals(MetadataElement.TITLE, sourceSetting.getOrderingProperty().get());

	//
	//
	//

	GSSource asSource = sourceSetting.asSource();

	Assert.assertEquals(BrokeringStrategy.MIXED, asSource.getBrokeringStrategy());

	Assert.assertEquals(ResultsPriority.DATASET, asSource.getResultsPriority());
	Assert.assertEquals(OrderingDirection.ASCENDING, asSource.getOrderingDirection());

	Assert.assertEquals("label2", asSource.getLabel());
	Assert.assertEquals("endpoint2", asSource.getEndpoint());
	Assert.assertEquals("id2", asSource.getUniqueIdentifier());

	Assert.assertEquals(MetadataElement.TITLE.getName(), asSource.getOrderingProperty());
    }

}
