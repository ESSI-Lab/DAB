package eu.essi_lab.identifierdecorator;

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

import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class IdentifierDecoratorTest {

	private DatabaseReader dbReader;
	private SourcePrioritySetting sourcePrioritySetting;

	@Before
	public void init() {

		dbReader = Mockito.mock(DatabaseReader.class);

		sourcePrioritySetting = Mockito.mock(SourcePrioritySetting.class);

	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void test0() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(GSException.class);

		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, false);

	}

	@Test
	public void test1() {
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		decorator.decorateDistributedIdentifier(resource);

		Assert.assertEquals(originalId + "@identifier", resource.getPublicId());
	}

	@Test
	public void test2() throws DuplicatedResourceException, ConflictingResourceException, GSException {
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, false);

		Assert.assertEquals(originalId + "@identifier", resource.getPublicId());
	}

	@Test
	public void test3() throws DuplicatedResourceException, ConflictingResourceException, GSException {
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, false);

		Assert.assertEquals(originalId + "@identifier", resource.getPublicId());
	}

	@Test
	public void test4() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setOriginalId(originalId);
		existing.setSource(source);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, false);

	}

	@Test
	public void test5() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setOriginalId(originalId);
		existing.setSource(source);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, true, false);

	}

	@Test
	public void test6() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setOriginalId(originalId);
		existing.setSource(source);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, true);

	}

	@Test
	public void test7() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setOriginalId(originalId);
		existing.setSource(source);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, true, true);

	}

	@Test
	public void test8() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource incomingResource = new Dataset();
		incomingResource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		incomingResource.setOriginalId(originalId);

		GSResource storedResource = new Dataset();
		storedResource.setOriginalId(originalId);
		storedResource.setSource(source);
		// time stamp is before the end harvesting timestamp, consolidated resource
		storedResource.getPropertyHandler().setResourceTimeStamp();
		HarvestingProperties harvestingProperties = new HarvestingProperties();
		harvestingProperties.setEndHarvestingTimestamp();

		// time stamp is after the end harvesting timestamp, is an incoming resource
		incomingResource.getPropertyHandler().setResourceTimeStamp();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(storedResource);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(incomingResource, harvestingProperties, null, false, false, false);

		Assert.assertEquals(originalId + "@identifier", incomingResource.getPublicId());

	}

	@Test
	public void test9() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);

		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource incomingResource = new Dataset();
		incomingResource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		incomingResource.setOriginalId(originalId);

		GSResource storedResource = new Dataset();
		storedResource.setOriginalId(originalId);
		storedResource.setSource(source);
		// time stamp is before the end harvesting timestamp, consolidated resource
		storedResource.getPropertyHandler().setResourceTimeStamp();
		HarvestingProperties harvestingProperties = new HarvestingProperties();
		harvestingProperties.setEndHarvestingTimestamp();

		// it means this is a re-harvesting
		harvestingProperties.setHarvestingCount(1);

		GSResource re_storedResource = new Dataset();
		re_storedResource.setOriginalId(originalId);
		re_storedResource.setSource(source);

		// time stamp is after the end harvesting timestamp, is a re-stored resource
		re_storedResource.getPropertyHandler().setResourceTimeStamp();

		// time stamp is after the end harvesting timestamp, is an incoming resource
		incomingResource.getPropertyHandler().setResourceTimeStamp();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(storedResource, re_storedResource);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(incomingResource, harvestingProperties, null, false, false, false);

	}

	@Test
	public void test10() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);

		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource incomingResource = new Dataset();
		incomingResource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		incomingResource.setOriginalId(originalId);

		GSResource storedResource = new Dataset();
		storedResource.setOriginalId(originalId);
		storedResource.setSource(source);
		// time stamp is before the end harvesting timestamp, consolidated resource
		storedResource.getPropertyHandler().setResourceTimeStamp();
		HarvestingProperties harvestingProperties = new HarvestingProperties();
		harvestingProperties.setEndHarvestingTimestamp();

		// it means this is a re-harvesting
		harvestingProperties.setHarvestingCount(1);

		GSResource re_storedResource = new Dataset();
		re_storedResource.setOriginalId(originalId);
		re_storedResource.setSource(source);

		// time stamp is after the end harvesting timestamp, is a re-stored resource
		re_storedResource.getPropertyHandler().setResourceTimeStamp();

		// time stamp is after the end harvesting timestamp, is an incoming resource
		incomingResource.getPropertyHandler().setResourceTimeStamp();

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(storedResource, re_storedResource);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(incomingResource, harvestingProperties, null, false, true, false);

		Assert.assertEquals(originalId + "@identifier", incomingResource.getPublicId());

	}

	@Test
	public void test11() throws DuplicatedResourceException, ConflictingResourceException, GSException {
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setSource(source);
		existing.setOriginalId(originalId);
		String existingDABId = "existingDABId";
		existing.setPublicId(existingDABId);
		existing.setPrivateId(existingDABId);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				String oid = invocationOnMock.getArgument(0);

				if (!oid.equalsIgnoreCase(originalId))
					throw new Exception("Expected " + originalId);

				GSSource gsSource = invocationOnMock.getArgument(1);
				if (!gsSource.getUniqueIdentifier().equalsIgnoreCase(source.getUniqueIdentifier()))
					throw new Exception("Expected " + source.getUniqueIdentifier());

				return existing;
			}
		}).when(dbReader).getResource(Mockito.any(), Mockito.any());

		Assert.assertNotEquals(existingDABId, resource.getPublicId());
		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, false, false, false);

		Assert.assertEquals(existingDABId, resource.getPublicId());
	}

	@Test
	public void test12() throws DuplicatedResourceException, ConflictingResourceException, GSException {
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setSource(source);
		existing.setOriginalId(originalId);
		String existingDABId = "existingDABId";
		existing.setPublicId(existingDABId);
		existing.setPrivateId(existingDABId);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				String oid = invocationOnMock.getArgument(0);

				if (!oid.equalsIgnoreCase(originalId))
					throw new Exception("Expected " + originalId);

				GSSource gsSource = invocationOnMock.getArgument(1);
				if (!gsSource.getUniqueIdentifier().equalsIgnoreCase(source.getUniqueIdentifier()))
					throw new Exception("Expected " + source.getUniqueIdentifier());

				return existing;
			}
		}).when(dbReader).getResource(Mockito.any(), Mockito.any());

		Assert.assertNotEquals(existingDABId, resource.getPublicId());
		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, false, true, false);

		Assert.assertEquals(existingDABId, resource.getPublicId());
	}

	@Test
	public void test13() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setSource(source);
		existing.setOriginalId(originalId);
		String existingDABId = "existingDABId";
		existing.setPublicId(existingDABId);
		existing.setPrivateId(existingDABId);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				String oid = invocationOnMock.getArgument(0);

				if (!oid.equalsIgnoreCase(originalId))
					throw new Exception("Expected " + originalId);

				GSSource gsSource = invocationOnMock.getArgument(1);
				if (!gsSource.getUniqueIdentifier().equalsIgnoreCase(source.getUniqueIdentifier()))
					throw new Exception("Expected " + source.getUniqueIdentifier());

				return existing;
			}
		}).when(dbReader).getResource(Mockito.any(), Mockito.any());

		Assert.assertNotEquals(existingDABId, resource.getPublicId());
		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, false, false, true);

	}

	@Test
	public void test14() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(DuplicatedResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);

		GSResource existing = new Dataset();
		existing.setSource(source);
		existing.setOriginalId(originalId);
		String existingDABId = "existingDABId";
		existing.setPublicId(existingDABId);
		existing.setPrivateId(existingDABId);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource, existing);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				String oid = invocationOnMock.getArgument(0);

				if (!oid.equalsIgnoreCase(originalId))
					throw new Exception("Expected " + originalId);

				GSSource gsSource = invocationOnMock.getArgument(1);
				if (!gsSource.getUniqueIdentifier().equalsIgnoreCase(source.getUniqueIdentifier()))
					throw new Exception("Expected " + source.getUniqueIdentifier());

				return existing;
			}
		}).when(dbReader).getResource(Mockito.any(), Mockito.any());

		Assert.assertNotEquals(existingDABId, resource.getPublicId());
		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, false, true, true);

	}

	@Test
	public void test15() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GSSource gsSource = invocationOnMock.getArgument(0);
				if (!source.getUniqueIdentifier().equalsIgnoreCase(gsSource.getUniqueIdentifier()))
					throw new Exception("Expected " + source.getUniqueIdentifier());

				return true;
			}
		}).when(sourcePrioritySetting).isPrioritySource(Mockito.any());

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);
		sameOrigiIdDifferentSource.setPublicId(UUID.randomUUID().toString());

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, false);

		Assert.assertEquals(originalId, resource.getPublicId());

	}



	@Test
	public void test16() throws DuplicatedResourceException, ConflictingResourceException, GSException {

		expectedException.expect(ConflictingResourceException.class);
		IdentifierDecorator decorator = Mockito.spy(new IdentifierDecorator(sourcePrioritySetting, Boolean.TRUE, dbReader));

		GSSource source = new GSSource();
		source.setLabel("Source");
		source.setUniqueIdentifier("identifier");

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

				GSSource gsSource = invocationOnMock.getArgument(0);
				if (!source.getUniqueIdentifier().equalsIgnoreCase(gsSource.getUniqueIdentifier()))
					throw new Exception("Expected " + source.getUniqueIdentifier());

				return true;
			}
		}).when(sourcePrioritySetting).isPrioritySource(Mockito.any());

		GSResource resource = new Dataset();
		resource.setSource(source);
		String originalId = UUID.randomUUID().toString();
		resource.setOriginalId(originalId);

		GSSource source2 = new GSSource();
		source2.setLabel("Source2");
		source2.setUniqueIdentifier("identifier2");

		GSResource sameOrigiIdDifferentSource = new Dataset();
		sameOrigiIdDifferentSource.setOriginalId(originalId);
		sameOrigiIdDifferentSource.setSource(source2);
		sameOrigiIdDifferentSource.setPublicId(originalId);

		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IdentifierType identifierType = invocationOnMock.getArgument(0);

				if (IdentifierType.ORIGINAL.compareTo(identifierType) != 0)
					throw new Exception("Expected " + IdentifierType.ORIGINAL);

				String identifier = invocationOnMock.getArgument(1);
				if (!originalId.equalsIgnoreCase(identifier))
					throw new Exception("Expected " + originalId);

				return Arrays.asList(sameOrigiIdDifferentSource);
			}
		}).when(dbReader).getResources(Mockito.any(), Mockito.any());

		decorator.decorateHarvestedIdentifier(resource, new HarvestingProperties(), null, true, false, false);



	}

}
