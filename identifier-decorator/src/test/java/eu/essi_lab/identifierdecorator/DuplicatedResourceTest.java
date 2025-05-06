/**
 *
 */
package eu.essi_lab.identifierdecorator;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;

/**
 * @author Fabrizio
 * @see https://confluence.geodab.eu/pages/viewpage.action?spaceKey=GPD&title=Identifier+Decorator
 */
public class DuplicatedResourceTest {

	private DatabaseReader dbReader;
	private Dataset incomingResource;
	private IdentifierDecorator decorator;
	private Dataset storedResource;
	private Dataset re_storedResource;
	private String originalId;

	/**
	 *
	 */
	public DuplicatedResourceTest() {

	}

	@Before
	public void init() {

		dbReader = Mockito.mock(DatabaseReader.class);

		originalId = UUID.randomUUID().toString();

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("idenfier");

		incomingResource = new Dataset();
		incomingResource.setSource(source);
		incomingResource.setOriginalId(originalId);

		storedResource = new Dataset();
		storedResource.setSource(source);
		storedResource.setOriginalId(originalId);

		re_storedResource = new Dataset();
		re_storedResource.setSource(source);
		re_storedResource.setOriginalId(originalId);

		try {
			Mockito.when(dbReader.getResources(IdentifierType.ORIGINAL, originalId)).thenReturn(Arrays.asList(storedResource));
		} catch (GSException e) {
		}

		SourcePrioritySetting sourcePrioritySetting = Mockito.mock(SourcePrioritySetting.class);
		Mockito.doReturn(Boolean.TRUE).when(sourcePrioritySetting).preserveIdentifiers();
		decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, dbReader));

		Mockito.when(decorator.getDatabaseReader()).thenReturn(dbReader);
	}

	@Test
	public void case1Test() {

		HarvestingProperties harvestingProperties = new HarvestingProperties();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //
					true, // first harvesting
					false, // is recovery
					false); // is incremental

		} catch (DuplicatedResourceException e) {

			return;

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException thrown");
		}

		Assert.fail("DuplicatedResourceException not thrown");
	}

	@Test
	public void case2RecoveryTest() {

		HarvestingProperties harvestingProperties = new HarvestingProperties();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //

					true, // first harvesting
					true, // is recovery
					false); // is incremental

		} catch (DuplicatedResourceException e) {

			return;

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException thrown");
		}

		Assert.fail("DuplicatedResourceException not thrown");
	}

	@Test
	public void case2IncrementalTest() {

		HarvestingProperties harvestingProperties = new HarvestingProperties();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //

					true, // first harvesting
					false, // is recovery
					true); // is incremental

		} catch (DuplicatedResourceException e) {

			return;

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException thrown");
		}

		Assert.fail("DuplicatedResourceException not thrown");
	}

	@Test
	public void case3Test() {

		HarvestingProperties harvestingProperties = new HarvestingProperties();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //

					false, // first harvesting
					false, // is recovery
					true); // is incremental

		} catch (DuplicatedResourceException e) {

			return;

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException thrown");
		}

		Assert.fail("DuplicatedResourceException not thrown");
	}

	@Test
	public void case4Test() {

		// time stamp is before the end harvesting timestamp, consolidated resource
		storedResource.getPropertyHandler().setResourceTimeStamp();

		HarvestingProperties harvestingProperties = new HarvestingProperties();
		harvestingProperties.setEndHarvestingTimestamp();

		// time stamp is after the end harvesting timestamp, is an incoming resource
		incomingResource.getPropertyHandler().setResourceTimeStamp();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //

					false, // first harvesting
					false, // is recovery
					false); // is incremental

		} catch (DuplicatedResourceException e) {

			Assert.fail("DuplicatedResourceException thrown");

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException not thrown");
		}

		// OK
	}

	@Test
	public void case5Test() {

		// now there are 3 resources. the incoming, the stored and the re-stored
		try {
			Mockito.when(dbReader.getResources(IdentifierType.ORIGINAL, originalId)).//
					thenReturn(Arrays.asList(storedResource, re_storedResource));

		} catch (GSException e) {
		}

		// time stamp is before the end harvesting timestamp, consolidated resource
		storedResource.getPropertyHandler().setResourceTimeStamp();

		HarvestingProperties harvestingProperties = new HarvestingProperties();
		harvestingProperties.setEndHarvestingTimestamp();

		// it means this is a re-harvesting
		harvestingProperties.setHarvestingCount(1);

		// time stamp is after the end harvesting timestamp, is a re-stored resource
		re_storedResource.getPropertyHandler().setResourceTimeStamp();

		// time stamp is after the end harvesting timestamp, is an incoming resource
		incomingResource.getPropertyHandler().setResourceTimeStamp();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //

					false, // first harvesting
					false, // is recovery
					false); // is incremental

		} catch (DuplicatedResourceException e) {

			return;

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException not thrown");
		}

		Assert.fail("DuplicatedResourceException not thrown");
	}

	@Test
	public void case6Test() {

		// now there are 3 resources. the incoming, the stored and the re-stored
		try {
			Mockito.when(dbReader.getResources(IdentifierType.ORIGINAL, originalId)).//
					thenReturn(Arrays.asList(storedResource, re_storedResource));

		} catch (GSException e) {
		}

		// time stamp is before the end harvesting timestamp, consolidated resource
		storedResource.getPropertyHandler().setResourceTimeStamp();

		HarvestingProperties harvestingProperties = new HarvestingProperties();
		harvestingProperties.setEndHarvestingTimestamp();

		// it means this is a re-harvesting
		harvestingProperties.setHarvestingCount(1);

		// time stamp is after the end harvesting timestamp, is an incoming resource
		incomingResource.getPropertyHandler().setResourceTimeStamp();

		// time stamp is after the end harvesting timestamp, is a re-stored resource
		re_storedResource.getPropertyHandler().setResourceTimeStamp();

		try {
			decorator.decorateHarvestedIdentifier(//
					incomingResource, //
					harvestingProperties, //
					null, //

					false, // first harvesting
					true, // is recovery
					false); // is incremental

		} catch (DuplicatedResourceException e) {

			return;

		} catch (ConflictingResourceException e) {

			Assert.fail("ConflictingResourceException thrown");

		} catch (GSException e) {

			Assert.fail("GSException not thrown");
		}

		Assert.fail("DuplicatedResourceException not thrown");
	}
}
